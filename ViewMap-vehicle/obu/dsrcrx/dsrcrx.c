int main(int arg, char *argv[]) {
	WSMIndication rxpkt;
	int  ret = 0;
        struct arguments arg1;
        
        if (arg < 5) {
            return 0;
        } 
	pid = getpid();
	memset(&entry, 0 , sizeof(WMEApplicationRequest));
	entry.psid = 5;
        if ((atoi(argv[1]) > USER_REQ_SCH_ACCESS_NONE) || (atoi(argv[1]) < USER_REQ_SCH_ACCESS_AUTO)) {
            printf("User request type invalid: setting default to auto\n");
            entry.userreqtype = USER_REQ_SCH_ACCESS_AUTO; 
      
        } else {
	    entry.userreqtype = atoi(argv[1]);
        }
        if (entry.userreqtype == USER_REQ_SCH_ACCESS_AUTO_UNCONDITIONAL) {
            if (arg < 5) {
                printf("channel needed for unconditional access\n");
                return 0;
            } else {
                entry.channel = atoi(argv[5]);
            }
        }
       
        entry.schaccess  = atoi(argv[2]);
        entry.schextaccess = atoi(argv[3]);
	g_ipaddr = "127.0.0.1";
	g_period = atoi(argv[4]);
        if (arg > 5) {
            strncpy(arg1.macaddr, argv[5], 17);
            set_args(entry.macaddr, &arg1, ADDR_MAC);
        }
	printf("Invoking driver \n");

	if (invokeWAVEDevice(WAVEDEVICE_LOCAL, 0) < 0) {
		printf("Open Failed. Quitting\n");
		exit(-1);
	}

	printf("Registering User %d\n", entry.psid);
	checkregister = registerUser(pid, &entry);
	if (checkregister < 0) {
		checkremove = !removeUser(pid, &entry);
		printf("Removing user if already present  %d\n", checkremove);
		checkregister = registerUser(pid, &entry);
		printf("USER Registered %d with PSID =%u \n", checkregister, entry.psid );
	} else {
		printf("USER Registered %d with PSID =%u \n", checkregister, entry.psid );
	}
	uint64_t *lat,*lng,*alt,*spd;
	uint64_t *at,*ti,*pt,*ex,*ey,*ev;
	uint32_t *dte,*fx,*nst;
	int status=0;
	char ch ='1';
	double distance = 0.0;
	int sockfd;
	int portno, n;
	struct sockaddr_in serv_addr;
    	struct hostent *server;
	char buffer[1100];
	portno = 9912;
	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if (sockfd < 0) error("ERROR opening socket");
	server = gethostbyname("A.B.C.D");

	signal(SIGINT,(void *)sig_int);
	signal(SIGTERM,(void *)sig_term);

	time_t tm_time;
	struct tm *st_time;
	char tm_buf[100];
	char fm_buf[100];
	memset(fm_buf, '\0', 100);

	time(&tm_time);
	st_time = localtime(&tm_time);

	if (st_time->tm_hour>14) {
		sprintf(tm_buf, "%04d%02d%02d%02d%02d", st_time->tm_year+1900, st_time->tm_mon+1, st_time->tm_mday+1, st_time->tm_hour-15, st_time->tm_min);
	} else {
		sprintf(tm_buf, "%04d%02d%02d%02d%02d", st_time->tm_year+1900, st_time->tm_mon+1, st_time->tm_mday, st_time->tm_hour+9, st_time->tm_min);
	}
	sprintf(fm_buf, "/var/rx_%s.csv", tm_buf);
	log_fd = fopen(fm_buf, "w");

	int seq_no = 1;
	
	fprintf(log_fd, "Scenario#,Sequence#,Time,Latitude,Longitude,Altitude,Speed,Latitude(Vs.),Longitude(Vs.),Altitude(Vs.),Speed(Vs.),RSSI,Channel,Delay,Distance\n");
	
	while (1) {
		gpssockfd = gpsc_connect1();
		
		if (gpssockfd < 0) {
			printf("gpstime: gpsc is not running...%d\n",gpssockfd);
			exit(0);
		}

		memset(&wsmgps,0,sizeof(GPSData)); 
		status = write(gpssockfd,&ch,1);

		if (status < 1) {
			syslog(LOG_INFO,"gpstime: write error %d (err %d)!!\n", status, errno);
			exit(0);
		}

		status = read(gpssockfd,&wsmgps,sizeof(GPSData));
		
		if (status < sizeof(GPSData)) {
			syslog(LOG_INFO,"gpstime: read error %d (err %d) exp %d!!\n", status, errno, sizeof(GPSData));
			gpsc_close_sock();
			gpssockfd=-1;
			sleep(2);
		}

		if (!BIGENDIAN) {
			at=((uint64_t*)(&wsmgps.actual_time));
			*at=swap64(*at);
			ti=((uint64_t*)(&wsmgps.time));
			*ti=swap64(*ti);
			pt=((uint64_t*)(&wsmgps.local_tod));
			*pt=swap64(*pt);
			lat=((uint64_t*)(&wsmgps.latitude));
			*lat=swap64(*lat);
			lng=((uint64_t*)(&wsmgps.longitude));
			*lng=swap64(*lng);
			alt=((uint64_t*)(&wsmgps.altitude));
			*alt=swap64(*alt);
			spd=((uint64_t*)(&wsmgps.speed));
			*spd=swap64(*spd);
			dte=(uint32_t*)(&wsmgps.date);
			*dte=swap32_(*dte);
			ex=((uint64_t*)(&wsmgps.epx));
			*ex=swap64(*ex);
			ey=((uint64_t*)(&wsmgps.epy));
			*ey=swap64(*ey);
			ev=(uint64_t*)(&wsmgps.epv);
			*ev=swap64(*ev);
		}

		if (wsmgps.actual_time == GPS_INVALID_DATA || wsmgps.actual_time == 0.0) {
			printf("gpstime:wrg at=%lf fix=%d\n",wsmgps.actual_time,wsmgps.fix);
			gpsc_close_sock();
			gpssockfd=-1;
			sleep(4);
			continue;
		}
		ret = rxWSMPacket(pid, &rxpkt);

		if (ret > 0) {
			time(&tm_time);
			struct	timeval	recv_time;
			gettimeofday(&recv_time,NULL);
			st_time = localtime(&recv_time.tv_sec);
			char rx_time[11];
			if (st_time->tm_hour>14) {
				sprintf(rx_time, "%02d:%02d:%02d.%02ld", st_time->tm_hour-15, st_time->tm_min, st_time->tm_sec, recv_time.tv_usec/10000);
			}
			else {
				sprintf(rx_time, "%02d:%02d:%02d.%02ld", st_time->tm_hour+9, st_time->tm_min, st_time->tm_sec, recv_time.tv_usec/10000);
			}

			WsmTEST_STD *p_stdWSM = NULL;
			char temp_buf[rxpkt.data.length];
			memcpy(temp_buf, rxpkt.data.contents, rxpkt.data.length);
			p_stdWSM = (WsmTEST_STD*)temp_buf;
			WsmTEST_STD tempWSMstd;
			memset(&tempWSMstd, '\0', sizeof(WsmTEST_STD));
			memcpy(&tempWSMstd, p_stdWSM, sizeof(WsmTEST_STD));
			char datamsg[50];
			char txmsgmsg[1025];
			memset(&datamsg, '\0', sizeof(datamsg));
			memset(&txmsgmsg, '\0', sizeof(txmsgmsg));
			strncpy(txmsgmsg, tempWSMstd.txmsg, 1025);
			strncpy(datamsg, tempWSMstd.data, 25);
			
			if (!strcmp("ABCDEFGHIJKLMNOPQRSTUVWXY", datamsg)) {
				if (count == 0) {
					g_seqNo1 = tempWSMstd.seqNo;
					time(&g_time1);
				}			
				
				double distance = get_distance(wsmgps.latitude, wsmgps.longitude, tempWSMstd.latitude, tempWSMstd.longitude);
				fprintf(log_fd, "%d,%d,%s,%lf,%lf,%lf,%.3f,%lf,%lf,%lf,%.3f,%d,%d,%.7lf,%.3lf\n", tempWSMstd.scenNum, tempWSMstd.seqNo, rx_time, wsmgps.latitude, wsmgps.longitude, wsmgps.altitude, (float)wsmgps.speed*3.6, tempWSMstd.latitude, tempWSMstd.longitude, tempWSMstd.altitude, tempWSMstd.speed, rxpkt.rssi, entry.channel, (tempWSMstd.usecTime-(double)(recv_time.tv_sec)-(double)(recv_time.tv_usec)/1000000.0), distance);
				g_seqNo2 = tempWSMstd.seqNo;
				count++;
				seq_no++;
				
				sockfd = socket(AF_INET, SOCK_STREAM, 0);
				bzero((char *) &serv_addr, sizeof(serv_addr));
				serv_addr.sin_family = AF_INET;
				bcopy((char *)server->h_addr, (char *)&serv_addr.sin_addr.s_addr, server->h_length);
				serv_addr.sin_port = htons(portno);
				connect(sockfd,(struct sockaddr *)&serv_addr,sizeof(serv_addr));
				bzero(buffer,1100);
				sprintf(buffer, "%d|%s|%.3f|%.3f|%d|%d|%.7lf|%.3lf|%s\n", tempWSMstd.seqNo, rx_time, (float)wsmgps.speed*3.6, tempWSMstd.speed, rxpkt.rssi, entry.channel, (tempWSMstd.usecTime-(double)(recv_time.tv_sec)-(double)(recv_time.tv_usec)/1000000.0), distance, txmsgmsg);
				n = write(sockfd, buffer, strlen(buffer));
				bzero(buffer,1100);
				n = read(sockfd,buffer,1100);
				printf("RX #%d dis:%.3lf m spd:%.3f km/h spd(vs):%.3f km/h rssi:%d msglen:%d\n",tempWSMstd.seqNo, distance, (float)wsmgps.speed*3.6, tempWSMstd.speed, rxpkt.rssi, strlen(txmsgmsg));
				close(sockfd);
			} else {
				blank++;
			}
		} else {
			blank++;
		}

	}
	fclose(log_fd);
}