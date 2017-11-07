/*
 * readingFromSocket.h
 *
 *  Created on: Jan 14, 2017
 *      Author: idanjc
 */

#ifndef INCLUDE_READINGFROMSOCKET_H_
#define INCLUDE_READINGFROMSOCKET_H_
#include <connectionHandler.h>
#include "Packets.h"
class readingFromSocket{

private:
	/*
	 * Fields
	 */
	ConnectionHandler& handler; // The ConnectionHandler used to send/get bytes and packets
	/*
	 * Methods
	 */
	void createAndHandlePacket(short opCode, std::string*& RRQFileName, std::string*& WRQFileName, bool*& isDIRQ, bool*& isRRQ, bool*& isWRQ);// Creates a packet for the given opCode and handle the packet accordingly
	short extractShort(short amount);// Decoded the amount of bytes needed to a short

	// Handling the different packets
	void handleDataPacket(std::string*& RRQFileName, std::string*& WRQFileName, bool*& isDIRQ, bool*& isRRQ, bool*& isWRQ);
	void handleRRQDataPacket(std::string*& RRQFileName, std::string*& WRQFileName, bool*& isDIRQ, bool*& isRRQ, bool*& isWRQ);
	void handleWRQDataPacket(std::string*& RRQFileName, std::string*& WRQFileName, bool*& isDIRQ, bool*& isRRQ, bool*& isWRQ);
	void handleDIRQDataPacket();
	void handleACKPacket(Packet*& packet, std::string*& RRQFileName, std::string*& WRQFileName, bool*& isDIRQ, bool*& isRRQ, bool*& isWRQ);
	void handleErrorPacket(Packet*& packet, bool*& isDIRQ, bool*& isRRQ, bool*& isWRQ);
	void handleBCASTPacket(Packet*& packet);


public:
	readingFromSocket(ConnectionHandler& connectionHandler);
	virtual ~readingFromSocket();
	void run(std::string* RRQFileName, std::string* WRQFileName, bool* isDIRQ, bool* isRRQ, bool* isWRQ, bool* terminateThread); // When run as a thread, this is running first (like run() in Java)
};
#endif /* INCLUDE_READINGFROMSOCKET_H_ */

