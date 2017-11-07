package bgu.spl171.net.impl.packets;

public class ACKPacket implements Packet {

	private short blockNum;
	
	public ACKPacket(){
		blockNum = 0;
	}
	
	public ACKPacket(short blockNum){
		this.blockNum = blockNum;
	}
	
	public short BlockNum() {
		return blockNum;
	}

	@Override
	public short opCode() {
		return 4;
	}

}
