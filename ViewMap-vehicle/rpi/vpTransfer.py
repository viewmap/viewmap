class vpTransfer:
    def make_header(self, dtype, code, misc):
        header = {"type":dtype, "code":code, "misc": misc}
        return header
    def send_hello(self, url):
        final = 0
	session = requesocks.session()

	session.proxies = {
            "http": "socks5://127.0.0.1:9050",
            "https": "socks5://127.0.0.1:9050",
	}
	con = sqlite3.connect("/home/pi/blackbox.sql")
        cursor = con.cursor()
        cursor.execute("select count(name) from blackboxfile where PROFILESEND='0'")
        for result in cursor:
            final = int(result[0])
        con.close()

	header = self.make_header(0,0,final)
        resp = session.post(url, data=header)
        print "status code >> "+ str(resp.status_code)

        return resp.text
    def send_vp(self, url):
        final = 0
        flag = True
	session = requesocks.session()

	session.proxies = {
            "http": "socks5://127.0.0.1:9050",
            "https": "socks5://127.0.0.1:9050",
	}

        con = sqlite3.connect("/home/pi/blackbox.sql")
        cursor = con.cursor()
        cursor.execute("select count(name) from blackboxfile where PROFILESEND='0'")
        for result in cursor:
            final = int(result[0])
        con.close()

        with open("/home/pi/Python/sample.txt", 'wb') as openf:
            con = sqlite3.connect("/home/pi/blackbox.sql")
            cursor = con.cursor()
            cursor.execute("select PROFILE, RU from blackboxfile where PROFILESEND='0'")
            for result in cursor:
                if flag:
                    openf.write(str(result[1]) + "\n" + str(result[0])[0:len(str(result[0]))-1])
                    flag = False
                else:
                    openf.write("\n>>>\n" + str(result[1]) + "\n" + str(result[0])[0:len(str(result[0]))-1])
            con.close()
        header = self.make_header(0,3,final)
	files = {"payload": open("/home/pi/Python/sample.txt","rb")}
	
	resp = session.post(url, data=header, files=files)
        print "status code >> "+ str(resp.status_code)
        return resp.text
    def str_to_dict(self, input_str):
        return ast.literal_eval(input_str)
    def __init__(self):
        url = "http://A.B.C.D:8000"
	resp = self.send_hello(url)
	resp_dict = str_to_dict(resp)

	if resp_dict["code"] == 1:
            resp = send_vp(url)
	    resp = str_to_dict(resp)
	    if resp["code"] == 4:
                print "=== VP send finish ==="
                con = sqlite3.connect("/home/pi/blackbox.sql")
                cursor = con.cursor()
                cursor.execute("select name from blackboxfile where PROFILESEND='0'")
		for result in cursor:
                    cursor.execute("update blackboxfile set PROFILESEND='1' where NAME='" + str(result[0]) + "'")
                con.close()
	    else :
                print "recv ok error"
	else:
            print "resp error"