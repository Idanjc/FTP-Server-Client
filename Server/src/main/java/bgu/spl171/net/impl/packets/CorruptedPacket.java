package bgu.spl171.net.impl.packets;

public class CorruptedPacket implements Packet {

	/**
	 *  this is a packet sent by the message encoder-decoder if the opcode was incorrect.
	 *  It allows us to deal with this error here in the protocol
	 */
	
	@Override
	public short opCode() {
		return 0;
	}

}
