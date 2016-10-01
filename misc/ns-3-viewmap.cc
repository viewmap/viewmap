#include "ns3/vector.h"
#include "ns3/string.h"
#include "ns3/socket.h"
#include "ns3/double.h"
#include "ns3/config.h"
#include "ns3/log.h"
#include "ns3/command-line.h"
#include "ns3/mobility-model.h"
#include "ns3/yans-wifi-helper.h"
#include "ns3/position-allocator.h"
#include "ns3/mobility-helper.h"
#include "ns3/internet-stack-helper.h"
#include "ns3/ipv4-address-helper.h"
#include "ns3/ipv4-interface-container.h"
#include <iostream>

#include "ns3/ocb-wifi-mac.h"
#include "ns3/wifi-80211p-helper.h"
#include "ns3/wave-mac-helper.h"

#include "ns3/ns2-mobility-helper.h"

#include "ns3/random-variable-stream.h"
#include "mytag.h"

#include <fstream>
#include <stdlib.h>
#include <cmath>
#include <vector>

using namespace ns3;

NS_LOG_COMPONENT_DEFINE ("viewfinder");


/*
 * In WAVE module, there is no net device class named like "Wifi80211pNetDevice",
 * instead, we need to use Wifi80211pHelper to create an object of
 * WifiNetDevice class.
 *
 * usage:
 *  NodeContainer nodes;
 *  NetDeviceContainer devices;
 *  nodes.Create (2);
 *  YansWifiPhyHelper wifiPhy = YansWifiPhyHelper::Default ();
 *  YansWifiChannelHelper wifiChannel = YansWifiChannelHelper::Default ();
 *  wifiPhy.SetChannel (wifiChannel.Create ());
 *  NqosWaveMacHelper wifi80211pMac = NqosWave80211pMacHelper::Default();
 *  Wifi80211pHelper wifi80211p = Wifi80211pHelper::Default ();
 *  devices = wifi80211p.Install (wifiPhy, wifi80211pMac, nodes);
 *
 * The reason of not providing a 802.11p class is that most of modeling
 * 802.11p standard has been done in wifi module, so we only need a high
 * MAC class that enables OCB mode.
 */


#define PAIRING_INTERVAL 30
#define MARGIN 0.2
#define PROFILE_INTERVAL 60

// struct Info {
// 	Vector position;
// 	Vector velocity;
// };

struct Info {
	uint32_t id;
	Vector position;
	
	Info(uint32_t i, Vector p) : id(i), position(p){

	}
};

struct Info* info;

Time* tmPreviousTxDelay;
Time tmMaximumTxDelay = MilliSeconds(10);

int simulationType = 0;

double endTime = 1.0;

/* Node State:
	0) Broadcast: 자신의 정보를 주기적으로 Broadcast하는 상태
	1) WaitForAck: Connection Acknowledgement Message를 기다리는 상태(Timeout 3초)
	2) Connected: PAIRING_INTERVAL동안 상대방과 Pair가 맺어진 상태
*/
// int* node_state;
// int* node_pair;

Vector *firstLocation;
std::vector<Info> *locations;


// static void TimeOut(uint32_t id) {
// 	if(node_state[id] == 1) {
// 		node_state[id] = 0;
// 		node_pair[id] = -1;
// 		//std::cout << id << " is timeout" << std::endl;
// 	}
// }

// static void EndPairing(uint32_t id) {
// 	if(node_state[id] == 2) {
// 		node_state[id] = 0;
// 		node_pair[id] = -1;
// 	}
// }



/* Receive Packet
	
	(1) 상대방이 Broadcast하는 정보를 받았을 경우
	(2) 상대방으로부터 Connection Request Message를 받았을 경우
	(3) 상대방으로부터 Connection Acknowledgement Message를 받았을 경우

	pairing condition

	1. -1 <= v1 * v2 / |v1| * |v2| < 0
	2. pairingInterval > OD(l[B, t] -> l[A, t+60]) / speed[B, t] * (1 + MARGIN)
	 
*/
void ReceivePacket (Ptr<Socket> socket)
{
	uint32_t id = socket->GetNode()->GetId();
	Ptr<MobilityModel> mob = socket->GetNode()->GetObject<MobilityModel>();

	Ptr<Packet> packet;
	while (packet = socket->Recv ())
	{
		// Vector position = mob->GetPosition();
		// Vector velocity = mob->GetVelocity();

		MyTag tag;
		packet->PeekPacketTag(tag);
		uint32_t sid = tag.GetSimpleValue();

		uint8_t *buffer = new uint8_t[packet->GetSize()];
		packet->CopyData (buffer, packet->GetSize());
		
		char type;
	
		// double p[2], v[2];
		double fl[2];

		memcpy(&type, buffer, sizeof(char));

		// p[0] = ((double*)(buffer+1))[0];
		// p[1] = ((double*)(buffer+1))[1];
		// v[0] = ((double*)(buffer+1))[2];
		// v[1] = ((double*)(buffer+1))[3];
		fl[0] = ((double*)(buffer+1))[4];
		fl[1] = ((double*)(buffer+1))[5];
		
		bool exist = false;
		for (uint i = 0; i < locations[id].size(); i++){	
			if (sid == locations[id][i].id) {
				exist = true;
				break;
			}	
		}
		if (! exist) {
			locations[id].push_back(Info(sid, Vector(fl[0], fl[1], 0.0)));
		}
		
		//std::cout << type << "\t " << sid << " -> " << id << " \t" << p[0] << "\t" << p[1] << "\t" << v[0] << "\t" << v[1] << std::endl;
	
		//std::cout << "나의 id: " << id << std::endl;
		// switch(node_state[id]) {
		// 	case 0: {
		// 		// Receive Broadcast Message
		// 		if(type == '1') {
		// 			uint32_t did = 0;
		// 			memcpy(&did, buffer + 1 + sizeof(double) * 4, sizeof(uint32_t));
		// 			if(id != did) break;
		// 		}
		// 		else if(type == '2') break;
				
		// 		//std::cout << "\t상대방 id: " << sid << std::endl;
		// 		// 내적 조건
		// 		double prod_speed = sqrt(pow(velocity.x, 2) + pow(velocity.y, 2)) * sqrt(pow(v[0], 2) + pow(v[1], 2));
		// 		if(prod_speed <= 0) break;

		// 		double inner = (velocity.x * v[0] + velocity.y * v[1]) / prod_speed;

		// 		if(-1 <= inner && inner < 0) {
		// 			// 근접 조건
		// 			Vector prevMyPosition = info[(int)(Now().GetSeconds()-1)][id].position;
		// 			Vector prevPairPosition = info[(int)(Now().GetSeconds()-1)][sid].position;

		// 			double prevDistance = sqrt(pow(prevMyPosition.x - prevPairPosition.x, 2) + pow(prevMyPosition.y - prevPairPosition.y, 2));
		// 			double currentDistance = sqrt(pow(position.x - p[0], 2) + pow(position.y - p[1], 2));

		// 			if(prevDistance > currentDistance) {
		// 				// 시간 조건
		// 				if((int)(Now().GetSeconds()+PAIRING_INTERVAL) >= endTime) {
		// 					//std::cout << PAIRING_INTERVAL << "초 후 위치를 예측 불가능" << std::endl;
		// 					break;
		// 				}
		// 				Vector predPosition = info[(int)(Now().GetSeconds()+PAIRING_INTERVAL)][id].position;
		// 				double distance = sqrt(pow(predPosition.x - p[0], 2) + pow(predPosition.y - p[1], 2));
		// 				double speed = sqrt(pow(v[0], 2) + pow(v[1], 2));
		// 				if(speed <= 0) break;

						
		// 				std::cout << "\ttime: " << Now().GetSeconds() << std::endl << "\tpairing interval: " << PAIRING_INTERVAL << std::endl
		// 				<< "\tdistance " << distance << " 상대방 position " << p[0] << ", " << p[1] << " 나의 예측 position " << predPosition.x << " " << predPosition.y << std::endl
		// 				<< "\tspeed " << speed * (1 + MARGIN) << " 상대방 velocity " << v[0] << " " << v[1] << std::endl;
						
		// 				if(PAIRING_INTERVAL > distance / (speed * (1 + MARGIN))) {
		// 					char data[1 + sizeof(double) * 4 + sizeof(uint32_t)] = {};

		// 					data[0] = type + 1; // type
		// 					memcpy(data + 1, &position.x, sizeof(double)); // position x
		// 					memcpy(data + 1 + sizeof(double), &position.y, sizeof(double)); // position y
		// 					memcpy(data + 1 + sizeof(double) * 2, &velocity.x, sizeof(double)); // velocity x
		// 					memcpy(data + 1 + sizeof(double) * 3, &velocity.y, sizeof(double)); // velocity y
		// 					memcpy(data + 1 + sizeof(double) * 4, &sid, sizeof(uint32_t)); // destination id
							

		// 					Ptr<Packet> pck = Create<Packet> ((uint8_t*)data, packet->GetSize());
						
		// 					MyTag tag;
		// 					tag.SetSimpleValue(id); // source id
		// 					pck->AddPacketTag(tag);
						
		// 					socket->Send (pck);

		// 					node_pair[id] = sid;

		// 					if(data[0] == '1') {
		// 						node_state[id] = 1;
		// 						//std::cout << id << "->" << sid << " connection request message" << std::endl;
		// 						Simulator::Schedule (Seconds(3), &TimeOut, id);
		// 					}
		// 					else if(data[0] == '2') {
		// 						node_state[id] = 2;
		// 						//std::cout << id << "->" << sid << " connection acknowledgement message" << std::endl;
		// 						Simulator::Schedule (Seconds(PAIRING_INTERVAL), &EndPairing, id);
		// 					}
		// 				}
		// 				else {
		// 					//std::cout << "\t시간 조건 성립하지 않음" << std::endl;
		// 				}
		// 			}
		// 			else {
		// 				//std::cout << "\t근접 조건 성립하지 않음" << std::endl;
		// 			}
		// 		}
		// 		else {
		// 			//std::cout << "\t내적 조건 성립하지 않음" << std::endl;
		// 		}
		// 		break;
		// 	}
		// 	case 1: {
		// 		// Receive Connection Acknowledge Message
		// 		if(type == '2') {
		// 			uint32_t did = 0;
		// 			memcpy(&did, buffer + 1 + sizeof(double) * 4, sizeof(uint32_t));
		// 			if(id == did && node_pair[id] == (int)sid) {
		// 				node_state[id] = 2;
		// 				Simulator::Schedule (Seconds(PAIRING_INTERVAL), &EndPairing, id);
		// 				std::cout << Now().GetSeconds() << " " <<  id << " " << sid << std::endl;
		// 			}
		// 		}
		// 		break;
		// 	}
		// 	case 2: {
		// 		// Nothing
		// 		break;
		// 	}
		// }
		//std::cout << "<Node" << id << " " << "from=\"" << sid << "\" " << "time=\""<< Now().GetSeconds() << "\" />"<<std::endl;
	}
}



/* Send Packet

	자신의 상태가 Broadcast(0)이면 주기적으로(tmPacketInterval마다) 각자 자신의 정보를 Broadcast한다
	
	type[1], information[8 + 8 + 8 + 8], data[]

*/
static void SendPacket (Ptr<Socket> socket, uint32_t packetSize, Time tmPacketInterval)
{
	uint32_t id = socket->GetNode()->GetId();
	
	Ptr<MobilityModel> mob = socket->GetNode()->GetObject<MobilityModel>();

	Vector position = mob->GetPosition();
	Vector velocity = mob->GetVelocity();

	char data[1 + sizeof(double) * 6] = {};

	data[0] = '0';

	memcpy(data + 1, &position.x, sizeof(double));
	memcpy(data + 1 + sizeof(double), &position.y, sizeof(double));
	memcpy(data + 1 + sizeof(double) * 2, &velocity.x, sizeof(double));
	memcpy(data + 1 + sizeof(double) * 3, &velocity.y, sizeof(double));
	memcpy(data + 1 + sizeof(double) * 4, &firstLocation[id].x, sizeof(double));
	memcpy(data + 1 + sizeof(double) * 5, &firstLocation[id].y, sizeof(double));
	
	//if(((double*)(data + 1))[2] != velocity.x) std::cout << "err" << std::endl;
	//std::cout << ((double*)(data + 1))[2] << " " << velocity.x << std::endl;

	//std::cout << "time: " << Now().GetSeconds() << " id: " << id << " vx: " << velocity.x << " vy: " << velocity.y << std::endl;

	Ptr<Packet> pkt = Create<Packet> ((uint8_t*)data, packetSize);

	MyTag tag;
	tag.SetSimpleValue(id);
	pkt->AddPacketTag(tag);

	socket->Send (pkt);
		//std::cout << "Broadcast: " << id << std::endl;

	Ptr<UniformRandomVariable> rand = CreateObject<UniformRandomVariable> ();
	uint32_t d_ns = static_cast<uint32_t> (tmMaximumTxDelay.GetInteger());
	Time tmTxDelay = NanoSeconds (rand->GetInteger(0, d_ns));
	Time txTime = tmPacketInterval - tmPreviousTxDelay[id] + tmTxDelay;
	tmPreviousTxDelay[id] = tmTxDelay;

	Simulator::ScheduleWithContext (socket->GetNode()->GetId(), txTime, &SendPacket, socket, packetSize, tmPacketInterval);
}



/* Print Information

	tmInterval마다 시간별 노드들의 ID와 위치, 속도를 출력한다

*/
static void PrintInformation (const NodeContainer &nodes, Time tmInterval)
{
	NS_LOG_INFO("Information : " <<  Now().GetSeconds());
	//std::cout << "" << std::endl;
	
	//std::cout << "<Time value=\"" << Now().GetSeconds() << "\">" << std::endl;

	for(uint n = 0; n < nodes.GetN(); n++) {
		Ptr<MobilityModel> mob = nodes.Get(n)->GetObject<MobilityModel>();
		Vector position = mob->GetPosition();
		Vector velocity = mob->GetVelocity();
		std::cout << Now().GetSeconds() << "\t" << (n+1) << "\t" << position.x << "\t" << position.y << "\t" << velocity.x << "\t" << velocity.y;

		if (((int)(Now().GetSeconds()) % PROFILE_INTERVAL) == 0) {
			for (uint i = 0; i < locations[n].size(); i++) {
				std::cout << "\t" << (int)(locations[n][i].id + 1) << "\t" << locations[n][i].position.x << "\t" << locations[n][i].position.y;
			}
			locations[n].clear();
			firstLocation[n].x = position.x;
			firstLocation[n].y = position.y;
		}

		std::cout << std::endl;
		// std::cout << "\t<Node id=\"" << n << "\" x=\"" << position.x << "\" y=\"" << position.y << "\" vx=\"" << velocity.x << "\" vy=\"" << velocity.y << "\"" << " />" << std::endl;
	}

	//std::cout << "</Time>" << std::endl;

	Simulator::Schedule (tmInterval, &PrintInformation, nodes, tmInterval);
}

static void CloseSocket(Ptr<Socket> socket)
{
	//std::cout << "close socket" << std::endl;
	socket->Close();
	//std::cout << "close socket complete" << std::endl;
}

static void EndSimulation(int nodeCount, double endTime)
{
	//std::cout << "end simulation" << std::endl;
	delete[] tmPreviousTxDelay;
	// delete[] node_state;
	// delete[] node_pair;

	if(simulationType == 0) {
		// for(int i = 0; i < endTime; i++) {
		// 	//std::cout << "del " << i << std::endl;
		// 	// delete[] info[i];

		// }
		// delete[] info;
		delete[] firstLocation;
		delete[] locations;
		
	}
	//std::cout << "end simulation complete" << std::endl;	
}

int main (int argc, char *argv[])
{

	bool verbose = false;
	std::string phyMode ("OfdmRate6MbpsBW10MHz");
	double txPower = 24.0;
	//uint32_t packetCount = 60;
	uint32_t packetSize = 1000; // bytes
	double packetInterval = 1.0; // seconds
	double startTime = 1.0;
	int nodeCount = 0;
	std::string traceFile;
	//int simulationType = 0;
	std::string simulationFile;



	CommandLine cmd;

	cmd.AddValue ("verbose", "turn on all WifiNetDevice log components", verbose);
	cmd.AddValue ("phyMode", "wifi phy mode", phyMode);
	cmd.AddValue ("txPower", "transmission power", txPower);
	//cmd.AddValue ("packetCount", "number of packets generated", packetCount);
	cmd.AddValue ("packetSize", "size of application packet sent", packetSize);
	cmd.AddValue ("packetInterval", "interval (seconds) between packets", packetInterval);
	cmd.AddValue ("startTime", "", startTime);
	cmd.AddValue ("endTime", "", endTime);
	cmd.AddValue ("nodeCount", "", nodeCount);
	cmd.AddValue ("traceFile", "", traceFile);
	cmd.AddValue ("simulationType", "", simulationType);
	cmd.AddValue ("simulationFile", "", simulationFile);

	cmd.Parse (argc, argv);

	// Simulation Type
	if(simulationType == 0) {
		// Print Information
		firstLocation = new Vector[nodeCount];
		locations = new std::vector<Info>[nodeCount];
		// std::cout << locations[0][0].id << std::endl;

	}
	// if(simulationType == 1) {
	// 	node_state = new int[nodeCount];
	// 	for(int i = 0; i < nodeCount; i++) node_state[i] = 0;

	// 	node_pair = new int[nodeCount];
	// 	for(int i = 0; i < nodeCount; i++) node_pair[i] = -1;

	// 	// Load Information
	// 	std::ifstream infile(simulationFile);
	// 	char line[128];

	// 	infile.getline(line, 100);
	// 	infile.getline(line, 100);

	// 	info = new struct Info*[(int)endTime];
	// 	for(int i = 0; i < endTime; i++) {
	// 		info[i] = new struct Info[nodeCount];
	// 	}
		
	// 	std::string s;
	// 	std::string del = "\t";

	// 	size_t pos = 0;
	// 	std::string token;
	// 	while(!infile.eof()) {
	// 		std::vector<std::string> items;
	// 		infile.getline(line, 100);
	// 		s = line;
	// 		if(s.length() == 0) break;
	// 		while ((pos = s.find(del)) != std::string::npos) {
	// 			token = s.substr(0, pos);
	// 			items.push_back(token);
	// 			s.erase(0, pos + del.length());
	// 		}
	// 		items.push_back(s);

	// 		int time = atoi(items[0].c_str());
	// 		int id = atoi(items[1].c_str()) - 1;

	// 		info[time][id].position.x = atof(items[2].c_str());
	// 		info[time][id].position.y = atof(items[3].c_str());
	// 		info[time][id].velocity.x = atof(items[4].c_str());
	// 		info[time][id].velocity.y = atof(items[5].c_str());
	// 		//std::cout << time << ", " << (id+1) << ", " << info[time][id].position.x << ", " << info[time][id].position.y << std::endl;
	// 	}
	// 	infile.close();
	// 	//std::cout << "Load Complete" << std::endl;
	// }

	NodeContainer c;
	c.Create (nodeCount);

	// The below set of helpers will help us to put together the wifi NICs we want
	YansWifiPhyHelper wifiPhy =  YansWifiPhyHelper::Default ();
	YansWifiChannelHelper wifiChannel = YansWifiChannelHelper::Default ();
	Ptr<YansWifiChannel> channel = wifiChannel.Create ();
	wifiPhy.SetChannel (channel);
	// ns-3 supports generate a pcap trace
	wifiPhy.SetPcapDataLinkType (YansWifiPhyHelper::DLT_IEEE802_11);
	wifiPhy.Set("TxPowerStart", DoubleValue(txPower));
	wifiPhy.Set("TxPowerEnd", DoubleValue(txPower));
	NqosWaveMacHelper wifi80211pMac = NqosWaveMacHelper::Default ();
	Wifi80211pHelper wifi80211p = Wifi80211pHelper::Default ();
	if (verbose)
		wifi80211p.EnableLogComponents ();      // Turn on all Wifi 802.11p logging

	wifi80211p.SetRemoteStationManager ("ns3::ConstantRateWifiManager",
		                      "DataMode",StringValue (phyMode),
		                      "ControlMode",StringValue (phyMode));
	NetDeviceContainer devices = wifi80211p.Install (wifiPhy, wifi80211pMac, c);

	// Tracing
	//wifiPhy.EnablePcap ("wave-simple-80211p", devices);
	MobilityHelper mobility;

	/*
	Ptr<ListPositionAllocator> positionAlloc = CreateObject<ListPositionAllocator> ();
	positionAlloc->Add (Vector (0.0, 0.0, 0.0));
	positionAlloc->Add (Vector (250.0, 0.0, 0.0));
	mobility.SetPositionAllocator (positionAlloc);
	mobility.SetMobilityModel ("ns3::ConstantPositionMobilityModel");
	mobility.Install (c);
	*/
	
	Ns2MobilityHelper ns2 = Ns2MobilityHelper(traceFile);
	mobility.SetMobilityModel("ns3::ConstantPositionMobilityModel");
	mobility.Install(c);
	ns2.Install();

	InternetStackHelper internet;
	internet.Install (c);
	Ipv4AddressHelper ipv4;
	NS_LOG_INFO ("Assign IP Addresses.");
	ipv4.SetBase ("10.0.0.0", "255.255.240.0");
	Ipv4InterfaceContainer i = ipv4.Assign (devices);

	TypeId tid = TypeId::LookupByName ("ns3::UdpSocketFactory");

	Ptr<UniformRandomVariable> rand = CreateObject<UniformRandomVariable> ();

	/*
	std::cout << "<result>" << std::endl;
	std::cout << "<info>" << std::endl
	    << "\t<nodeCount>" << nodeCount << "</nodeCount>" << std::endl
	    << "\t<packetSize>" << packetSize << "</packetSize>" << std::endl
	    << "\t<startTime>" << startTime << "</startTime>" << std::endl
	    << "\t<endTime>" << endTime << "</endTime>" << std::endl
	    << "\t<txPower>" << txPower << "</txPower>" << std::endl
	    << "\t<traceFile>" << traceFile << "</traceFile>" << std::endl
	    << "\t<packetInterval>" << packetInterval << "</packetInterval>" << std::endl
	    << "\t<packetNumber>" << packetCount << "</packetNumber>" << std::endl
	    << "</info>" << std::endl << std::endl;
	*/

	std::cout << "start simulation" << std::endl;
	std::cout << nodeCount << std::endl;
	std::cout << endTime << std::endl;

	Time tmStart = Seconds(startTime);

	tmPreviousTxDelay = new Time[nodeCount];
	for (int id = 0; id < nodeCount; id++)
	{

		Ptr<Socket> recvSink = Socket::CreateSocket (c.Get (id), tid);
		InetSocketAddress local = InetSocketAddress (Ipv4Address::GetAny (), 80);
		recvSink->Bind (local);
		recvSink->SetRecvCallback (MakeCallback (&ReceivePacket));
		std::pair<Ptr<Ipv4>, uint32_t> interface = i.Get (id);
		Ptr<Ipv4> pp = interface.first;
		Ptr<NetDevice> dev = pp->GetObject<NetDevice> ();
		recvSink->BindToNetDevice (dev);

		//Ptr<Socket> source = Socket::CreateSocket (c.Get (id), tid);
		InetSocketAddress remote = InetSocketAddress (Ipv4Address ("255.255.255.255"), 80);
		recvSink->SetAllowBroadcast (true);
		recvSink->Connect (remote);

		if(simulationType == 0) {
			uint32_t d_ns = static_cast<uint32_t> (tmMaximumTxDelay.GetInteger());
			Time tmDraft = NanoSeconds (rand->GetInteger (0, double(0.5)));
			Time tmTxDelay = NanoSeconds (rand->GetInteger (0, d_ns));
			Time txTime = tmStart + tmDraft + tmTxDelay;
			tmPreviousTxDelay[id] = tmTxDelay;
			Simulator::ScheduleWithContext (recvSink->GetNode ()->GetId (), txTime, &SendPacket, recvSink, packetSize, Seconds (packetInterval));
		}
		Simulator::Schedule (Seconds(endTime), &CloseSocket, recvSink);
	}

	if(simulationType == 0) Simulator::Schedule (Seconds(0.0), &PrintInformation, c, Seconds(1.0));
	Simulator::Schedule (Seconds(endTime), &EndSimulation, nodeCount, endTime);
	Simulator::Stop (Seconds(endTime));
	Simulator::Run ();

	//std::cout << "</result>" << std::endl;

	Simulator::Destroy ();

	// std::cout << "=== Check End ===" << std::endl;

	return 0;
}
