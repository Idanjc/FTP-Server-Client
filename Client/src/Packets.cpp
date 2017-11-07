/*
 * Packets.cpp
 *
 *  Created on: Jan 16, 2017
 *      Author: idanjc
 */
#include "Packets.h"
#include <boost/lexical_cast.hpp>
#include <iostream>

/*
 *  Class Packet
 */
Packet::Packet(){}
Packet::~Packet(){}

/*
 * Class logDelRWPackets
 * Used as father class for LOGRQ, DELRQ, RRQ, WRQ
 */
logDelRWPackets::logDelRWPackets(std::string& _name): name(_name),size(3){
	size = size + name.length();
}
logDelRWPackets::~logDelRWPackets(){}


void logDelRWPackets::sendPacketData(short opCode,ConnectionHandler& handler){
	char data[size];
	bytesEncDec ed;
	ed.shortToBytes(opCode,data,0);
	for (unsigned int i = 0;i<name.length();i++)
		data[i+2] = name.at(i);
	data[size -1] = '\0';
	handler.sendBytes(data,size);
}

/*
 * Class delRQPacket
 */

delRQPacket::delRQPacket(std::string& fileName): logDelRWPackets(fileName), Packet(), opCode(8){}
delRQPacket::~delRQPacket(){}

void delRQPacket::sendData(ConnectionHandler& handler){
	sendPacketData(opCode, handler);
}

/*
 * Class logRQPacket
 */

logRQPacket::logRQPacket(std::string& userName): logDelRWPackets(userName), Packet(), opCode(7){}
logRQPacket::~logRQPacket(){}

void logRQPacket::sendData(ConnectionHandler& handler){
	sendPacketData(opCode, handler);
}

/*
 * Class RRQ Packet
 */

RRQPacket::RRQPacket(std::string& fileName): logDelRWPackets(fileName), Packet(), opCode(1){}
RRQPacket::~RRQPacket(){}

void RRQPacket::sendData(ConnectionHandler& handler){
	sendPacketData(opCode,handler);
}

/*
 * Class WRQ Packet
 */

WRQPacket::WRQPacket(std::string& fileName): logDelRWPackets(fileName),Packet(), opCode(2){}
WRQPacket::~WRQPacket(){}

void WRQPacket::sendData(ConnectionHandler& handler){
	sendPacketData(opCode,handler);
}

/*
 * Class ACKPacket
 */

ACKPacket::ACKPacket(short block):Packet(), blockNumber(block), opCode(4){}
ACKPacket::~ACKPacket(){}

void ACKPacket::sendData(ConnectionHandler& handler){
	bytesEncDec ed;
	char data[4];
	ed.shortToBytes(opCode,data,0);
	ed.shortToBytes(blockNumber,data,2);
	handler.sendBytes(data,4);
}

void ACKPacket::toString(){
	std::cout << "ACK " + boost::lexical_cast<std::string>(blockNumber) << std::endl;
}

/*
 * Class DIRQPacket
 */

DIRQPacket::DIRQPacket():Packet(), opCode(6){}
DIRQPacket::~DIRQPacket(){}

void DIRQPacket::sendData(ConnectionHandler& handler){
	char data[2];
	bytesEncDec ed;
	ed.shortToBytes(opCode,data,0);
	handler.sendBytes(data,2);
}

/*
 * Class DISCPacket
 */

DISCPacket::DISCPacket():Packet(), opCode(10){}
DISCPacket::~DISCPacket(){}

void DISCPacket::sendData(ConnectionHandler& handler){
	char data[2];
	bytesEncDec ed;
	ed.shortToBytes(opCode,data,0);
	handler.sendBytes(data,2);
}

/*
 * Class BCASTPacket
 */

BCASTPacket::BCASTPacket(short addOrDel, std::string& file): Packet(), opCode(9), changeInFile(addOrDel), size(4), fileName(file){
	size = size +fileName.length();
}
BCASTPacket::~BCASTPacket(){}

void BCASTPacket::sendData(ConnectionHandler& handler){}

void BCASTPacket::toString(){
	std::string ans;
	switch(changeInFile){
		case 0:
			ans = "BCAST del " + fileName;
			break;
		case 1:
			ans = "BCAST add " + fileName;
			break;
	}
	std::cout<< ans << std::endl;
}

/*
 * Class ERRORPacket
 */

ERRORPacket::ERRORPacket(short error, std::string& msg): Packet(), opCode(5), errorCode(error), size(5), errorMsg(msg){
	size = size + errorMsg.length();
}
ERRORPacket::~ERRORPacket(){}

void ERRORPacket::sendData(ConnectionHandler& handler){
	char data[size];
	bytesEncDec ed;
	ed.shortToBytes(opCode,data,0);
	ed.shortToBytes(errorCode,data,2);
	for (unsigned int i=0;i<errorMsg.length();i++)
		data[i+4] = errorMsg.at(i);
	data[size-1] = '\0';
	handler.sendBytes(data,size);
}

void ERRORPacket::toString(){
	std::cout << "Error " + boost::lexical_cast<std::string>(errorCode) + " Message: " + errorMsg << std::endl;
}

/*
 * Class DATAPacket
 */

DATAPacket::DATAPacket(short packet_size, short block_number, char* _data): opCode(3), packetSize(packet_size), blockNumber(block_number), size(6), data(_data){
	size = size + packetSize;
}

DATAPacket::DATAPacket(const DATAPacket& other):opCode(other.opCode), packetSize(other.packetSize), blockNumber(other.blockNumber), size(other.size), data(other.data){}

void DATAPacket::copy(const DATAPacket& other){
		opCode = other.opCode;
		packetSize = other.packetSize;
		blockNumber = other.blockNumber;
		size = other.size;
		for(int i =0; i<other.packetSize;i++)
			data[i] = (other.data)[i];
}
DATAPacket& DATAPacket::operator=(const DATAPacket& other){
	if (this != &other){
		copy(other);
	}
	return *this;
}
DATAPacket::~DATAPacket(){
	delete data;
}

void DATAPacket::sendData(ConnectionHandler& handler){
	char sentData[size];
	bytesEncDec ed;
	ed.shortToBytes(opCode, sentData, 0);
	ed.shortToBytes(packetSize, sentData, 2);
	ed.shortToBytes(blockNumber, sentData, 4);
	for(short i=0;i<packetSize; i++){
		sentData[i+6] = data[i];
	}
	handler.sendBytes(sentData,size);
}


