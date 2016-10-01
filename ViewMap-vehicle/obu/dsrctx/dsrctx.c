int main (int argc, char *argv[]) {
	int result ;
	pid = getpid();
        if (argc < 9) {
		return 0; 
        }

	int portno;
	int n;
	bzero(buffer, 1025);
	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if (sockfd < 0) 
		error("ERROR opening socket");
	
	bzero((char *) &serv_addr, sizeof(serv_addr));
	portno = 9911;
	serv_addr.sin_family = AF_INET;
	serv_addr.sin_addr.s_addr = INADDR_ANY;
	serv_addr.sin_port = htons(portno);
	
	if (bind(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr)) < 0)
		error("ERROR on binding");
	
	listen(sockfd,5);
	clilen = sizeof(cli_addr);

        taarg.channel = atoi(argv[2]); 
        taarg.channelinterval = atoi(argv[3]); 

	g_txpower = atoi(argv[6]);
	g_datarate = atoi(argv[7]);
	g_ipaddr = "127.0.0.1";
	g_scenNum = 3;
	g_period = atoi(argv[8]);

	printf("Filling Provider Service Table entry %d\n",buildPSTEntry(argv));
	printf("Building a WSM Request Packet %d\n", buildWSMRequestPacket());
	printf("Building a WME Application  Request %d\n",buildWMEApplicationRequest());
        printf("Builing TA request %d\n", buildWMETARequest());
	
	if (invokeWAVEDriver(0) < 0) {
		printf( "Opening Failed.\n ");
		exit(-1);
	} else {
		printf("Driver invoked\n");

	}

	registerWMENotifIndication(receiveWME_NotifIndication);
	registerWRSSIndication(receiveWRSS_Indication);
	registertsfIndication(receiveTsfTimerIndication);

	printf("Registering provider\n ");
	if (registerProvider( pid, &entry ) < 0) {
		checkremove = removeProvider(pid, &entry);
		printf("remove returns: %d\n", checkremove);
		sleep(5);
		checkregister = registerProvider(pid, &entry);
		printf("register returns: %d", checkregister);
		if (checkregister < 0) {
			printf("Register Provider failed\n");
		} else {
			printf("provider registered with PSID = %u\n",entry.psid );
		}
	} else {	
		printf("provider registered with PSID = %u\n",entry.psid );
	}
        printf("starting TA\n");
        if (transmitTA(&tareq) < 0)  {
            printf("send TA failed\n "); 
        } else {
            printf("send TA successful\n") ;
        }
	strcpy(buffer, "00000000000000000000000000000000");
	pthread_create(&threads, NULL, &thread_main, (void *)0);

	result =txWSMPPkts(pid);
	if ( result == 0 ) {
		printf("All Packets transmitted\n");
	} else {
		printf("%d Packets dropped\n",result);
	}
	return 1;
}