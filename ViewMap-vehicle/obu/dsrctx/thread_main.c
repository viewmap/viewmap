void *thread_main(void *arg) {
	int portno;
    	char readBuffer[1025];
	while (1) {
		newsockfd = accept(sockfd, (struct sockaddr *) &cli_addr, &clilen);
		if (newsockfd < 0) {
			error("ERROR on accept");
		}
		if (read(newsockfd, readBuffer, 1024) < 0) {
			error("ERROR reading from socket");
		}
        	if (NULL != strstr(readBuffer, "ABCDEFGHIJKLMNOPQRSTUVWXYZ")) {
			if (write(newsockfd, dateBuffer, strlen(dateBuffer)) < 0) {
                                error("ERROR writing to socket");
                        }
		} else {
			bzero(buffer, 1025);
			strncpy(buffer, readBuffer, strlen(readBuffer));
			if (write(newsockfd,"Server got Client`s message",27) < 0) {
				error("ERROR writing to socket");
			}
		}
		close(newsockfd);
	}
}