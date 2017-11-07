package bgu.spl171.net.srv;

import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import java.util.function.Supplier;

public class ServerImpl<T> extends BaseServer<T> {

    public ServerImpl(
            int port,
            Supplier<BidiMessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> encdecFactory) {
		super(port, protocolFactory, encdecFactory);
    }

    protected void execute(BlockingConnectionHandler<T>  handler){
    	Thread thread = new Thread(handler);
    	getConnections().connect(handler, handler.getId());
    	thread.start();
    }

}
