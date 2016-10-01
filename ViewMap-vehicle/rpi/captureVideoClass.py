class captureVideoClass:
    def __init__(self, videoFileName):
        self.fileName = videoFileName
        self.capture('/home/pi/Videos/', self.fileName)

    def process(self, file, resolution, q):
        writer = cv2.VideoWriter(file, cv2.VideoWriter_fourcc(*'MJPG'), 9, resolution)
        while True:
            i = q.get()
            if i == None:
                break
            writer.write(i)
        writer.release()
        
    def capture(self, path, fileName):
        with picamera.PiCamera() as cam:
            cam.resolution = (1280, 720)
            cam.framerate = 30
            q = Queue()
            outputs = [MyOutput(cam, 1, q, 0), MyOutput(cam, 2, q)]
            cam.start_recording(outputs[0], format='bgr')
            cam.start_recording(outputs[1], format='bgr', splitter_port=2)
            t = time()

            print(path + fileName)
            p = Process(target=self.process, args=(path + fileName, cam.resolution, q))
            p.start()
            outputs[0].state = 0
            outputs[1].state = 1

            while time() - t < 56:
                cam.wait_recording()
                if outputs[1].state == 1  and len(outputs[1].data) == 0 and len(outputs[0].data) > 30:
                    outputs[0].count = outputs[1].count = 0
                    outputs[1].state = 0
                    outputs[0].state = 1
                    outputs[1], outputs[0] = outputs[0], outputs[1]
            cam.stop_recording()
            cam.stop_recording(splitter_port=2)
            outputs[0].state = 2
                        
            while len(outputs[1].data) > 0:
                q.put(outputs[1].data.pop(0))
                if outputs[1].state == 1 and len(outputs[1].data) == 0:
                    outputs[1].state = 2
                    outputs[0].state = 1
                    outputs[1], outputs[0] = outputs[0], outputs[1]
            q.put(None)
            p.join()