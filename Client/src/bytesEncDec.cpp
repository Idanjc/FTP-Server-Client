/*
 * bytesEncDec.cpp
 *
 *  Created on: Jan 14, 2017
 *      Author: idanjc
 */
#include "bytesEncDec.h"

bytesEncDec::bytesEncDec(){}
bytesEncDec::~bytesEncDec(){}
short bytesEncDec:: bytesToShort(char* bytesArr, short amount)
{
	short result;
	if (amount>1){
		result = (short)((bytesArr[0] & 0xff) << 8);
		result += (short)(bytesArr[1] & 0xff);
	}
	else
		result = (short)(bytesArr[0] & 0xff);
    return result;
}

void bytesEncDec::shortToBytes(short num, char* bytesArr, int i)
{
    bytesArr[i] = ((num >> 8) & 0xFF);
    bytesArr[i+1] = (num & 0xFF);
}
