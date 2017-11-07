/*
 * Packet.h
 *
 *  Created on: Jan 16, 2017
 *      Author: idanjc
 */

#ifndef INCLUDE_PACKETS_PACKET_H_
#define INCLUDE_PACKETS_PACKET_H_

#include <string>
#include "bytesEncDec.h"
#include "connectionHandler.h"

/*
 * All the classes for the different classes are here.
 * Each packet inherits from Packet which is an abstract class with only one method - sendData.
 * Some of the classes also have a toString function that prints their message to the screen (ACK, ERROR, BCAST)
 */


/*
 *  Class Packet
 */

class Packet{

public:
	Packet();
	virtual ~Packet();
	virtual void sendData(ConnectionHandler& handler)=0;
};


/*
 * Class logDelRWPackets
 * Used as father class for LOGRQ, DELRQ, RRQ, WRQ
 */

class logDelRWPackets{

protected:
	std::string name;
	int size;
	void sendPacketData(short opCode,ConnectionHandler& handler);
public:
	logDelRWPackets(std::string& _name);
	virtual ~logDelRWPackets();
};


/*
 * Class delRQPacket
 */
class delRQPacket: public logDelRWPackets, public Packet {
private:
	short opCode;
public:
	delRQPacket(std::string& fileName);
	virtual ~delRQPacket();
	virtual void sendData(ConnectionHandler& handler);
};


/*
 * Class logRQPacket
 */
class logRQPacket: public logDelRWPackets, public Packet {

private:
	short opCode;
public:
	logRQPacket(std::string& userName);
	virtual ~logRQPacket();
	virtual void sendData(ConnectionHandler& handler);
};


/*
 * Class RRQ Packet
 */
class RRQPacket: public logDelRWPackets, public Packet {

private:
	short opCode;
public:
	RRQPacket(std::string& fileName);
	virtual ~RRQPacket();
	virtual void sendData(ConnectionHandler& handler);
};


/*
 * Class WRQ Packet
 */

class WRQPacket: public logDelRWPackets, public Packet {
private:
	short opCode;
public:
	WRQPacket(std::string& fileName);
	virtual ~WRQPacket();
	virtual void sendData(ConnectionHandler& handler);
};



/*
 * Class ACKPacket
 */
class ACKPacket: public Packet {
private:
	short blockNumber;
	short opCode;
public:
	ACKPacket(short block);
	virtual ~ACKPacket();
	virtual void sendData(ConnectionHandler& handler);
	void toString();
};

/*
 * Class DIRQPacket
 */

class DIRQPacket: public Packet{
private:
	short opCode;
public:
	DIRQPacket();
	virtual ~DIRQPacket();
	virtual void sendData(ConnectionHandler& handler);
};

/*
 * Class DISCPacket
 */

class DISCPacket: public Packet{
private:
	short opCode;
public:
	DISCPacket();
	virtual ~DISCPacket();
	virtual void sendData(ConnectionHandler& handler);
};
/*
 * Class BCASTPacket
 */

class BCASTPacket: public Packet{
private:
	short opCode;
	short changeInFile; // Whether a file was added (1) or deleted (0)
	int size;
	std::string fileName;
public:
	BCASTPacket(short addOrDel, std::string& file);
	virtual ~BCASTPacket();
	virtual void sendData(ConnectionHandler& handler);
	void toString();

};

/*
 * Class ERRORPacket
 */

class ERRORPacket: public Packet {
private:
	short opCode;
	short errorCode;
	int size;
	std::string errorMsg;
public:
	ERRORPacket(short error, std::string& msg);
	virtual ~ERRORPacket();
	virtual void sendData(ConnectionHandler& handler);
	void toString();
};

/*
 * Class DATAPacket
 */

class DATAPacket: public Packet {
private:
	short opCode;
	short packetSize;
	short blockNumber;
	short size;
	char* data;
public:
	DATAPacket(short packet_size, short block_number, char* _data);
	DATAPacket(const DATAPacket& other); // Copy Constructor
	DATAPacket& operator=(const DATAPacket& other); // Copy assignment operator
	void copy(const DATAPacket& other);
	virtual ~DATAPacket();
	virtual void sendData(ConnectionHandler& handler);
};

#endif /* INCLUDE_PACKETS_PACKET_H_ */
