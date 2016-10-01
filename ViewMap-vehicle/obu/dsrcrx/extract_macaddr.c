
int extract_macaddr(u_int8_t *mac, char *str) {
	int maclen = IEEE80211_ADDR_LEN;
	int len = strlen(str);
	int i = 0, j = 0, octet = 0, digits = 0, ld = 0, rd = 0;
	char num[2];
	u_int8_t tempmac[maclen];
	memset(tempmac, 0, maclen);
	memset(mac, 0, maclen);
	if ((len < (2 * maclen - 1)) || (len > (3 * maclen - 1))) return -1;
     	while (i < len) {
		j = i;
		while (str[i] != ':' && (i < len)) {
			i++;
		}
		if (i > len) exit(0);
		digits = i - j;
		if ((digits > 2) ||  (digits < 1) || (octet >= maclen)) {
			return -1;
		}
		num[1] = tolower(str[i - 1]);
		num[0] = (digits == 2)? tolower(str[i - 2]) : '0';
		if (isxdigit(num[0]) && isxdigit(num[1])) {
			ld  =  (isalpha(num[0]))? 10 + num[0] - 'a' : num[0] - '0';
			rd  =  (isalpha(num[1]))? 10 + num[1] - 'a' : num[1] - '0';
			tempmac[octet++] =  ld * 16 + rd ;
		} else {
			return -1;
		}
		i++;
	}
	if (octet > maclen) return -1;
	memcpy(mac, tempmac, maclen);
	return 0;
}