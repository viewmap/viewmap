import sys
import math
import random

#load trace file
#parse fake location
# f = open('LA_santa_monica_1km_1_600sec_45kmh_30nodes.out.result.txt', 'r')

f = open(sys.argv[1] + '.out.result.txt', 'r')
# f = open('../test/LA_santa_monica_1km_1_601sec_45kmh_50nodes'+ '.out.result.txt', 'r')

# FAKE_PICK_NUM = 1

nodeCount = int(f.readline())
endTime = int(f.readline())

info = []
fake_info = []

for each in range(endTime):
	info.append([])

for each in range(endTime / 60):
	fake_info.append([])

while True:
	line = f.readline()
	
	if not line:
		break
	
	split_line = line.split('\t')

	if int(split_line[0]) % 60 == 0:
		info[int(split_line[0])].append([int(split_line[1]), float(split_line[2]), float(split_line[3]), float(split_line[4]), float(split_line[5])])

		# list_count_per_loop = len(split_line[6:]) / 3
		
		list_count_per_loop = int(math.ceil((len(split_line[6:]) / 3) / 10.0))

		for i in range(list_count_per_loop):
				fake_info[(int(split_line[0]) / 60) - 1].append([])

		# if (len(split_line[6:]) / 3) == 0:
		# 	pass			
		# elif (len(split_line[6:]) / 3) <= FAKE_PICK_NUM:
		# 	for i in range(len(split_line[6:]) / 3):
		# 		fake_info[(int(split_line[0]) / 60) - 1].append([])
		# else:
		# 	list_count_per_loop = FAKE_PICK_NUM
		# 	for i in range(FAKE_PICK_NUM):
		# 		fake_info[(int(split_line[0]) / 60) - 1].append([])
		
		# for i in range(len(split_line[6:]) / 3):
		# 	fake_info[(int(split_line[0]) / 60) - 1].append([])

		selector = range(list_count_per_loop)
		random.shuffle(selector)

		for idx, random_num in enumerate(selector):
			list_idx = len(fake_info[(int(split_line[0]) / 60) - 1]) - list_count_per_loop + idx
			
			fake_info[(int(split_line[0]) / 60) - 1][list_idx].append(int(split_line[6 + random_num * 3 + 0]))
			fake_info[(int(split_line[0]) / 60) - 1][list_idx].append(float(split_line[2]))
			fake_info[(int(split_line[0]) / 60) - 1][list_idx].append(float(split_line[3]))
			fake_info[(int(split_line[0]) / 60) - 1][list_idx].append(float(split_line[6 + random_num * 3 + 1]))
			fake_info[(int(split_line[0]) / 60) - 1][list_idx].append(float(split_line[6 + random_num * 3 + 2]))

		# if (len(split_line[6:]) / 3) <= 3:
		# 	for idx, each in enumerate(split_line[6:]):
		# 		list_idx = len(fake_info[(int(split_line[0]) / 60) - 1]) - (len(split_line[6:]) / 3) + (idx / 3)

		# 		if idx % 3 == 0:
		# 			fake_info[(int(split_line[0]) / 60) - 1][list_idx].append(int(each))
		# 			fake_info[(int(split_line[0]) / 60) - 1][list_idx].append(float(split_line[2]))
		# 			fake_info[(int(split_line[0]) / 60) - 1][list_idx].append(float(split_line[3]))
		# 		else:
		# 			fake_info[(int(split_line[0]) / 60) - 1][list_idx].append(float(each))
		# else:
		# 	selector = range(len(split_line[6:]) / 3)
		# 	random.shuffle(selector)

		# 	for idx, random_num in enumerate(selector[:3]):
		# 		list_idx = len(fake_info[(int(split_line[0]) / 60) - 1])  + idx
		# 		print list_idx
		# 		fake_info[(int(split_line[0]) / 60) - 1][list_idx].append(int(split_line[6 + random_num * 3 + 0]))
		# 		fake_info[(int(split_line[0]) / 60) - 1][list_idx].append(float(split_line[2]))
		# 		fake_info[(int(split_line[0]) / 60) - 1][list_idx].append(float(split_line[3]))
		# 		fake_info[(int(split_line[0]) / 60) - 1][list_idx].append(float(split_line[6 + random_num * 3 + 1]))
		# 		fake_info[(int(split_line[0]) / 60) - 1][list_idx].append(float(split_line[6 + random_num * 3 + 2]))
	else:
		info[int(split_line[0])].append([int(split_line[1]), float(split_line[2]), float(split_line[3]), float(split_line[4]), float(split_line[5])])

f.close()

# make trace file with fake location

nodeNumberCounter = nodeCount
for interval_idx, each_interval in enumerate(fake_info):
	if interval_idx == 0:

		continue

	for each_node in each_interval:
		# print '=== Start Make Fake ==='
		# print 'fake node info >> ' + str(each_node)

		node_id = each_node[0]
		# print 'node id >> ' + str(node_id)

		des_x = each_node[1]
		des_y = each_node[2]
		# print 'des_x , des_y >>' + str(des_x) +' '+ str(des_y)

		fk_x = each_node[3]
		fk_y = each_node[4]

		first_loc_x = info[interval_idx * 60][node_id - 1][1]
		first_loc_y = info[interval_idx * 60][node_id - 1][2]

		prev_first_loc_x = info[interval_idx * 60 - 1][node_id - 1][1]
		prev_first_loc_y = info[interval_idx * 60 - 1][node_id - 1][2]		

		if (fk_x - first_loc_x) == 0 and (fk_y - first_loc_y) == 0:
			tmp_prev_x = fk_x
			tmp_prev_y = fk_y
			tmp_cur_x = 0.0
			tmp_cur_y = 0.0

			for each in info[interval_idx * 60 + 2:interval_idx * 60 + 31]:
				tmp_cur_x = each[node_id - 1][1]
				tmp_cur_y = each[node_id - 1][2]

				if (tmp_cur_x - tmp_prev_x) != 0 or (tmp_cur_y - tmp_prev_y) != 0:
					first_loc_x = tmp_prev_x
					first_loc_y = tmp_prev_y
					fk_x = tmp_cur_x
					fk_y = tmp_cur_y
					break

				else:
					tmp_prev_x = tmp_cur_x
					tmp_prev_y = tmp_cur_y

		fake_start_idx = (interval_idx * 60) + 1
		fake_mid_idx = (interval_idx * 60) + 30
		fake_end_idx = (interval_idx * 60) + 60

		cur_x = fk_x
		cur_y = fk_y

		prev_x = first_loc_x
		prev_y = first_loc_y

		dist_x = cur_x - prev_x
		dist_y = cur_y - prev_y

		# print 'src_x, src_y >>' + str(fk_x) +' '+ str(fk_y)
		# print 'first_loc_x, first_loc_y >>' + str(first_loc_x) +' '+ str(first_loc_y)

		for time, each_time in enumerate(info[fake_start_idx:fake_mid_idx+1]):
			# print '\n'
			# print ' time >> ' + str()
			each_time.append([])
			each_time[-1].append(nodeNumberCounter + 1)
			# print 'nodenumber >>' + str(nodeNumberCounter + 1)

			if time == 0:
				prev_x = cur_x
				prev_y = cur_y

				cur_x = first_loc_x + (first_loc_x - prev_first_loc_x) / 2
				cur_y = first_loc_y + (first_loc_y - prev_first_loc_y) / 2

				each_time[-1].append(cur_x)
				each_time[-1].append(cur_y)
				each_time[-1].append((first_loc_x - prev_first_loc_x) / 2)
				each_time[-1].append((first_loc_y - prev_first_loc_y) / 2)

				continue

			dist_x = cur_x - prev_x
			dist_y = cur_y - prev_y

			prev_x = cur_x
			prev_y = cur_y

			dist_half = math.sqrt((dist_x*dist_x) + (dist_y*dist_y)) / 2

			pred_x = cur_x + dist_x
			pred_y = cur_y + dist_y

			# print 'pred_x , pred_y >>' + str(pred_x) +' '+ str(pred_y)

			# print str(des_x) + ' compare ' + str(pred_x)
			# print str(des_y) + ' compare ' + str(pred_y)

			if des_x == pred_x and des_y == pred_y:
				each_time[-1].append(cur_x)
				each_time[-1].append(cur_y)
				each_time[-1].append(0.0)
				each_time[-1].append(0.0)

			else:
				cur_x = des_x - pred_x
				cur_y = des_y - pred_y

				dist_vec = math.sqrt((cur_x*cur_x) + (cur_y*cur_y))

				cur_x = cur_x * (dist_half / dist_vec)
				cur_y = cur_y * (dist_half / dist_vec)

				cur_x = cur_x + pred_x
				cur_y = cur_y + pred_y

				each_time[-1].append(cur_x)
				each_time[-1].append(cur_y)
				each_time[-1].append(dist_x)
				each_time[-1].append(dist_y)

			# print 'cur_x , cur_y >> ' + str(cur_x) + ' ' + str(cur_y)

		for loop_cnt, each_time in enumerate(info[fake_mid_idx+1:fake_end_idx+1]):
			each_time.append([])
			each_time[-1].append(nodeNumberCounter + 1)

			if loop_cnt == 28:
				cur_x = des_x + ((fk_x - des_x) / 60)
				cur_y = des_y + ((fk_y - des_y) / 60)
				
				dist_x = cur_x - prev_x
				dist_y = cur_y - prev_y
				
				prev_x = cur_x
				prev_y = cur_y

				each_time[-1].append(cur_x)
				each_time[-1].append(cur_y)
				each_time[-1].append(dist_x)
				each_time[-1].append(dist_y)

				continue

			if loop_cnt == 29:
				each_time[-1].append(des_x)
				each_time[-1].append(des_y)
				each_time[-1].append(des_x - prev_x)
				each_time[-1].append(des_y - prev_y)

				break

			vec_x = cur_x - prev_x
			vec_y = cur_y - prev_y

			dist_x = cur_x - prev_x
			dist_y = cur_y - prev_y

			prev_x = cur_x
			prev_y = cur_y

			# dist_half = math.sqrt((dist_x*dist_x) + (dist_y*dist_y)) / 2
			# dist_half = math.sqrt(math.pow(dist_x, 2) + math.pow(dist_y, 2)) / 2

			cur_x = cur_x + (vec_x / 2)
			cur_y = cur_y + (vec_y / 2)

			each_time[-1].append(cur_x)
			each_time[-1].append(cur_y)
			each_time[-1].append(dist_x)
			each_time[-1].append(dist_y)


		# for each_time in info[fake_end_idx+1:]:
		# 	each_time.append([])
		# 	each_time[nodeNumberCounter].append(nodeNumberCounter + 1)

		# 	each_time[nodeNumberCounter].append(cur_x)
		# 	each_time[nodeNumberCounter].append(cur_y)
		# 	each_time[nodeNumberCounter].append(0.0)
		# 	each_time[nodeNumberCounter].append(0.0)

		nodeNumberCounter = nodeNumberCounter + 1

#save new file
# f = open('../test/fake.txt', 'w')
f = open(sys.argv[1] + '.fake.txt', 'w')

f.write(str(nodeCount)+'\n')
f.write(str(endTime)+'\n')

for time, each_time in enumerate(info):
	for each_node in each_time:
		f.write(str(time)+'\t')
		for element in each_node:
			f.write(str(element))
			f.write('\t')
		f.write('\n')

f.close()

print '=== end program ==='
