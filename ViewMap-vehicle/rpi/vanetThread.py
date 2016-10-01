class vanetThread(threading.Thread):
    def run(self):
        self.states = {}
        self.initstates = {}
        self.mylocation = (0.0, 0.0)
        distance_m = 0.0
        ipself = ""
        iface = pyiface.getIfaces()
        for interface in iface:
            try:
                subinterface = str(interface).split()
                interfaceoption = subinterface.index('Bcast:A.B.C.D')
                ipself = subinterface[interfaceoption - 1][5:]
            except Exception, E:
                with open('/home/pi/ERRORLOG.txt', 'ab') as openf:
                    openf.write(str(strftime('%Y-%m-%d %H:%M:%S ')) + "ThreadClass1:" + str(E) + "\n")
                pass
        UDPSock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        UDPSock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        UDPSock.bind(('', 33333))
        
        while True:
            data, addr = UDPSock.recvfrom(2048)
            if ipself != "" and addr[0] != ipself:
                data_recv = data.strip().split(':')
                try:
                    data_recv_gps = data_recv[1].split(',')
                    self.oplocation = (float(data_recv_gps[0]), float(data_recv_gps[1]))
                    distance_m = haversine.haversine(self.mylocation, self.oplocation) * 1000
                    if 500.00 >= distance_m:
                        if data_recv[3] in self.states:
                            del self.states[data_recv[3]]
                            self.states[data_recv[3]] = data
                        else:
                            self.states[data_recv[3]] = data
                            self.initstates[data_recv[3]] = data
                except Exception, E:
                    with open('/home/pi/ERRORLOG.txt', 'ab') as openf:
                        openf.write(str(strftime('%Y-%m-%d %H:%M:%S ')) + "ThreadClass1:" + str(E) + "\n")
                    pass
        UDPSock.close()