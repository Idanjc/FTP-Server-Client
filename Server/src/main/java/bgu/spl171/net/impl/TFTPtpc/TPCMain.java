package bgu.spl171.net.impl.TFTPtpc;

import java.io.IOException;

import bgu.spl171.net.impl.packets.Packet;
import bgu.spl171.net.impl.packets.PacketEncoderDecoder;
import bgu.spl171.net.impl.packets.PacketsProtocol;
import bgu.spl171.net.srv.ServerImpl;

public class TPCMain {

	public static void main(String[] args) {
		int port;
		if (args.length>0){
			try{
				port = Integer.parseInt(args[0]);
				ServerImpl<Packet> server = new ServerImpl<Packet>(port,() -> new PacketsProtocol(),PacketEncoderDecoder::new );
				server.serve();

				server.close();
			}catch(NumberFormatException e){
				System.out.println("			bad input");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
