package bgu.spl171.net.impl.packets;

public class DELRQPacket implements Packet {

	private String filename;
	
	public DELRQPacket(String filename) {
		this.filename = filename;
	}

	public String filename(){
		return filename;
	}
	
	@Override
	public short opCode() {
		return 8;
	}

}
