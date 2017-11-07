package bgu.spl171.net.impl.TFTPreactor;

import java.io.IOException;

import bgu.spl171.net.impl.packets.Packet;
import bgu.spl171.net.impl.packets.PacketEncoderDecoder;
import bgu.spl171.net.impl.packets.PacketsProtocol;
import bgu.spl171.net.srv.Reactor;

public class ReactorMain {
	public static void main(String[] args) {
		int port;
		if (args.length>0){
			try{
				port = Integer.parseInt(args[0]);
				Reactor<Packet> reactor = new Reactor<Packet>(4, port,() -> new PacketsProtocol(),PacketEncoderDecoder::new );
				reactor.serve();

				reactor.close();
			}catch(NumberFormatException e){
				System.out.println("			bad input");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
