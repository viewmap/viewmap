void sig_int(void) {
	time(&g_time2);
	double time_total = (difftime(g_time2, g_time1) + (g_seqNo1 * g_period/1000000)) * 1000000/g_period;
	float total = (float)(g_seqNo2 - g_seqNo1 + 1);
	if (log_fd != NULL) {
		fprintf(log_fd, "\n\nPackets received = %llu/%.0lf = %.2lf%%\n", count, time_total, (count/time_total * 100));
	}
	fclose(log_fd);
	removeUser(pid, &entry);
	signal(SIGINT,SIG_DFL);
	printf("\n\nPackets received = %llu/%.0lf = %.2lf%%\n", count, time_total, (count/time_total * 100));
	printf("Blank Poll = %llu\n", blank); 
	printf("testrx killed by kill signal\n");
	exit(0);
}