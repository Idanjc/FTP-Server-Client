#include "readingFromSocket.h"
#include <boost/thread.hpp>

#include <fstream>
#include <algorithm>

readingFromSocket::readingFromSocket(ConnectionHandler& connectionHandler):	handler(connectionHandler) {}
readingFromSocket::~readingFromSocket(){}

// Public Methods
void readingFromSocket::run(std::string* RRQFileName,std::string* WRQFileName, bool* isDIRQ, bool* isRRQ, bool* isWRQ, bool* terminateThread){
	while(!(*terminateThread)){
		short opCode = extractShort(2); // Read first 2 bytes and create short for opCode
		createAndHandlePacket(opCode, RRQFileName, WRQFileName, isDIRQ, isRRQ, isWRQ); // Create a packet according to the opCode and handle it appropriatly
	}
}

//Private methods

// Encode the two bytes that make a short
short readingFromSocket::extractShort(short amount){
	short ans;
	char data[amount];
	handler.getBytes(data,amount);
	bytesEncDec ed;
	ans = ed.bytesToShort(data,amount);
	return ans;
}


//Create a new packet, according to the given opCode then handle it according to the packet type
void readingFromSocket::createAndHandlePacket(short opCode, std::string*& RRQFileName, std::string*& WRQFileName, bool*& isDIRQ, bool*& isRRQ, bool*& isWRQ){
	Packet* packet;
	std::string contents; //string that would contain the message of a packet
	short number; // short that would contain the numbers: block number (for ACK), error code (for ERROR) or the addOrDel (for BCAST)
	switch(opCode){
	case 3: // DATA Packet
		handleDataPacket(RRQFileName, WRQFileName, isDIRQ, isRRQ, isWRQ);
		break;
	case 4: // ACK Packet - get the ACK block number and handle it
		number = extractShort(2);
		packet = new ACKPacket(number);
		handleACKPacket(packet,RRQFileName, WRQFileName, isDIRQ, isRRQ, isWRQ);
		delete packet;
		 //Check if a WRQ was requested and client needs to write a file to the server
		if ((*isWRQ) == true && number == 0){
			handleWRQDataPacket(RRQFileName, WRQFileName, isDIRQ, isRRQ, isWRQ);
			(*WRQFileName) = "";
			(*isWRQ) = false;
		}
		break;
	case 5: // ERROR Packet- get error number, the error message, create a new ERROR packet and handle it
		number = extractShort(2);
		handler.getLine(contents);
		packet = new ERRORPacket(number,contents);
		handleErrorPacket(packet, isDIRQ, isRRQ, isWRQ);
		delete packet;
		break;
	case 9: // BCAST Packet - get what happened (add/del), get to who it happened and handle it
		number = extractShort(1);
		handler.getLine(contents);
		packet = new BCASTPacket(number, contents);
		handleBCASTPacket(packet);
		delete packet;
		break;
	}
}

void readingFromSocket::handleDataPacket(std::string*& RRQFileName, std::string*& WRQFileName, bool*& isDIRQ, bool*& isRRQ, bool*& isWRQ){
	if ( (*isRRQ) == true) // Client sent a RRQ - we handle it
	{
		handleRRQDataPacket(RRQFileName, WRQFileName, isDIRQ, isRRQ, isWRQ);
		(*RRQFileName) = "";
		(*isRRQ) = false;
	}
	if ( (*isDIRQ) == true){ // Client sent DIRQ - we handle it
		handleDIRQDataPacket();
		(*isDIRQ) = false;
	}
}

void readingFromSocket::handleRRQDataPacket(std::string*& RRQFileName, std::string*& WRQFileName, bool*& isDIRQ, bool*& isRRQ, bool*& isWRQ){
	std::ofstream outfile(*RRQFileName);
	short packetSize, blockNumber,opCode;
	if(outfile.is_open()){
		do{ // Send all the data packets
			packetSize = extractShort(2);
			blockNumber = extractShort(2);
			char data[packetSize];
			handler.getBytes(data, packetSize);
			outfile.write(data,packetSize);
			Packet* packet = new ACKPacket(blockNumber);
			packet->sendData(handler);
			if (packetSize == 512){
			/*
			 * 	Wait for the opCode of the next packet. If it is a data packet, continue reading.
			 *	If it's not a data packet, it should be an Error packet, handle it and stop reading the file.
			 */
				opCode = extractShort(2);
				if (opCode != 3){//Not a Data Packet - meaning ERROR Packet
					createAndHandlePacket(opCode, RRQFileName, WRQFileName, isDIRQ, isRRQ, isWRQ);
					delete packet;
					break;
				}
			}
			delete packet;
		}while (packetSize == 512);
		outfile.flush();
		std::cout << "RRQ " + (*RRQFileName) + " complete" << std::endl;
	}
	outfile.close();
}

void readingFromSocket::handleWRQDataPacket(std::string*& RRQFileName, std::string*& WRQFileName, bool*& isDIRQ, bool*& isRRQ, bool*& isWRQ){
	std::ifstream infile(*WRQFileName);
	int size, i=0;
	short blockNumber = 1;
	const short maxPacketSize = 512;
	bool bytesLeftToRead = true;
	infile.seekg(0,std::ios::end);
	size = infile.tellg(); // size of the file
	infile.seekg(0,std::ios::beg);
	if (infile.is_open()){
		do{ // Send al the data packets
			if ((size - i - maxPacketSize) >= 0){ // There are more than 512 bytes left to read in the file
				char* data = new char[maxPacketSize];
				infile.read(data,maxPacketSize);
				Packet* packet = new DATAPacket(maxPacketSize,blockNumber, data);
				packet->sendData(handler);
				blockNumber = blockNumber + 1;
				i = i + maxPacketSize;
				delete packet;
			}
			else{ // There are less than 512 bytes to read in the file, meaning this is the last packet to send
				char* data = new char[size-i];
				infile.read(data, size-i);
				Packet* packet = new DATAPacket((short)(size-i),blockNumber, data);
				packet->sendData(handler);
				bytesLeftToRead = false;
				delete packet;
			}
			short opCode = extractShort(2); // A Data packet was sent, get response from server - should be ACK or ERROR. Block until response arrives (get it's opCode)
			createAndHandlePacket(opCode,RRQFileName, WRQFileName, isDIRQ, isRRQ, isWRQ); // Create a packet for the response and handle it (ERROR - write the error to the screen and stop the WRQ, ACK - write ACK<blockNumber> to screen and continue)
		}while(bytesLeftToRead == true && (*isWRQ) == true);
		std::cout << "WRQ " + (*WRQFileName) + " complete" << std::endl;
	}
	infile.close();
}

void readingFromSocket::handleDIRQDataPacket(){
	short packetSize, blockNumber;
	std::string dir = "";
	do{ // Get all the data packets
		packetSize = extractShort(2);
		blockNumber = extractShort(2);
		char data[packetSize];
		handler.getBytes(data, packetSize);
		for(int i =0; i<packetSize;i++) // add the data to a string that would hold all of the data in the end
			dir = dir + data[i];
		Packet* packet = new ACKPacket(blockNumber);
		packet->sendData(handler);
		if (packetSize == 512){
		/*
		 * The next Packet to receive will still be a data packet of the same file, but the 2 first bytes are still the opcode.
		* This will wait until the opcode is taken from the packet, leaving the necessary data
		*/
			extractShort(2);
		}
		delete packet;
	}while (packetSize == 512);
	std::replace(dir.begin(),dir.end(),'\0','\n'); // replace all occurrences of \0 in \n
	std::string dirWithoutLastSpace = dir.substr(0,dir.length()-1); // omit the last \n so we don't get a two new lines after the last file
	std::cout << dirWithoutLastSpace << std::endl; // print the files
}

void readingFromSocket::handleACKPacket(Packet*& packet, std::string*& RRQFileName, std::string*& WRQFileName, bool*& isDIRQ, bool*& isRRQ, bool*& isWRQ){
	(dynamic_cast<ACKPacket*>(packet))->toString();
}

void readingFromSocket::handleErrorPacket(Packet*& packet, bool*& isDIRQ, bool*& isRRQ, bool*& isWRQ){
	(dynamic_cast<ERRORPacket*>(packet))->toString();
	// If an error was received after RRQ,WRQ or DIRQ, reset their boolean flags:
	if ((*isRRQ) == true || (*isWRQ) == true || (*isDIRQ) == true){
		(*isRRQ) = false;
		(*isWRQ) = false;
		(*isDIRQ) = false;
	}
}
void readingFromSocket::handleBCASTPacket(Packet*& packet){
	(dynamic_cast<BCASTPacket*>(packet))->toString() ;
}

