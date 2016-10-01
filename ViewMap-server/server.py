from flask import Flask
from flask import request

import MySQLdb
import time
import os
from time import localtime, strftime

app = Flask(__name__)

@app.route("/test", methods=['GET'])
def test():
    return "hello world!"

@app.route("/", methods=['GET','POST'])
def recv_header():
    
    action = read_header(request.form["type"], request.form["code"], request.form["misc"])

    result = ""

    if action == "00":
        # check storage func implement not yet
        vp_num = check_storage(uni_to_int(request.form["misc"]))
        result = vp_ok(vp_num)
        
        return result

    elif action == "03":
        vps = request.files["payload"].read()
        save_vps(vps)
        result = vp_data_ok()

        return result

    elif action == "15":
        lastseen = request.form["misc"]
        result = send_vrl(lastseen)

        return result

    elif action == "20":
        #check storage with video size
        video_size = request.form["misc"]

        vp = request.files["payload"].read()

        result = check_vp(vp)

        return result
    
    elif action == "23":
        # identify video and vp same
        try:
            save_video(request.files["payload"], request.form["misc"])
            result = video_data_ok()

            return result

        except:
            print "save video error!"

            return result
    elif action == "20":
        # identify profile
        result = "video_hello implement not yet"

        return result

    else:
        result = "No result"
        return result

    return "No Action"

def read_header(dtype, code, misc):
    if dtype == "0":
        if code == "0":
            return "00"
        elif code == "3":
            return "03"
        else:
            return "header error1"
    elif dtype == "1":
        if code == "5":
            return "15"
        else:
            return "header error2"

    elif dtype == "2":
        if code == "0":
            return "20"
        elif code == "3":
            return "23"
        else:
            return "header error3"
    else:
        return "header error4"
        
    return "other function"

def save_video(video_file, cookie):
    # how to write new files
    # make name with vp?
    save_file = open("video/"+cookie+".mp4", "wb")
    for b in video_file:
        save_file.write(b)
    save_file.close()

def save_vps(vps_file):
    try:
        db = MySQLdb.connect("localhost", "root", "322network", "viewmap")
        cursor = db.cursor()
    
        try:
            cursor.execute("insert into raw_data (data, isManage) values (%s, %s)", (vps_file, False))
            db.commit()
            print "===raw data insert done==="
        except:
            db.rollback()
            print "===insert raw data part roll back!==="
        db.close()
    except:
        print "===db connect error!==="

def vp_ok(vp_num):
    return make_response(0, 1, vp_num)

def vp_data_ok():
    return make_response(0, 4, 0)

def video_data_ok():
    return make_response(2, 4, 0)

def check_storage(file_size):
    return file_size

def make_response(dtype, code, misc):
    resp = {"type":dtype, "code":code, "misc":misc}
    return str(resp)

def make_payload_response(dtype, code, misc, payload):
    resp = {"type":dtype, "code":code, "misc":misc, "payload":payload}
    return str(resp)

def uni_to_int(input_uni):
    return int(unicode(input_uni))
    
def send_vrl(lastseen):
    db = MySQLdb.connect("localhost","root","322network","viewmap")
    cursor = db.cursor()
    
    try:
        cursor.execute("select ru,cookie from vr_list where time <(%s)",(lastseen))
        data = cursor.fetchall()

        ru_list = list()

        for each in data:
            ru_list.append(each[0])
            
        str_ru_list = ">>>".join(ru_list)
        
        data_dict = {"rus" : str_ru_list}

        return make_payload_response(1, 6, len(ru_list), data_dict)

    except:
        db.rollback()
        print "error in send vrl!"

def check_vp(vp):
    vp_details = vp.split("\n")
    ru = vp_details[0]

    server_hash_set = get_hash_set(ru)

    bloom_filter = vp_details[1]

    hash_set = list()

    check_flag = False
    
    for vd in vp_details[2:]:
        each_hash = vd.split(":")[4]
        hash_set.append(each_hash)

    for each_hash in server_hash_set:
        hash_list = each_hash.split(">>>")
        check_flag = is_same_hash(hash_list, hash_set)

    if check_flag == True:
        ###################################
        # implement func find cookie by ru#
        ##################################
        return make_response(2, 1, 123456)
    else:
        return make_response(2, 2, 0)

def is_same_hash(server_hash, target_hash):
    is_same = False
    
    cnt = 0
    for each in server_hash:
        if each == target_hash[cnt]:
            is_same = True
            cnt = cnt + 1
        else:
            is_same = False
            break

    return is_same

def get_hash_set(ru):
    db = MySQLdb.connect("localhost","root","322network","viewmap")
    cursor = db.cursor()
    
    result = list()

    try:
        cursor.execute("select max(ru_index) from vm_profile where ru = %s", (ru))
        data = cursor.fetchall()
        ru_index = data[0][0]

    except Exception, e:
        db.rollback()
        print "get_vp func select err!"
        raise e

    if ru_index == 1:
        try:
            cursor.execute("select hash from vm_profile where ru = %s order by id asc",(ru))

        except Exception, e:
            db.rollback()
            print "select hash by ru 1 err!"
            raise e
            
        hash_raw = cursor.fetchall()

        hash_set = list()

        for each in hash_raw:
            hash_set.append(each[0])

        hash_set_str = ">>>".join(hash_set)

        result.append(hash_set_str)
            
        return result

    else:
        index = 1

        for each in range(ru_index):
            try:
                cursor.execute("select hash from vm_profile where ru = %s and ru_index = %s order by id asc",(ru, index))

            except Exception, e:
                db.rollback()
                print "select hash by ru 2 err"
                raise e
            hash_raw = cursor.fetchall()
            hash_set = list()
                
            for each in hash_raw:
                hash_set.append(each[0])
            
            hash_set_str = ">>>".join(hash_set)
            
            result.append(hash_set_str)
            index = index + 1
        
        return result

def make_log(message):
    f = open("viewmap_log/viewmap_server_log.txt", "a")
    full_message = strftime("%Y-%m-%d %H:%M:%S", localtime()) + " " + message + "\n"
    f.write(full_message)
    f.close()         

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=8000)
