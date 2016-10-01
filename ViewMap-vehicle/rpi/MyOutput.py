class MyOutput(object):
    def __init__(self, camera, id, q, state=1):
        self.cam = camera
        self.id = id
        self.data = []
        self.q = q
        self.state = state
                
        self.t = time()
        self.count = 0
    def blurLicensePlate(self, image):
        n = 4
        copy = image.copy()
        size = image.shape[:2]
        image = cv2.resize(image, (size[1] / n, size[0] / n))
        image = image[0:size[0] / n, 0:size[1] / n]
        gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

        kernel = np.ones((3 / n, 9 / n), np.uint8)# 3,9
        
        dilated = cv2.dilate(gray, kernel)
        eroded = cv2.erode(dilated, kernel)
        tophat = cv2.subtract(eroded, gray)
        blur = cv2.GaussianBlur(tophat, (3, 3), 0) #5,5
        ret, threshold = cv2.threshold(blur, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
        _, contours, hierarchy = cv2.findContours(threshold, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_NONE)

        for cnt in contours:
            x, y, w, h = rect = cv2.boundingRect(cnt)
            x, y, w, h = x - 5, y - 5, w + 10, h + 10

            if x < 0 or y < 0 or x + w > size[1] / n or y + h > size[0] / n:
                continue
            if y < size[0] / 3 / n:
                continue
            if w < 5 or h < 5:
                continue
            if w > 300 / n or h > 300 / n or w * h > 10000 / n / n:
                continue

            roi = image[y:y+h, x:x+w]
            roi_size = roi.shape[:2]
            roi = cv2.resize(roi, (roi_size[1] / 3, roi_size[0] / 3))
            roi = cv2.resize(roi, (roi_size[1] * n, roi_size[0] * n), interpolation=cv2.INTER_NEAREST)
            copy[y * n:(y+h) * n, x * n:(x+w) * n] = roi
        return copy
    def write(self, s):
        if self.state == 0:
            self.count += 1
            blur = self.blurLicensePlate(picamera.array.bytes_to_rgb(s, self.cam.resolution))
            self.data.append(blur)
        elif self.state == 1:
            if len(self.data) > 0:
                self.q.put(self.data.pop(0))
    byte = 0
    bytePlusHash = ""
    oldFileName = ""
    newHash0 = ""
    newHash1 = ""
    oldHash = "00000000000000000000000000000000"
    timeCounter = 0
    raspividThread = raspividThread()
    pynmeaThread = pynmeaThread()
    vanetDsrcThread = vanetDsrcThread()
    TunnelThread = TunnelThread()
    raspividThread.start()
    pynmeaThread.start()
    vanetDsrcThread.start()
    TunnelThread.start()

    while True:
        sleep(1)
        gpsData = '{:%Y%m%d%H%M%S}'.format(pynmeaThread.DayTime) + ":" +  '{:f}'.format(pynmeaThread.NewLocation[0]) + "," + '{:f}'.format(pynmeaThread.NewLocation[1])
        fileName = '{:%y%m%d_%H%M%S}'.format(pynmeaThread.DayTime)
        raspividThread.FileName = fileName
        raspividThread.lat, raspividThread.lon = pynmeaThread.NewLocation[0], pynmeaThread.NewLocation[1]
        nowFileName = raspividThread.videoFileName
        tempQu = raspividThread.Qu
        newHash0 = raspividThread.Ru
        nowGPSData = gpsData
        vanetDsrcThread.mylocation = pynmeaThread.NewLocation
        tempVideoData = []
        try:
            if nowFileName != oldFileName:
                if len(vanetDsrcThread.states) >= 0 and newHash1 != "":
                    state = []
                    startCoordinate = []
                    for k in vanetDsrcThread.states.keys():
                        tempSplit = vanetDsrcThread.states.get(k).split(':')
                        state.append(tempSplit[0]+':'+tempSplit[1]+':'+tempSplit[2]+':'+tempSplit[3]+':'+tempSplit[4])
                        startCoordinate.append(vanetDsrcThread.states.get(k).split(':')[5])
                    for k in vanetDsrcThread.initstates.keys():
                        tempSplit = vanetDsrcThread.initstates.get(k).split(':')
                        state.append(tempSplit[0]+':'+tempSplit[1]+':'+tempSplit[2]+':'+tempSplit[3]+':'+tempSplit[4])

                    endCoordinate = '{:f}'.format(pynmeaThread.NewLocation[0]) + "," + '{:f}'.format(pynmeaThread.NewLocation[1])
                    tmapclass = tmapClass(startCoordinate, endCoordinate, bytePlusHash.split(':')[0])
                    listFake = tmapclass.get_listReturn()
                    for x in listFake:
                        state.append(x)
                    bloomFilter = BloomFilter(num_bytes=256, num_probes=6, iterable=state)
                    with open('/home/pi/messageView.txt', 'ab') as openf:
                        textHashOldFileName = newHash0
                        textBloomFilter = binascii.hexlify(bloomFilter.array).decode()
                        openf.write(textHashOldFileName + "\n" + textBloomFilter + "\n" + bytePlusHash)
                    try :
                        con = sqlite3.connect("/home/pi/blackbox.sql")
                        cursor = con.cursor()
                        cursor.execute("CREATE TABLE blackboxfile(PROFILE text, NAME text, QU text, RU text, PROFILESEND integer, VIDEOSEND integer)")
                        cursor.execute("INSERT INTO blackboxfile VALUES('" + textBloomFilter + "\n" + bytePlusHash + "', '" + oldFileName + "', '" + tempQu + "', '" + newHash0 + "', '0', '0')")
                        for x in tmapclass.get_bytePlusHash():
                            cursor.execute("INSERT INTO blackboxfile VALUES('" + textBloomFilter + "\n" + x + "', '" + oldFileName + "', '" + tempQu + "', '" + newHash0 + "', '0', '0')")
                        con.commit()
                        con.close()

                    except sqlite3.OperationalError as E:
                        with open('/home/pi/ERRORLOG.txt', 'ab') as openf:
                            openf.write(str(strftime('%Y-%m-%d %H:%M:%S ')) + "MAIN:" + str(E) + "\n")
                        cursor.execute("INSERT INTO blackboxfile VALUES('" + textBloomFilter + "\n" + bytePlusHash + "', '" + oldFileName + "', '" + tempQu + "', '" + newHash0 + "', '0', '0')")
                        for x in tmapclass.get_bytePlusHash():
                            cursor.execute("INSERT INTO blackboxfile VALUES('" + textBloomFilter + "\n" + x + "', '" + oldFileName + "', '" + tempQu + "', '" + newHash0 + "', '0', '0')")
                        con.commit()
                        con.close()
                byte = 0
                bytePlusHash = ""
                vanetDsrcThread.states = {}
                vanetDsrcThread.initstates = {}
                tempVideoData = []
                oldFileName = nowFileName
                oldHash = "00000000000000000000000000000000"

                vpTransfer()
                videoTransfer()
            if timeCounter == 0: 
                with open('/home/pi/Videos/' + nowFileName, 'rb') as openf:
                    openf.seek(byte, 0)
                    data = openf.read()
                    byte += len(data)
                timeCounter = 9
            else:
                timeCounter -= 1
        except Exception as E:
            with open('/home/pi/ERRORLOG.txt', 'ab') as openf:
                openf.write(str(strftime('%Y-%m-%d %H:%M:%S ')) + "MAIN:" + str(E) + "\n")
	    pass
	else:
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            
            try:
                hashOrigin = nowGPSData + ":" + '{0:08d}'.format(byte) + ":" + oldHash + ":" + data
                newHash1 = hashlib.md5(hashOrigin).hexdigest()
                bytePlusHash += nowGPSData + ":" + newHash0 + ":" + '{0:08d}'.format(byte) + ":" + newHash1 + "\n"
                sendstr = nowGPSData + ":" + '{0:08d}'.format(byte) + ":" + newHash0 + ":" + newHash1 + ":" + '{:f}'.format(raspividThread.nowlat) + "," + '{:f}'.format(raspividThread.nowlon)
                server_address = ('A.B.C.D', 9911)
                sock.connect(server_address)
		sock.sendall(sendstr)
		sock.recv(128)
                oldHash = newHash1
	    except Exception as E:
                with open('/home/pi/ERRORLOG.txt', 'ab') as openf:
                    openf.write(str(strftime('%Y-%m-%d %H:%M:%S ')) + "MAIN:" + str(E) + "\n")
                    pass
            finally:
                sock.close()