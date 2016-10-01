class videoTransfer:
    def make_header(self, dtype, code, misc):
        header = {"type":dtype, "code":code, "misc": misc}
        return header
    def send_hello(self, url):
        session = requesocks.session()
        session.proxies = {
            "http": "socks5://127.0.0.1:9050",
            "https": "socks5://127.0.0.1:9050",
        }
        header = self.make_header("1", "0", "0")
        resp = session.post(url, data = header)
        print "status code >> "+resp.status_code
        print "recv text >> "+resp.text
        return resp.text
    def send_lastseen(self, url):
        session = requesocks.session()
        session.proxies = {
            "http": "socks5://127.0.0.1:9050",
            "https": "socks5://127.0.0.1:9050",
        }
        current_lastseen = self.get_lastseen()
        header = self.make_header("1","5",current_lastseen)
        resp = session.post(url, data = header)
        print "status code >> "+str(resp.status_code)
        print "recv text >> "+str(resp.text)
        self.update_lastseen()
        # recv Ru List
        return resp.text
    def get_lastseen(self):
        try:
            con = sqlite3.connect("/home/pi/blackbox.sql")
            cursor = con.cursor()
            cursor.execute("CREATE TABLE blackboxtime(LASTSEEN timestamp)")
            cursor.execute("INSERT INTO blackboxtime VALUES('2016-01-01 12:00:00')")
            con.commit()
            cursor.execute("SELECT * FROM blackboxtime")
            for result in cursor:
                final = result[0]
            con.close()
        except sqlite3.OperationalError :
            con = sqlite3.connect("/home/pi/blackbox.sql")
            cursor = con.cursor()
            cursor.execute("SELECT * FROM blackboxtime")
            for result in cursor:
                final = result[0]
            con.close()
        lastseen = final[0:4] + final[5:7] + final[8:10] + final[11:13] + final[14:16] + final[17:19]
        print lastseen
        return lastseen
    def update_lastseen(self):
        con = sqlite3.connect("/home/pi/blackbox.sql")
        cursor = con.cursor()
        cursor.execute("INSERT INTO blackboxtime VALUES('" + str(datetime.datetime.now()) + "')")
        con.commit()
        con.close()
        return
    def send_video(self, url, video_file, cookie):
        print '/home/pi/Videos/' + video_file
        session = requesocks.session()
        session.proxies = {
            "http": "socks5://127.0.0.1:9050",
            "https": "socks5://127.0.0.1:9050",
        }
        header = self.make_header("2", "3", cookie)
        files = {"payload" : open('/home/pi/Videos/' + video_file,"rb")}
        resp = session.post(url, data=header, files=files)
        print "status code >> "+str(resp.status_code)
        return resp.text
    def str_to_dict(self, input_str):
        return ast.literal_eval(input_str)
    def get_vp(self, input_ru):
        vp = "TARGET_VP"
        return vp
    def send_hello_video(self, url, ru):
        session = requesocks.session()
        session.proxies = {
            "http": "socks5://127.0.0.1:9050",
            "https": "socks5://127.0.0.1:9050",
            }
        con = sqlite3.connect("/home/pi/blackbox.sql")
        cursor = con.cursor()
        cursor.execute("select * from blackboxfile where RU = '" + ru + "'")
        for result in cursor:
            vp = result[3] + "\n" + result[0]
            fn = result[1]
            break
        with open('/home/pi/Videos/' + fn, 'rb') as openf:
            data = openf.read()
            with open('/home/pi/Videos/' + ru, 'wb') as writef:
                writef.write(data)
        vfs = len(data)
        file_size = vfs

        with open('/home/pi/Videos/' + 'vpsample.txt', 'wb') as openf:
            openf.write(vp)

        header = self.make_header("2", "0", file_size)
        files = {"payload" : open('/home/pi/Videos/' + 'vpsample.txt',"rb")}
        resp = session.post(url, data=header, files=files)
        print "status code >> "+str(resp.status_code)
        return resp.text
    def subrun(self):
        vrl = self.send_lastseen("http://A.B.C.D:8000")
        vrl_dict = self.str_to_dict(vrl)
        payload_dict = self.str_to_dict(str(vrl_dict["payload"]))
        ru_list = str(payload_dict["rus"]).split(">>>")
        try:
            con = sqlite3.connect("/home/pi/blackbox.sql")
            cursor = con.cursor()
            cursor.execute("CREATE TABLE blackboxrequest(RU text)")
            for ru in ru_list:
                if ru == '':
                    continue
                cursor.execute("INSERT INTO blackboxrequest VALUES('" + ru + "')")
                con.commit()
                con.close()
        except sqlite3.OperationalError :
            con = sqlite3.connect("/home/pi/blackbox.sql")
            cursor = con.cursor()
            for ru in ru_list:
                if ru == '':
                    continue
                cursor.execute("INSERT INTO blackboxrequest VALUES('" + ru + "')")
                con.commit()
                con.close()
        return
    def __init__(self):
        self.subrun()
        con = sqlite3.connect("/home/pi/blackbox.sql")
	cursor = con.cursor()
	cursor.execute("SELECT RU FROM blackboxrequest")
	for result in cursor:
                url = "http://A.B.C.D:8000"
                ru = str(result[0])
                vrl = send_hello_video(url, ru)
                vrl_dict = str_to_dict(vrl)
                start_time = time()
                if vrl_dict["type"] ==2 and vrl_dict["code"] == 1:
                        send_video(url, ru, str(vrl_dict["misc"]))
                end_time = time()
                print (end_time - start_time)
        con.close()