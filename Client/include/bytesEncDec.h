/*
 * bytesEncDec.h
 *
 *  Created on: Jan 14, 2017
 *      Author: idanjc
 */

#ifndef INCLUDE_BYTESENCDEC_H_
#define INCLUDE_BYTESENCDEC_H_

class bytesEncDec{

public:
	bytesEncDec();
	virtual ~bytesEncDec();

    // Decodes bytes to short
    short bytesToShort(char* bytesArr, short amount);

    // Encodes short to bytes at bytes[i],bytes[i+1]
    void shortToBytes(short num, char* bytesArr, int i);


};

#endif /* INCLUDE_BYTESENCDEC_H_ */
