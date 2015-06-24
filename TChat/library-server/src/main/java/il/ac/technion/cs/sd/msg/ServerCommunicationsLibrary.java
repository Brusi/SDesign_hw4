package il.ac.technion.cs.sd.msg;

import il.ac.technion.cs.sd.msg.Message.MessageType;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * server side of communications library.
 * Allows sending and receiving messages via communications library. 
 * Invokes the server side application when a new message arrives by polling 
 * a blocking queue of messages sent to the server. 
 */
public class ServerCommunicationsLibrary {
	private String serverAddress;
	private BlockingQueue<Message> requestQueue;
	private ReliableMessenger messenger;
	private BiConsumer<String, String> applicationAction;
	private volatile boolean isStopped;
	private Thread pollingThread;
	
	/**
	 *  A consumer that is passed to reliable messenger.
	 *  Will be invoked by reliable  messenger when it receives a message
	 */
	Consumer<Message> libraryActionOnReceive = new Consumer<Message>() {
		@Override
		public void accept(Message msg) {		
			if (!requestQueue.offer(msg)){
				//throw new RuntimeException("you succesfully created denial of service on server way to go asshole");
			}
		}
	};

	/**
	 * a thread that polls the blocking queue of received messages and invokes the
	 * consumer supplied by the application when there is a message to take from
	 * the queue 
	 */
	private Runnable serverPollingThread= ()->{
		while(!isStopped){
			try{
				Message msg= requestQueue.take();
				applicationAction.accept(msg.getFrom(), msg.getContent());
			}
			catch(Exception e){
			}
		}
	};
	
	/**
	 * creates a new instance of ServerCommunicationsLibrary
	 * @param serverAddress the address of the server
	 * @param action a consumer supplied by the application which handles the received messages
	 */
	public ServerCommunicationsLibrary(String serverAddress, BiConsumer<String, String> action) {
		this.serverAddress = serverAddress;
		applicationAction = action;
		requestQueue= new LinkedBlockingQueue<Message>();
		isStopped= true;
	}
	
	/**
	 * start the server library. initializes messenger and starts the thread that polls
	 * the request queue  
	 */
	public void start(){
		if(!isStopped){
			throw new RuntimeException("tried to start communications library while already running");
		}
		messenger = new ReliableMessenger(serverAddress, libraryActionOnReceive);
		isStopped= false;
		pollingThread= new Thread(serverPollingThread);
		pollingThread.start();
		return;
	}
	
	/**
	 * start the server library. initializes messenger and starts the thread that polls
	 * the request queue  
	 * @param messenger the serverLibrary will use this as the messenger 
	 */
	public void start(ReliableMessenger messenger){
		if(!isStopped){
			throw new RuntimeException("tried to start communications library while already running");
		}
		this.messenger = messenger;
		isStopped= false;
		pollingThread= new Thread(serverPollingThread);
		pollingThread.start();
		return;
	}
	/**
	 * stops the server library  and kills the messenger
	 */
	public void stop(){
		isStopped= true;
		messenger.kill();
		messenger= null;
	}
	
	/**
	 * 
	 * @return returns true if the server library is stopped
	 */
	public boolean isStopped(){
		return isStopped;
	}
	
	/**
	 * sends a message with message type NEW_MESSAGE. Send using this method
	 * when the client is not awaiting a reply message from another client or from 
	 * the server
	 * @param target target sends the message to this user
	 * @param payload the payload that is sent
	 */
	public void Send(String target, String payload) {
		if (isStopped){
			throw new RuntimeException("tried to send message while library stopped");
		}
		SendAux(target, payload, MessageType.NEW_MESSAGE);
	}
	
	/**
	 * sends a message with message type REPLIED_MESSAGE. Send using this method
	 * when the client is waiting for a reply message from another client or from 
	 * the server
	 * @param target sends the message to this user
	 * @param payload the payload that is sent
	 */
	public void SendReply(String target, String payload) {
		if (isStopped){
			throw new RuntimeException("tried to send message while library stopped");
		}
		SendAux(target, payload, MessageType.REPLIED_MESSAGE);
	}
	
	private void SendAux(String target, String payload, MessageType messageType) {
		if (payload == null){
			throw new IllegalArgumentException();
		}
		
		Message msg = new Message(serverAddress, payload, messageType);
		messenger.Send(target, msg);
	}
}
