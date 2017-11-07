package bgu.spl171.net.impl.packets;

public class ERRORPacket implements Packet {

	/** reminder to myself (delete later):
	 * Value Meaning
		0 Not defined, see error message (if any).
		1 File not found – RRQ of non-existing file
		2 Access violation – File cannot be written, read or deleted.
		3 Disk full or allocation exceeded – No room in disk.
		4 Illegal TFTP operation – Unknown Opcode.
		5 File already exists – File name exists on WRQ.
		6 User not logged in – Any opcode received before Login completes.
		7 User already logged in – Login username already connected.
	 * 
	 */
	private short errorCode;
	private String errMsg;
	
	public ERRORPacket(int errorCode, String errMsg) {
		super();
		this.errorCode = (short)errorCode;
		this.errMsg = errMsg;
	}


	public short errorCode() {
		return errorCode;
	}


	public String errMsg() {
		return errMsg;
	}


	@Override
	public short opCode() {
		return 5;
	}

}
