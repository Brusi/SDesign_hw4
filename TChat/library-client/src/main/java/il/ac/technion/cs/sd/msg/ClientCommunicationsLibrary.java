package il.ac.technion.cs.sd.msg;

import il.ac.technion.cs.sd.msg.Message.MessageType;
import java.util.function.Consumer;

/**
 * The client side of the communication library. 
 * Allows sending and getting messages to and from other clients using a given server. <br>
 */
public class ClientCommunicationsLibrary {
	private String serverAddress;
	private String username;
	private ReliableMessenger messenger;
	
	private Consumer<String> applicationAction;
	
	// will be forwarded to the ReliableMessenger. This will be called in a new thread.
	Consumer<Message> libraryActionOnReceive = new Consumer<Message>() {
		
		@Override
		public void accept(Message msg) {
			applicationAction.accept(msg.getContent());
		}
	};
	
	/**
	 * Creates a new instance of the ClientCommunicationsLibrary
	 * @param serverAddress - the address of the server
	 * @param username - the address of the client
	 * @param action - the action to take when a new message arrives. This will be called in a separate thread.
	 */
	public ClientCommunicationsLibrary(String serverAddress, String username, Consumer<String> action) {
		this.serverAddress = serverAddress;
		this.username = username;
		applicationAction = action;
		
		messenger = new ReliableMessenger(username, libraryActionOnReceive);
	}
	
	/**
	 * Creates a new instance of the ClientCommunicationsLibrary
	 * @param serverAddress - the address of the server
	 * @param username - the address of the client
	 * @param action - the action to take when a new message arrives. This will be called in a separate thread.
	 * @param messenger - a messenger to use for sending messages.
	 */
	public ClientCommunicationsLibrary(String serverAddress, String username, Consumer<String> action, ReliableMessenger messenger) {
		this.serverAddress = serverAddress;
		this.username = username;
		applicationAction = action;	
		this.messenger = messenger;
	}
	
	/**
	 * Sends a message to another client via the server
	 */
	public void Send(String payload) {
		SendAux(payload, MessageType.NEW_MESSAGE);
	}
	
	/**
	 * Sends a message to another client via the server when the server is expecting a reply
	 * to a previous message he sent.
	 */
	public void SendReply(String payload) {
		SendAux(payload, MessageType.REPLIED_MESSAGE);
	}
	
	private void SendAux(String payload, MessageType messageType) {
		if (payload == null){
			throw new IllegalArgumentException();
		}
		
		Message msg = new Message(username, payload, messageType);
		messenger.Send(serverAddress, msg);
	}
	
	/**
	 * Sends a message to another client via the server and wait for a reply from the server.
	 */
	public String SendAndAwaitReply(String payload) {
		if (payload == null){
			throw new IllegalArgumentException();
		}
		
		Message msg = new Message(username, payload, MessageType.NEW_MESSAGE);
		String reply = messenger.SendAndAwaitReply(serverAddress, msg);
		return reply;
	}
	
	/**
	 * Stops the communications library; cleaning any resources
	 */
	public void stop() {
		messenger.kill();
		messenger = null;
	}
	
	/**
	 * gets the address of the client
	 * @return the address
	 */
	public String getAddress() {
		return messenger.getAddress();
	}
}
	
