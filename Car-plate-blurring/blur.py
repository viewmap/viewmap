import io
import cv2
import picamera
from picamera.streams import PiCameraCircularIO
from picamera.array import PiRGBArray
import picamera.array
import numpy as np
from time import sleep, time
from multiprocessing import Process, Queue, Pipe, Manager, Value, Array, Pool, Lock
import ctypes

from fractions import gcd

import sys

cascade_fn = 'eu.xml'
cascade = cv2.CascadeClassifier(cascade_fn)
n = 1

def detect(img, cascade):
        rects = cascade.detectMultiScale(img, 1.3, 1, minSize=(60 / n, 10 / n), maxSize=(180/n, 30/n), flags=cv2.CASCADE_DO_CANNY_PRUNING)
        if len(rects) == 0: return []
        rects[:, 2:] += rects[:,:2]
        return rects

def blurLicensePlate(image):
        
        copy = image.copy()
        size = image.shape[:2]
        image = cv2.resize(image, (size[1] / n, size[0] / n))
        image = image[0:size[0] / n, 0:size[1] / n]

        
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

        roi = gray[size[0] / 3 / n:size[0],:]
        rects = detect(roi, cascade)
        
        vis = image.copy()
        for x1, y1, x2, y2 in rects:
                
                roi = vis[size[0] / 3 / n + y1:size[0] / 3 / n + y2, x1:x2]
                
                roi_size = roi.shape[:2]
                roi = cv2.resize(roi, (roi_size[1] / 10, roi_size[0] / 10))
                roi = cv2.resize(roi, (roi_size[1] * n, roi_size[0] * n), interpolation=cv2.INTER_NEAREST)
                vis[(size[0] / 3 / n + y1) * n:(size[0] / 3 / n + y2) * n, x1 * n:(x2) * n] = roi

        return vis


class MyOutput(object):
        def __init__(self, camera, id, q, state=1):
                self.cam = camera
                self.id = id
                self.data = []
                self.q = q
                self.state = state
                
                self.t = time()
                self.count = 0

                
        def write(self, s):
                
                if self.state == 0:
                        self.count += 1
##                        blur = blurLicensePlate(picamera.array.bytes_to_rgb(s, self.cam.resolution))
                        blur = picamera.array.bytes_to_rgb(s, self.cam.resolution)
                        self.data.append(blur)
                elif self.state == 1:
                        if len(self.data) > 0:
                                self.q.put(self.data.pop(0))
                


def process(file, resolution, q):
        writer = cv2.VideoWriter(file, cv2.VideoWriter_fourcc(*'MJPG'), 9, resolution)
        while True:
                i = q.get()
                if i == None: break
                writer.write(i)
        writer.release()
        

# capture video
def capture(path, ext):
        with picamera.PiCamera() as cam:
                cam.resolution = (1280, 720)
                cam.framerate = 30

                video_count = 30
                

                q = Queue()
                
                outputs = [MyOutput(cam, 1, q, 0), MyOutput(cam, 2, q)]
                
                


                while True:
                        cam.start_recording(outputs[0], format='bgr')
                        cam.start_recording(outputs[1], format='bgr', splitter_port=2)
                        
                        video_count += 1
                        t = time()

                        print(path + str(video_count) + ext)
                        p = Process(target=process, args=(path + str(video_count) + ext, cam.resolution, q))
                        p.start()

                        outputs[0].state = 0
                        outputs[1].state = 1
                        
                        while time() - t <= 60:
                                cam.wait_recording(0.5)

                                if outputs[1].state == 1  and len(outputs[1].data) == 0 and len(outputs[0].data) > 30:
                                        outputs[0].count = outputs[1].count = 0
                                        outputs[1].state = 0
                                        outputs[0].state = 1
                                        outputs[1], outputs[0] = outputs[0], outputs[1]


                        cam.stop_recording()
                        cam.stop_recording(splitter_port=2)

                        start = time()
                        outputs[0].state = 2
                        
                        while len(outputs[1].data) > 0:
                                print(len(outputs[1].data))
                                q.put(outputs[1].data.pop(0))
                                if outputs[1].state == 1  and len(outputs[1].data) == 0:
                                        print(outputs[0].count, outputs[1].count)
                                        outputs[1].state = 2
                                        outputs[0].state = 1
                                        outputs[1], outputs[0] = outputs[0], outputs[1]


                        q.put(None)
                        p.join()
                        print('elapsed', time() - start)

               

if __name__ == '__main__':
        ext = '.avi'
        capture('Videos/', ext)
