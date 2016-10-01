class vanetDsrcThread(threading.Thread):
    def run(self):
        self.states = {}
        self.initstates = {}
        self.mylocation = (0.0, 0.0)
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_address = ('', 9913)
        self.sock.bind(server_address)
        self.sock.listen(5)
        while True:
            sleep(1)
            client, address = self.sock.accept()
            data = client.recv(1024)
            data_recv = data.strip().split('|')
            try:
                with open('/home/pi/explog.txt', 'ab') as openf:
                        openf.write(data)
                if 500.00 >= float(data_recv[7]):
                    data_recv_at = data_recv[8].strip().split(':')
                    if data_recv_at[3] in self.states:
                        del self.states[data_recv_at[3]]
                        self.states[data_recv_at[3]] = data_recv[8]
                    else:
                        self.states[data_recv_at[3]] = data_recv[8]
                        self.initstates[data_recv_at[3]] = data_recv[8]
            except Exception, e:
                pass
            finally:
                client.close()