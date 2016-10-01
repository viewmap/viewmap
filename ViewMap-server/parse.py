# -*- coding:utf-8 -*-
import MySQLdb
import sys
import time

db = MySQLdb.connect("localhost", "root","322network", "viewmap")
cursor = db.cursor()

while(True):
    time.sleep(60)
    try:
        print "=== start load and save ===" 

        vmp_mash = list()
        vmps = list()
        raw_profile_ids = list()

        try:           
            cursor.execute("select id, data from raw_data where isManage=false")
            recs = cursor.fetchall()
        
        except Exception, e:
            print "=== select query err! ==="
            raise e

        for rec in recs:
            raw_profile_ids.append(rec[0])
            vmp_mash.append(rec[1])

        for idx, mash in enumerate(vmp_mash):
            print "pass?"
            mashs = mash.split('>>>')
            for each in mashs:
                if each != '':
                    vmps.append(each)
        
            for vmp in vmps:
                vmp_detail = vmp.split('\n')
                vmp_detail = [x for x in vmp_detail if x != '']
                ru = vmp_detail[0]
               
                try:
                    cursor.execute("select distinct max(ru_index) from vm_profile where ru = %s", (ru))
                    ru_index_list = cursor.fetchall()

                    if ru_index_list[0][0] is None:
                        ru_index = 1
                    else:
                        ru_index = ru_index_list[0][0] + 1

                except Exception, e:
                    db.rollback()
                    print "select max ru_index err!"
                    raise e

                bloom_filter = vmp_detail[1]

                for vmp in vmp_detail[2:]:
                    profile = vmp.split(':')
                    date = profile[0]
                    latitude = profile[1].split(',')[0]
                    longitude = profile[1].split(',')[1]
                    file_size = profile[3]
                    hash_data = profile[4]

                    try:
                        cursor.execute("insert into vm_profile (ru, ru_index, bloom_filter, time, latitude, longitude, file_size, hash) values (%s,%s,%s,%s,%s,%s,%s,%s)",(ru, ru_index, bloom_filter, date, latitude,longitude, file_size, hash_data))
                        db.commit()
                        print "insert commit!"

                    except Exception, e:
                        db.rollback()
                        print "insert err! rollback!"
                        raise e
    	    try:
    	    	cursor.execute("update raw_data set isManage = true where isManage = false and id = %s",(raw_profile_ids[idx]))
    	    	db.commit()
    	    	print "update commit!"

    	    except Exception, e:
    	    	print "=== is Manage update err! ==="
    	    	raise e
    	   	
            print "=== load,save end ==="

    except Exception, e:
        db.rollback()
        print "err! rollback!"
        raise e

db.close()