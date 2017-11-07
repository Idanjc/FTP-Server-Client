package bgu.spl171.net.impl.packets;

public class RRQPacket implements Packet {

	public RRQPacket(String filename) {
		this.filename = filename;
	}

	private String filename;
	
	public String filename(){
		return filename;
	}
	
	@Override
	public short opCode() {
		return 1;
	}

}
