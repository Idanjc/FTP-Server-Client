package bgu.spl171.net.impl.packets;

public class DATAPacket implements Packet {

	private short packetSize;
	private short blockNum;
	private byte[] data; 
	
	public DATAPacket(short packetSize, short blockNum, byte[] data) {
		this.packetSize = packetSize;
		this.blockNum = blockNum;
		this.data = data;
	}

	public short packetSize() {
		return packetSize;
	}

	public short blockNum() {
		return blockNum;
	}

	public byte[] data() {
		return data;
	}
	
	void setData(byte[] data){
		this.data = data; 
	}
	
	@Override
	public short opCode() {
		return 3;
	}

}
