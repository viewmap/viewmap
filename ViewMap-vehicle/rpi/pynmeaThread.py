class pynmeaThread(threading.Thread):
    def run(self):
        self.DayTime = datetime.datetime.now()
        self.NewLocation = (0.0, 0.0)
        self.OldLocation = self.NewLocation

        serialPort = serial.Serial("/dev/ttyAMA0", 9600, timeout=0.5)
        while True:
            try:
                string = serialPort.readline().decode('ascii')
                if string.find('GGA') > 0:
                    msg = pynmea2.parse(string)
                    self.NewLocation = (int(float(msg.lat)/100)+(float(msg.lat)%100)/60, int(float(msg.lon)/100)+(float(msg.lon)%100)/60)
                    self.OldLocation = self.NewLocation
                if string.find('RMC') > 0:
                    rmcString = string.split(',')
                    dayYear, dayMon, dayDay = int(str(rmcString[9])[4:6]), int(str(rmcString[9])[2:4]), int(str(rmcString[9])[0:2])
                    timeHour, timeMin, timeSec = int(str(rmcString[1])[0:2]), int(str(rmcString[1])[2:4]), int(str(rmcString[1])[4:6])
                    dayYear += 2000
                    if timeHour > 14:
                        dayDay += 1
                        timeHour -= 15
                    else:
                        timeHour += 9
                    self.DayTime = datetime.datetime(dayYear, dayMon, dayDay, timeHour, timeMin, timeSec)
            except Exception, e:
                self.DayTime = datetime.datetime.now()
                self.NewLocation = self.OldLocation
                pass