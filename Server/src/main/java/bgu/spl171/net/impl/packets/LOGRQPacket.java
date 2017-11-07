package bgu.spl171.net.impl.packets;

public class LOGRQPacket implements Packet {

	private String username;
	
	public LOGRQPacket(String username) {
		this.username = username;
	}

	public String username(){
		return username;
	}
	
	@Override
	public short opCode() {
		return 7;
	}

}
