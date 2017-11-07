package bgu.spl171.net.impl.packets;

public class BCASTPacket implements Packet {

	private boolean isAdded;
	private String filename;
	
	public BCASTPacket(boolean isAdded, String filename) {
		super();
		this.isAdded = isAdded;
		this.filename = filename;
	}


	public boolean isAdded() {
		return isAdded;
	}


	public String filename() {
		return filename;
	}


	@Override
	public short opCode() {
		return 9;
	}

}
