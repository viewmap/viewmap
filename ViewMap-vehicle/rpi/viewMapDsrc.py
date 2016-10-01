if __name__ == "__main__":
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
        fileName = '{:%y%m%d_%H%M%S}'.format(pynmeaThread.DayTime) + '.avi'
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
                    mapclass = mapClass(startCoordinate, endCoordinate, bytePlusHash.split(':')[0])
                    listFake = mapclass.get_listReturn()
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
                        for x in mapclass.get_bytePlusHash():
                            cursor.execute("INSERT INTO blackboxfile VALUES('" + textBloomFilter + "\n" + x + "', '" + oldFileName + "', '" + tempQu + "', '" + newHash0 + "', '0', '0')")
                        con.commit()
                        con.close()

                    except sqlite3.OperationalError as E:
                        with open('/home/pi/ERRORLOG.txt', 'ab') as openf:
                            openf.write(str(strftime('%Y-%m-%d %H:%M:%S ')) + "MAIN:" + str(E) + "\n")
                        cursor.execute("INSERT INTO blackboxfile VALUES('" + textBloomFilter + "\n" + bytePlusHash + "', '" + oldFileName + "', '" + tempQu + "', '" + newHash0 + "', '0', '0')")
                        for x in mapclass.get_bytePlusHash():
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