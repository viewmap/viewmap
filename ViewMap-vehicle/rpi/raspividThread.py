class raspividThread(threading.Thread):
    def diskUsedRate(self):
        disk = os.statvfs("/")
        free = disk.f_bavail * disk.f_frsize
        total = disk.f_blocks * disk.f_frsize
        used = disk.f_frsize * (disk.f_blocks - disk.f_bfree)
        rate = float(used/1024/1024)/float(total/1024/1024) * 100
        return (rate)
    def run(self):
        self.FileName = ""
        self.videoFileName = ""
        self.Qu = ""
        self.Ru = ""
        locationList = []
        self.lat, self.lon = 0.0, 0.0
        que = Queue()
        flag = True
        while True:
            try:
                if self.FileName == "" or self.FileName[11:13] != "00":
                    if self.diskUsedRate() > 90.0:
                        pwd = '/home/pi/Videos/'
                        fileList = []
                        for (path, dirs, files) in os.walk(pwd):
                            for file in files:
                                if os.path.splitext(file)[1].lower() =='':
                                    fileList.append(file)
                                os.chdir(pwd)
                                os.remove(min(fileList))
                    continue
                self.nowFileName = self.FileName
                self.nowlat, self.nowlon = self.lat, self.lon
                locationList.append([self.nowlat, self.nowlon])
                self.Qu = '{0:08d}'.format(random.randint(0,99999999))
                self.Ru = hashlib.md5(self.Qu).hexdigest()
                self.videoFileName = self.nowFileName
                captureVideoClass(self.videoFileName)
                que.put(self.videoFileName)
                numQue = que.qsize()
                count = 0
            except Exception, e:
                print(e)
                with open('/home/pi/ERRORLOG.txt', 'ab') as openf:
                    openf.write(str(strftime('%Y-%m-%d %H:%M:%S ')) + "ThreadClass0:" + str(e) + "\n")
                pass