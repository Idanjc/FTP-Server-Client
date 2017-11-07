package bgu.spl171.net.impl.packets;


/**
 * this is an interface for all the packet types.
 * It contains only one method: an opcode getter which helps other functions to know
 * what kind of a packet this is for further handling.
 * then, every individual packet contains the relevant fields and getters for them
 * (DATA also contains a package-protected setter for a last-packet-fix in an RRQ
 * process in the protocol.    
 *
 */
public interface Packet {

	public short opCode();
}
