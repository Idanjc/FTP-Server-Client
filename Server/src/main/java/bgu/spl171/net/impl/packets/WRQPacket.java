package bgu.spl171.net.impl.packets;

public class WRQPacket implements Packet {

	private String filename;
	
	public WRQPacket(String filename) {
		this.filename = filename;
	}

	public String filename(){
		return filename;
	}
	
	@Override
	public short opCode() {
		return 2;
	}

}
