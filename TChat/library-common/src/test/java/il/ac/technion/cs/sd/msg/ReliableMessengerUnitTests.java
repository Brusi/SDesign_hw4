package il.ac.technion.cs.sd.msg;

import static org.junit.Assert.assertEquals;
import il.ac.technion.cs.sd.msg.Message.MessageType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Test;

public class ReliableMessengerUnitTests {
	// a helper method and list for creating messengers, so we can remember to kill them
	private final Collection<ReliableMessenger>	messengers			= new ArrayList<>();
	
	// all listened to incoming messages will be written here
	private final BlockingQueue<Message>	incomingMessages	= new LinkedBlockingQueue<>();
	
	private ReliableMessenger mForReply;
	
	private ReliableMessenger startAndAddToList() throws Exception {
		return startAndAddToList(messengers.size() + "", x -> incomingMessages.add(x));
	}
	
	private ReliableMessenger startAndAddToList(Consumer<Message> c) throws Exception {
		return startAndAddToList(messengers.size() + "",c);
	}
	
	private ReliableMessenger startAndAddToList(String address, Consumer<Message> c) throws Exception {
		ReliableMessenger $ = new ReliableMessenger(address, c);
		messengers.add($);
		return $;
	}
	
	@After
	public void teardown() throws Exception {
		// it is very important to kill all messengers,
		// to free their address and more importantly, their daemon threads
		incomingMessages.clear();
		for (ReliableMessenger m: messengers)
			try {
				m.kill();
			} catch (Exception e) {/* do nothing */}
	}
	
	@Test(timeout=10000)
	public void sendNeverFailsToSendMessages() throws Exception {
		ReliableMessenger m1 = startAndAddToList();
		ReliableMessenger m2 = startAndAddToList();
		int num_tries = 200; // the odds of this test failing are lower than you winning the lottery
		for (int i = 0; i < num_tries; i++){
			Message msg = new Message(m1.getAddress(), "Message #" + i, MessageType.NEW_MESSAGE);
			m1.Send(m2.getAddress(), msg);
		}
		
		Thread.sleep(10);
		
		assertEquals(incomingMessages.size(), num_tries);
	}
	
	@Test(timeout=1000)
	public void clientLibraryForwardsNewMessagesFromServerToSuppliedConsumer() throws Exception {
		ReliableMessenger m1 = startAndAddToList();
		ReliableMessenger m2 = startAndAddToList();

		Message msg = new Message(m1.getAddress(), "Hi", MessageType.NEW_MESSAGE);
		m1.Send(m2.getAddress(), msg);
		
		assertEquals(msg, incomingMessages.take());
	}
	
	@Test(expected= IllegalArgumentException.class)
	public void SendFailsOnInvalidTo() throws Exception {
		ReliableMessenger m1 = startAndAddToList();
		Message msg = new Message(m1.getAddress(), "Hi", MessageType.NEW_MESSAGE);
		m1.Send(null, msg);
	}
	
	@Test(expected= IllegalArgumentException.class)
	public void SendFailsOnInvalidMsg() throws Exception {
		ReliableMessenger m1 = startAndAddToList();
		m1.Send("someone", null);
	}
	
	@Test(timeout=1000)
	public void sendAndAwaitReplyWaitsForServerReply() throws Exception {
		ReliableMessenger m1 = startAndAddToList(x -> incomingMessages.add(x));
		mForReply = startAndAddToList(new Consumer<Message>() {
			
			@Override
			public void accept(Message arg0) {
				Message msg = new Message(mForReply.getAddress(), "boo", MessageType.REPLIED_MESSAGE);
				new Thread(() -> {mForReply.Send(m1.getAddress(), msg);}).start();
			}
		});

		Message msg = new Message(m1.getAddress(), "Hi", MessageType.NEW_MESSAGE);
		String reply = m1.SendAndAwaitReply(mForReply.getAddress(), msg);
		assertEquals("boo", reply);
	}
}
