
package bgu.spl171.net.impl.packets;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.srv.bidi.Connections;
import bgu.spl171.net.srv.bidi.ConnectionsImpl;

public class PacketsProtocol implements BidiMessagingProtocol<Packet> {

	private int id;
	private ConnectionsImpl<Packet> connections;
	private LinkedList<DATAPacket> sentDataList = new LinkedList<>();
	private short waitingForACK = -1;
	private short waitingForData = -1;
	private File writtenFile;
	private boolean shouldTerminate;

	@Override
	public void start(int connectionId, Connections<Packet> connections) {
		id = connectionId;
		this.connections = (ConnectionsImpl<Packet>) connections;
		shouldTerminate = false;
	}

	/**
	 * First, we check what opcode this packet has and process it in one of the other functions
	 */
	@Override
	public void process(Packet message) {
		if (message.opCode() < 1 || message.opCode() > 10)
			send(new ERRORPacket(4, "unkown opcode"));
		if (connections.isLoggedIn(id) || message.opCode()==7) {
			switch (message.opCode()) {
			case 1:
				RRQProcess((RRQPacket) message);
				break;
			case 2:
				WRQProcess((WRQPacket) message);
				break;
			case 3:
				DATAProcess((DATAPacket) message);
				break;
			case 4:
				ACKProcess((ACKPacket) message);
				break;
			case 5:
				ERRORProcess((ERRORPacket) message);
				break;
			case 6:
				DIRQProcess((DIRQPacket) message);
				break;
			case 7:
				LOGRQProcess((LOGRQPacket) message);
				break;
			case 8:
				DELRQProcess((DELRQPacket) message);
				break;
			case 9:
				BCASTProcess((BCASTPacket) message);
				break;
			case 10:
				DISCProcess((DISCPacket) message);
				break;
			default:
				break;
			}
		} else {
			send(new ERRORPacket(6, "user is not logged in"));
		}
	}

	/**
	 * The following functions are taking care of packets after they are recognized 
	 * @param message - the packet
	 */
	
	private void DISCProcess(DISCPacket message) {
		send(new ACKPacket());
		connections.disconnect(id);
		shouldTerminate = true;
	}

	private void BCASTProcess(BCASTPacket message) {
		send(new ERRORPacket(4, "This is a server-to-client packet only. where did you get it?"));
	}

	private void DELRQProcess(DELRQPacket message) {
		File file = stringToFile(message.filename());
		if (isFileExists(file)) {
			file.delete();
			send(new ACKPacket());
			connections.broadcast(new BCASTPacket(false, message.filename()));
		} else
			send(new ERRORPacket(1, "file not found"));
	}

	private void LOGRQProcess(LOGRQPacket message) {
		if (connections.isLoggedIn(id))
			send(new ERRORPacket(7, "user already logged in"));
		else if (connections.isUsernameExists(message.username()))
			send(new ERRORPacket(7, "username already exists"));
		else {
			connections.login(id, message.username());
			send(new ACKPacket());
		}
	}

	private void DIRQProcess(DIRQPacket message) {
		String filesnames = "";
		File folder = new File("Files");
		File[] filesList = folder.listFiles();
		if (filesList.length == 0){		// if there are no files, send an empty packet
			DATAPacket dataPack = new DATAPacket((short)0, (short)1, new byte[0]);
			send(dataPack);
			waitingForACK = dataPack.blockNum();
		}
		else{
			for (int i = 0; i < filesList.length; i++) { // create a file list
				if (filesList[i].canRead())
					filesnames += filesList[i].getName() + '\0';
			}
			// and now I will make it into data packs and send it:
			try {
				sentDataList = createDataList(new ByteArrayInputStream(filesnames.getBytes("UTF-8")));
				DATAPacket dataPack = sentDataList.poll();
				send(dataPack);
				waitingForACK = dataPack.blockNum();
				// the rest of the process will take place in the ACKprocess
			} catch (IOException e) {
				send(new ERRORPacket(2, "an IO exception occured"));
			}
		}
	}

	private void ERRORProcess(ERRORPacket message) { // in that case, we reset the RRQ/WRQ process
		waitingForACK = -1; 
		waitingForData = -1;	
		try {
			Files.delete(Paths.get("tmp")); 
			if (writtenFile!=null)
				writtenFile.delete();
			}
		catch(IOException e){} // delete the written file and tmp folder if exists
	}

	private void DATAProcess(DATAPacket message) {
		if (waitingForData == message.blockNum()) try{ //if this is indeed the data packet we expected...
			FileOutputStream fos = new FileOutputStream(writtenFile, true);
			fos.write(message.data());
			fos.close();
			send(new ACKPacket(message.blockNum()));
			if (message.packetSize() == 512){ // that means it's not the last packet
				waitingForData++;
			}
			else if (message.packetSize() < 512){ // that means it's the last packet
				waitingForData = -1;
				Files.move(writtenFile.toPath(), Paths.get("Files" + File.separator + writtenFile.getName())); // move the finished file to Files folder
				try {Files.delete(Paths.get("tmp"));}	catch(IOException e){} // if the tmp folder is empty, delete it
				connections.broadcast(new BCASTPacket(true, writtenFile.getName()));
				writtenFile = null;
			}
			else			
				send(new ERRORPacket(4, "DATA packet is illegaly big!"));
		}
		catch (IOException e) { send(new ERRORPacket(2, "an unexpected IO exception occured")); }
		else
			send(new ERRORPacket(4, "an unexpected DATA packet " + message.blockNum() + " arrived. expected DATA number: "+waitingForData));
	}

	private void ACKProcess(ACKPacket message) { // this process is mostly a continuation of the RRQ process
		if (waitingForACK == message.BlockNum()) { // if this is indeed the packet we expected we send the next data packet of the process
			if (!sentDataList.isEmpty()) {
				DATAPacket dataPack = sentDataList.poll();
				send(dataPack);
				waitingForACK = dataPack.blockNum();
			} else{
				waitingForACK = -1; // if we sent all the data packets we are not waiting for an ACK anymore
			}
		} else
			send(new ERRORPacket(0, "unexpected ACKPACKET. block # " + waitingForACK + " was expected"));
	}

	private void WRQProcess(WRQPacket message) {
		File file = stringToTempFile(message.filename());
		try {
			if (new File("Files" , file.getName()).exists())
				send(new ERRORPacket(5, "file already exists!"));
			else if (!file.createNewFile())
				send (new ERRORPacket(5, "Another user is currently writing a file with the same name"));
			else {
				waitingForData = 1;
				writtenFile = file;
				send(new ACKPacket());
				// the rest of the process will take place in the DATAprocess
			}
		}
		catch (IOException e) { send(new ERRORPacket(2, "an unexpected IO exception occured")); }
	}

	private void RRQProcess(RRQPacket message) {
		File file = stringToFile(message.filename());
		if (isFileExists(file)) {
			try {
				sentDataList = createDataList(new ByteArrayInputStream(Files.readAllBytes(file.toPath())));
				DATAPacket dataPack = sentDataList.poll();
				send(dataPack); // send the first packet
				waitingForACK = dataPack.blockNum(); //update to wait for the according ACK
				// the rest of the process will take place in the ACKprocess
			} catch (IOException e) {
				send(new ERRORPacket(2, "an IO exception occured"));
			}
		} else {
			send(new ERRORPacket(1, "required file not found"));
		}
	}

	/** helping functions: **/

	/**
	 * Creates a temporary file in the "tmp" folder.
	 * @param filename: The name of the file to create
	 * @return: the requested file
	 */
	private File stringToTempFile(String filename) {
		if (!Files.exists(Paths.get("tmp")))
			new File("tmp").mkdir(); // This is the folder where we will handle our new files before they are ready
		return (new File("tmp", filename)); // IMPORTANT: REMEMBER TO FIX
	}
	
	/**
	 * creates a file object of the "Files" folder for searching
	 * @param filename the name of the file
	 * @return the file
	 */
	private File stringToFile(String filename) {
		return (new File("Files", filename)); // IMPORTANT: REMEMBER TO FIX
	}

	private boolean isFileExists(File f) {
		return (f.exists());
	}

	private void send(Packet pack) {
		connections.send(id, pack);
	}

	/**
	 * Creates a list of up to 512-byte size packets from a ByteArrayInputStream
	 * @param reader an input stream the data of the packets would be collected from 
	 * @return a list of data packets, each of 512 bytes except the last one
	 * @throws IOException if a problem occurs with the reader
	 */
	private LinkedList<DATAPacket> createDataList(ByteArrayInputStream reader) throws IOException {
		LinkedList<DATAPacket> dataList = new LinkedList<DATAPacket>();
		short read;
		short block = 1;
		do { // move the data to chunks of up to 512 bytes each and place
				// them in the data queue
			byte[] dataChunk = new byte[512];
			read = (short) reader.read(dataChunk);
			if(read==-1) 
				read = 0;
			dataList.add(new DATAPacket(read, block++, dataChunk));
		} while (read == 512);
		// I will now fix the size of the last data chunk:
		if (read > 0){
			byte[] lastChunk = new byte[read];
			for (int j = 0; j < lastChunk.length; j++) {
				lastChunk[j] = dataList.peekLast().data()[j];
			}
			dataList.peekLast().setData(lastChunk);
		}
		else
			dataList.peekLast().setData(new byte[0]);			
		return dataList;
}
	

	@Override
	public boolean shouldTerminate() {
		return shouldTerminate;
	}

}
