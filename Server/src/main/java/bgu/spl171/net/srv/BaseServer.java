package bgu.spl171.net.srv;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.srv.bidi.ConnectionsImpl;

public abstract class BaseServer<T> implements Closeable, Server<T> {
 
    private final int port;
    private final Supplier<BidiMessagingProtocol<T>> protocolFactory;
    private final Supplier<MessageEncoderDecoder<T>> encdecFactory;
    private ServerSocket sock;
    private ConnectionsImpl<T> connections;
	int lastId = 0;
 
    public BaseServer(
            int port,
            Supplier<BidiMessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> encdecFactory
            ) {
 
    	connections = new ConnectionsImpl<>();
        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
        this.sock = null;
    }
 
	public void serve() {
		 
        try (ServerSocket serverSock = new ServerSocket(port)) {
 
            this.sock = serverSock; //just to be able to close
 
            while (!Thread.currentThread().isInterrupted()) {
 
                Socket clientSock = serverSock.accept();
 
                BlockingConnectionHandler<T> handler = new BlockingConnectionHandler<>(
                        clientSock,
                        encdecFactory.get(),
                        protocolFactory.get(),
                		connections,
                		lastId++)
                		;
 
                execute(handler);
            }
        } catch (IOException ex) {
        }
 
        System.out.println("server closed!!!");
    }
 
    @Override
    public void close() throws IOException {
        if (sock != null)
            sock.close();
    }
 
    protected abstract void execute(BlockingConnectionHandler<T>  handler);
 
 
  //For thread per client implementation:
 
    public static <T> BaseServer<T> threadPerClient(
            int port,
            Supplier<BidiMessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> encoderDecoderFactory) {
 
        return new BaseServer<T>(port, protocolFactory, encoderDecoderFactory) {
            @Override
            protected void execute(BlockingConnectionHandler<T> handler) {
                new Thread(handler).start();
            }
        };
 
    }
    
    public ConnectionsImpl<T> getConnections(){
    	return connections;
    }
}