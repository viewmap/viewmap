double get_distance(double P1_latitude, double P1_longitude, double P2_latitude, double P2_longitude) {
	double c22, c23, c26, c27, c29, c31, c33, c38, c40, c41, c43, c45;
	
	if ((P1_latitude == P2_latitude) && (P1_longitude == P2_longitude)) {
		return 0;
	}
	
	c22 = sin(atan((1 - 0.0033528107) * tan(P1_latitude * PI / 180)));
	c23 = cos(atan((1 - 0.0033528107) * tan(P1_latitude * PI / 180)));
	c26 = sin(atan((1 - 0.0033528107) * tan(P2_latitude * PI / 180)));
	c27 = cos(atan((1 - 0.0033528107) * tan(P2_latitude * PI / 180)));
	c29 = (P2_longitude * PI / 180) - (P1_longitude * PI / 180);

	c31 = (c27 * sin(c29) * c27 * sin(c29)) + (c23 * c26 - c22 * c27 * cos(c29)) * (c23 * c26 - c22 * c27 * cos(c29));
	c33 = (c22 * c26) + (c23 * c27 * cos(c29));
	
	c38 = 0;
	if (c31 == 0) {
		c38 = 0;
	} else {
		c38 = c23 * c27 * sin(c29) / sqrt(c31);
	}
	
	c40 = 0;
	if ((cos(asin(c38)) * cos(asin(c38))) == 0) {
		c40 = 0;
	} else {
		c40 = c33 - 2 * c22 * c26 / (cos(asin(c38)) * cos(asin(c38)));
	}

	c41 = cos(asin(c38)) * cos(asin(c38)) * (6378137.000000000 * 6378137.000000000 - 6356752.314140910 * 6356752.314140910) / (6356752.314140910 * 6356752.314140910);
	c43 = 1 + c41 / 16384 * (4096 + c41 * (-768 + c41 * (320 - 175 * c41)));
	c45 = c41 / 1024 * (256 + c41 * (-128 + c41 * (74 - 47 * c41)));

	return (6356752.314140910 * c43 * (atan(sqrt(c31) / c33) - (c45 * sqrt(c31) * (c40 + c45 / 4 * (c33 * (-1 + 2 * c40 * c40) - c45 / 6 * c40 * (-3 + 4 * c31) * (-3 + 4 * c40 * c40))))));
}