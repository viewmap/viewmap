void set_args(void *data ,void *argname, int datatype) {
	u_int8_t string[1000];
	struct arguments *argument1;
	argument1 = (struct arguments *)argname;
	switch(datatype) {
		case ADDR_MAC:
			memcpy(string, argument1->macaddr, 17);
			string[17] = '\0';
			if(extract_macaddr(data, string) < 0) {
			}
			break;
		case UINT8:
			memcpy(data, (char *)argname, sizeof(u_int8_t));
			break;
	}
}