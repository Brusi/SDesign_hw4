package il.ac.technion.cs.sd.msg;

import il.ac.technion.cs.sd.msg.Message.MessageType;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ReliableMessenger {
	private Messenger messenger;
	private Consumer<Message> actionOnReceive;
	private String user;
	
	// all listened to incoming messages will be written here
	private final BlockingQueue<String>	incomingMessages	= new LinkedBlockingQueue<>();
	
	private final int retrySendMessageTimeoutInMs = 20;
	
	BiConsumer<Messenger, String> reliableAction = new BiConsumer<Messenger, String>() {
		@Override
		public void accept(Messenger m, String arg) {
			if (arg.equals("")){
				incomingMessages.add(arg);
			}
			else
			{
				Message msg = Message.FromJsonString(arg);
				
				try {
					m.send(msg.getFrom(), "");
				} catch (MessengerException e) {
					//e.printStackTrace();
					// sending ack cannot fail
					//throw new RuntimeException("Error sending ack to: " + msg.getFrom());
				}
				
				if (msg.getMessageType().equals(MessageType.REPLIED_MESSAGE)){
					incomingMessages.add(arg);
				}else if (msg.getMessageType().equals(MessageType.NEW_MESSAGE)){
					new Thread(() -> {actionOnReceive.accept(msg);}).start();
				}	
			}
		}
	};
	
	public ReliableMessenger(String user, Consumer<Message> action) {
		actionOnReceive = action;
		this.user = user;
		
		try {
			messenger = new MessengerFactory().start(user, reliableAction);
		} catch (MessengerException e) {
			//e.printStackTrace();
			System.out.println(e.getLocalizedMessage());
			throw new RuntimeException("Failed to initialize messenger");
		}
	}
	
	public void Send(String to, Message msg) {
		if (to == null || msg == null){
			throw new IllegalArgumentException();
		}
		
		String jsonMessage = msg.ToJsonString();
		
		String ack = null;
		while (ack == null){
			try {
				messenger.send(to, jsonMessage);
				ack = incomingMessages.poll(retrySendMessageTimeoutInMs, TimeUnit.MILLISECONDS);
			} catch (MessengerException e) {
				//System.out.println("Error sending mail to server content " + msg.getContent());
				//throw new RuntimeException("Error sending mail to server content " + msg.getContent());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		
		return;
	}
	
	public String SendAndAwaitReply(String to, Message msg) {
		if (to == null || msg == null){
			throw new IllegalArgumentException();
		}
		
		String jsonMessage = msg.ToJsonString();
		
		String ack = null;
		while (ack == null){
			try {
				messenger.send(to, jsonMessage);
				ack = incomingMessages.poll(retrySendMessageTimeoutInMs, TimeUnit.MILLISECONDS);
			} catch (MessengerException e) {
				//System.out.println("Error sending mail to server content " + msg.getContent());
				//throw new RuntimeException("Error sending mail to server content " + msg.getContent());
			} catch (InterruptedException e) { // TODO remove above...
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		
		String reply = null;
		try {
			reply = incomingMessages.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		Message repliedMsg = Message.FromJsonString(reply);
		return repliedMsg.getContent();
	}
	
	public String getAddress(){
		return user;
	}
	
	public void kill() {
		try {
			messenger.kill();
			messenger = null;
		} catch (MessengerException e) {
			//System.out.println("Error killing messenger");
		}
	}
}
