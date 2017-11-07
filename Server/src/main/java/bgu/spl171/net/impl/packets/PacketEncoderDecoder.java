package bgu.spl171.net.impl.packets;

import bgu.spl171.net.api.MessageEncoderDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class PacketEncoderDecoder implements MessageEncoderDecoder<Packet> {

    private byte[] opBytes = new byte[2];
    private byte[] tempBytes = new byte[1 << 10]; // used to encode further after we have an opcode
    private byte[] dataBytes; // used to hold the data sent in a data packet
    private short opCode;
    private int opLen = 0;
    private int tempLen = 0;
    private short dataLen = -1;
    
    @Override
    public Packet decodeNextByte(byte nextByte) {
    	if (opLen < 2){
    		pushByteToOp(nextByte);
	    	if (opLen == 2){ // now we know what packet this is
	    		opCode = bytesToShort(opBytes, 0); //find out what the packet is
	    		if(opCode < 1 || opCode >10){  // if the opcode is illegal we will erase the message,
	    			resetByteArrays(); 			//return a corrupted packet and start from scratch
	    			return new CorruptedPacket();
				}
	    		if(opCode == 6){ // in case of a DIRQ the opCode is enough
	    			resetByteArrays();
	    			return new DIRQPacket();
	    		}
	    		if(opCode == 10){ // DISC
	    			resetByteArrays();
					return new DISCPacket();
	    		}
	    	}
	    	return null; //unknown packet yet
    	}
    	else{ // if we already read 2 bytes we have an opcode and we know what kind of a packet this is
    		switch (opCode) {
    		
			case 1://RRQ
				if (nextByte == '\0') {
					String filename = popString();
					resetByteArrays();
		            return new RRQPacket(filename);
		        }
		        pushByteToTemp(nextByte);
		        return null; //not a name yet
		        
			case 2: // WRQ
				if (nextByte == '\0') {
					String filename = popString();
					resetByteArrays();
		            return new WRQPacket(filename);
		        }
		        pushByteToTemp(nextByte);
		        return null; //not a name yet
		        
			case 3: // DATA
				if (tempLen < 4){
		    		pushByteToTemp(nextByte);
		    		if (tempLen == 4 && dataLen == -1){
		    			dataBytes = new byte[bytesToShort(tempBytes, 0)];
		    			if (dataBytes.length == 0){
		    				resetByteArrays();
    						return new DATAPacket(bytesToShort(tempBytes, 0), bytesToShort(tempBytes, 2), dataBytes);
		    			}
		    			dataLen = 0;
		    		}
		            return null; //unknown size yet
		    	}
				else{
					dataBytes[dataLen++] = nextByte;
					if(dataBytes.length == dataLen){
						dataLen = -1;
						resetByteArrays();
						return new DATAPacket(bytesToShort(tempBytes, 0), bytesToShort(tempBytes, 2), dataBytes);
					}
				}
				return null;
				
			case 4: // ACK
	    		pushByteToTemp(nextByte);
	    		if (tempLen == 2){
					resetByteArrays();
					return new ACKPacket(bytesToShort(tempBytes, 0));
	    		}
	    		return null; //unknown block yet
				
			case 5: // ERROR
				if (tempLen < 2){
		    		pushByteToTemp(nextByte);
		            return null; //unknown error code yet
		    	}
				if (nextByte == '\0') {
					resetByteArrays();
		            return new ERRORPacket(bytesToShort(tempBytes, 0),popStringFrom(2));
		        }
		        pushByteToTemp(nextByte);
		        return null; //not a message yet
		        
			case 7: // LOGRQ
				if (nextByte == '\0') {
					String username = popString();
					resetByteArrays();
		            return new LOGRQPacket(username);
		        }
		        pushByteToTemp(nextByte);
		        return null; //not a name yet
		        
			case 8: // DELRQ
				if (nextByte == '\0') {
					String filename = popString();
					resetByteArrays();
		            return new DELRQPacket(filename);
		        }
		        pushByteToTemp(nextByte);
		        return null; //not a name yet
		        
			case 9: // BCAST. Since we should have never got it, we don't really care what's attached to it.
				return new BCASTPacket(false, "");
				
			default: // shouldn't happen
				resetByteArrays();
				return new CorruptedPacket();
			}
    	}
    	
    }

    @Override
    public byte[] encode(Packet packet) {
    	byte[] result;
        switch (packet.opCode()) {
		
        case 3: // DATA
			result = new byte[6 + ((DATAPacket)packet).packetSize()];
			shortToBytes((short)3, result, 0);
			shortToBytes(((DATAPacket)packet).packetSize(), result, 2);
			shortToBytes(((DATAPacket)packet).blockNum(), result, 4);
			for (int i = 0; i < ((DATAPacket)packet).data().length; i++) {
				result[i+6] = ((DATAPacket)packet).data()[i];
			}
			return result;
		
        case 4: // ACK
			result = new byte[4];
			shortToBytes((short)4, result, 0);
			shortToBytes(((ACKPacket)packet).BlockNum(), result, 2);
			return result;
		
        case 5: // ERROR
			byte[] errMsgBytes = ((ERRORPacket)packet).errMsg().getBytes();
			result = new byte[5+errMsgBytes.length];
			shortToBytes((short)5, result, 0);
			shortToBytes(((ERRORPacket)packet).errorCode(), result, 2);
			System.arraycopy(errMsgBytes, 0, result, 4, errMsgBytes.length);
			result[result.length-1] = '\0';
			return result;
		
        case 9: // BCAST
			byte[] fileNameBytes = ((BCASTPacket)packet).filename().getBytes();
			result = new byte[4+fileNameBytes.length];
			shortToBytes((short)9, result, 0);
			result[2] = (byte)(((BCASTPacket)packet).isAdded() ? 1 : 0 );
			System.arraycopy(fileNameBytes, 0, result, 3, fileNameBytes.length);
			result[result.length-1] = '\0';
			return result;
		default:
			return encode(new ERRORPacket(0, "Oops! The server tried to send an illegal packet "+packet.opCode()));
		}
    }
	
    // helping functions: encoding:
    
    /**
     * just like your original, but able to put the values in an existing byte array where we choose
     * @param num: the number to encode
     * @param bytesArr: the byte array to write to
     * @param pos: where in the array to put the bytes. will use bytesArr in pos and pos+1
     */
	public void shortToBytes(short num, byte[] bytesArr, int pos)
	{
	    bytesArr[pos] = (byte)((num >> 8) & 0xFF);
	    bytesArr[pos+1] = (byte)(num & 0xFF);
	}

    // helping functions: decoding:
    
	/**
     * Just like the original, but now it can pick a short encoded in the middle of the byte array
     * @param byteArr: the byte array to decode from
     * @param startPos: where to start the decoding. Continue to the end.
     * @return the short taken from the byte array
     */
	public short bytesToShort(byte[] byteArr, int startPos)  
    {
        short result = (short)((byteArr[startPos] & 0xff) << 8);
        result += (short)(byteArr[startPos+1] & 0xff);
        return result;
    }
    
    private void pushByteToOp(byte nextByte) {
        opBytes[opLen++] = nextByte;
    }
    private void pushByteToTemp(byte nextByte) {
    	if (tempLen >= tempBytes.length) {
    		tempBytes = Arrays.copyOf(tempBytes, opLen * 2);
    	}
    	tempBytes[tempLen++] = nextByte;
    }
   
    /**
     * get a string from tempBytes
     * @return string decoded from whatever found in tempByte
     */
    private String popString() { 
        String result = new String(tempBytes, 0, tempLen, StandardCharsets.UTF_8);
        return result;
    }
    
    /**
     * get a string from tempBytes
     * @return string decoded from whatever found in tempByte starting from i position
     */
    private String popStringFrom(int i) {
    	String result = new String(tempBytes, i, tempLen, StandardCharsets.UTF_8);
        return result;
	}
    
    /**
     *  reset the registered lengths so we can start from scratch for next message
     */
    private void resetByteArrays(){
    	opLen = 0;
    	tempLen = 0;
    	opCode = 0;
    }

}
