class TunnelThread(threading.Thread):
    def run(self):
        sock0 = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_address0 = ('', 9912)
        sock0.bind(server_address0)
        sock0.listen(5)
        while True :
            client, address = sock0.accept()
            data = client.recv(1024)
            if data :
                client.send(data)
                client.close()
            try :
                sock1 = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                server_address1 = ('A.B.C.D', 9913)
                sock1.connect(server_address1)
                sock1.sendall(data)
                sock1.recv(128)
            except socketError :
                sleep(1)
                pass
            finally :
                sock1.close()