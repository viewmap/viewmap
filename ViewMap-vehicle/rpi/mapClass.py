class mapClass:
    def __init__(self, start, end, fileName):
        self.listReturn = []
        self.listBytePlusHash = []
        
        for startx in start:
            bytePlusHash = ""
            listStep = []
            listGps = []
            listFakeGps = ['0' for _ in range(60)]
            listFakeGps[0] = startx
            listFakeGps[59] = end
            startSplit = listFakeGps[0].split(',')
            endSplit = listFakeGps[59].split(',')
            for x in self.get_route(startSplit[1], startSplit[0], endSplit[1], endSplit[0]):
                listGps.append('{:f}'.format(x[1]) + "," + '{:f}'.format(x[0]))
            
            if (len(listGps)-1)>59:
                for i in range(59):
                    listFakeGps[i] = listGps[i]
                listFakeGps[59] = listGps[len(listGps)-1]
            else:
                listFakeGps[0] = listGps[0]
                listStep.append(0)
        
                k = 60/(len(listGps)-1)
                for i in range(1, (len(listGps)-1)):
                    listFakeGps[i*k-1] = listGps[i]
                    listStep.append(i*k-1)

                listFakeGps[59] = listGps[len(listGps)-1]
                listStep.append(59)
                
                for i in range(len(listStep)-1):
                    for x in range(listStep[i]+1, listStep[i+1]):
                        listFakeGpsSetSplit = listFakeGps[int(listStep[i])].split(',')
                        listFakeGpsEndSplit = listFakeGps[int(listStep[i+1])].split(',')
                        listFakeGps[x] = ('{:f}'.format(float(listFakeGpsSetSplit[0]) + (float(listFakeGpsEndSplit[0])-float(listFakeGpsSetSplit[0]))
                                             * float(x-listStep[i])/float(listStep[i+1]-listStep[i]))
                                        + ','
                                        + '{:f}'.format(float(listFakeGpsSetSplit[1]) + (float(listFakeGpsEndSplit[1])-float(listFakeGpsSetSplit[1]))
                                        * float(x-listStep[i])/float(listStep[i+1]-listStep[i])))
            listTest = ['0' for _ in range(60)]
            for x in range(60):
                if x == 59:
                    listTest[x] = (fileName[0:10] + '{0:02d}'.format(int(fileName[10:12])+1) + '00'
                        + ":" + listFakeGps[x]
                        + ":" + '{0:08d}'.format(random.randint(0,99999999))
                        + ":" + hashlib.md5(str(random.randint(0,99999999))).hexdigest()
                        + ":" + hashlib.md5(str(random.randint(0,99999999))).hexdigest())
                    bytePlusHash += (fileName[0:10] + '{0:02d}'.format(int(fileName[10:12])+1) + '00'
                                            + ":" + listFakeGps[x]
                                            + ":" + '{0:08d}'.format(random.randint(0,99999999))
                                            + ":" + hashlib.md5(str(random.randint(0,99999999))).hexdigest()
                                            + ":" + hashlib.md5(str(random.randint(0,99999999))).hexdigest() + "\n")
                else:
                    listTest[x] = (fileName[0:12] + '{0:02d}'.format(x+1)
                        + ":" + listFakeGps[x]
                        + ":" + '{0:08d}'.format(random.randint(0,99999999))
                        + ":" + hashlib.md5(str(random.randint(0,99999999))).hexdigest()
                        + ":" + hashlib.md5(str(random.randint(0,99999999))).hexdigest())
                    bytePlusHash += (fileName[0:12] + '{0:02d}'.format(x+1)
                                            + ":" + listFakeGps[x]
                                            + ":" + '{0:08d}'.format(random.randint(0,99999999))
                                            + ":" + hashlib.md5(str(random.randint(0,99999999))).hexdigest()
                                            + ":" + hashlib.md5(str(random.randint(0,99999999))).hexdigest() + "\n")
            self.listReturn.append(listTest[0])
            self.listReturn.append(listTest[59])
            self.listBytePlusHash.append(bytePlusHash)

    def get_listReturn(self):
        return self.listReturn

    def get_bytePlusHash(self):
        return self.listBytePlusHash

    def get_route(startX, startY, endX, endY):
        result = list()
        DEV_ID = 'ABCD'
        APP_KEY = 'ABCDEFG'
	    url = 'A.B.C.D'

	    headers = {
	        'x-skpop-userId':DEV_ID,
	        'Accept-Language':'abc',
	        'Date':'abcde',
	        'Content-Type':'abcdef',
	        'Accept':'abcdefg',
	    'access_token':"",
	    'appKey':APP_KEY,
	    }

	    payload = {
	        'version':'1',
	        'callback':"",
	        'endX':endX,
	        'endY':endY,
	        'startX':startX,
	        'startY':startY,
    	    'reqCoordType':'WGS84GEO',
	        'resCoordType':'WGS84GEO',
	    } 

	    res = requests.post(url, headers = headers,data = payload)

	    for mesh in res.json()['features']:
		    if type(mesh['geometry']['coordinates'][0]) is float:
			    result.append(mesh['geometry']['coordinates'])
		    else:
			    if len(mesh['geometry']['coordinates']) <= 2:
				    pass
			    else:
				    for each_pair in mesh['geometry']['coordinates'][1:-1]:
					    result.append(each_pair)

	    return result