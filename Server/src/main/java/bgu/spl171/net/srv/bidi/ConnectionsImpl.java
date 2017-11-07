package bgu.spl171.net.srv.bidi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class ConnectionsImpl<T> implements Connections<T> {

	private HashMap<Integer, ConnectionHandler<T>> connectionList = new HashMap<>();
	private HashMap<Integer, String> nameList = new HashMap<>();

	@Override
	public boolean send(int connectionId, T msg) {
		if(connectionList.containsKey(connectionId)) {
			connectionList.get((Integer) connectionId).send(msg);
			return true;
		}
		return false;
	}

	@Override
	public void broadcast(T msg) {
		for (Iterator<Entry<Integer,ConnectionHandler<T>>> iterator = connectionList.entrySet().iterator(); iterator.hasNext();) {
			ConnectionHandler<T> connectionHandler = iterator.next().getValue();
			connectionHandler.send(msg);
		}
	}

	@Override
	public void disconnect(int connectionId) {
		nameList.remove(connectionId);
		connectionList.remove(connectionId);
	}

	public void connect(ConnectionHandler<T> connectionHandler, int id) {
		connectionList.put(id, connectionHandler);
	}

	public void login(int id, String name) {
		nameList.put((Integer) id, name);
	}

	public boolean isLoggedIn(int id) {
		return nameList.containsKey(id);
	}
	
	public boolean isUsernameExists (String username){
		return nameList.containsValue(username);
	}

}
