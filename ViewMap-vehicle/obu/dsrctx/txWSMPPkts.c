int txWSMPPkts(int pid) {
	int ret = 0 , count = 0;
	uint64_t *lat,*lng,*alt,*spd;
	uint64_t *at,*ti,*pt,*ex,*ey,*ev;
	uint32_t *dte,*fx,*nst;
	int status=0;
	char ch ='1';

	signal(SIGINT,(void *)sig_int);
	signal(SIGTERM,(void *)sig_term);

	printf("Message length is %d\n\n", g_packetLength);
	int i;
	packets = 0;

	float dat_rate;
	switch (g_datarate) {
		case 0:	
			dat_rate = 0.0;
		break;

		case 1:	
			dat_rate = 3.0;
		break;
			
		case 2:	
			dat_rate = 4.5;
		break;
			
		case 3:	
			dat_rate = 6.0;
		break;
			
		case 4:	
			dat_rate = 9.0;
		break;
			
		case 5:	
			dat_rate = 12.0;
		break;
			
		case 6:	
			dat_rate = 18.0;
		break;
			
		case 7:	
			dat_rate = 24.0;
		break;
			
		case 8:	
			dat_rate = 27.0;
		break;
			
		case 9:	
			dat_rate = 36.0;
		break;
			
		case 10:	
			dat_rate = 48.0;
		break;
			
		case 11:	
			dat_rate = 54.0;
		break;
		
		default:
		break;
	}
	time_t tm_time;
	struct tm *st_time;
	char tm_buf[100];
	char fm_buf[100];
	memset(tm_buf, '\0', 100);
	memset(fm_buf, '\0', 100);

	time(&tm_time);
	st_time = localtime(&tm_time);

	if (st_time->tm_hour>14) {
		sprintf(tm_buf, "%04d%02d%02d%02d%02d", st_time->tm_year+1900, st_time->tm_mon+1, st_time->tm_mday+1, st_time->tm_hour-15, st_time->tm_min);
	} else {
		sprintf(tm_buf, "%04d%02d%02d%02d%02d", st_time->tm_year+1900, st_time->tm_mon+1, st_time->tm_mday, st_time->tm_hour+9, st_time->tm_min);
	}
		sprintf(fm_buf, "/var/tx_%s.csv", tm_buf);
	
	log_fd = fopen(fm_buf, "w");
	fprintf(log_fd, "Scenario#,Sequence#,Time,Latitude,Longitude,Altitude,Speed,TXPower\n");

	for (i=0; ; i++) {
		time(&tm_time);
		st_time = localtime(&tm_time);
		char tx_time[6];
		char tx_datetime[14];
		if (st_time->tm_hour>14) {
			sprintf(tx_time, "%02d%02d%02d", st_time->tm_hour-15, st_time->tm_min, st_time->tm_sec);
		} else {
			sprintf(tx_time, "%02d%02d%02d", st_time->tm_hour+9, st_time->tm_min, st_time->tm_sec);
		}
        if (st_time->tm_hour>14) {
			 sprintf(tx_datetime, "%04d%02d%02d%02d%02d%02d", st_time->tm_year+1900, st_time->tm_mon+1, st_time->tm_mday+1, st_time->tm_hour-15, st_time->tm_min, st_time->tm_sec);
        } else {
			sprintf(tx_datetime, "%04d%02d%02d%02d%02d%02d", st_time->tm_year+1900, st_time->tm_mon+1, st_time->tm_mday, st_time->tm_hour+9, st_time->tm_min, st_time->tm_sec);
        }

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
			sleep(1);
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
			sleep(2);
			continue;
		}
		struct	timeval	snd_time;
		gettimeofday(&snd_time,NULL);
		WsmTEST_75B	wsmT75B;
		memset(&wsmreq.data, '\0', sizeof(WsmTEST_75B));
		memset(&wsmreq.data, '\0', sizeof(WSMData));
		wsmreq.data.length = 1100; 
		wsmT75B.scenNum = g_scenNum;
		wsmT75B.seqNo = (uint16_t)i+1;
		wsmT75B.nPacket = g_nPackets;
		wsmT75B.latitude = wsmgps.latitude;
		wsmT75B.longitude = wsmgps.longitude;
		wsmT75B.altitude = wsmgps.altitude;
		wsmT75B.speed = (float)wsmgps.speed*3.6;
		strncpy(wsmT75B.txtime, tx_time, 6);
		wsmT75B.usecTime = (double)(snd_time.tv_sec)+(double)(snd_time.tv_usec)/1000000.0;
		strncpy(wsmT75B.data, "ABCDEFGHIJKLMNOPQRSTUVWXY", 25);
		strncpy(wsmT75B.txmsg, buffer, strlen(buffer));
		memcpy(wsmreq.data.contents, &wsmT75B, sizeof(wsmT75B));
		
		printf("PID = %d", pid);
		ret = txWSMPacket(pid, &wsmreq);
		
		if (ret < 0) { 
			drops++;
		} else {
			packets++;
			count++;
			printf("#%d time:%s lat:%lf long:%lf alt:%lf spd:%.3fkm/h\nmsg:%s\n", i+1, tx_time, wsmgps.latitude, wsmgps.longitude, wsmgps.altitude, (float)wsmgps.speed*3.6, buffer);
			sprintf(dateBuffer, "%lf,%lf,%s", wsmgps.latitude, wsmgps.longitude, tx_datetime);
			fprintf(log_fd, "%d,%d,%s,%lf,%lf,%lf,%.3f,%d\n", g_scenNum, i+1, tx_time, wsmgps.latitude, wsmgps.longitude, wsmgps.altitude, (float)wsmgps.speed*3.6,g_txpower);
		}

		usleep(g_period);
	}

	printf("\n Transmitted =  %d dropped = %llu\n",count,drops); 
	fclose(log_fd);
	return drops; 
}