package il.ac.technion.cs.sd.msg;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.msg.Message.MessageType;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ClientCommunicationsLibraryUnitTests {

	private final BlockingQueue<String>	incomingMessages	= new LinkedBlockingQueue<>();

	private ReliableMessenger mockMessenger = Mockito.mock(ReliableMessenger.class);
	private ClientCommunicationsLibrary clientLibrary = new ClientCommunicationsLibrary("server", "client", x -> incomingMessages.add(x) , mockMessenger);
	
	@Before
	public void init() throws Exception {
		incomingMessages.clear();
	}
	
	@Test
	public void clientLibrarySendsMessagesToSuppliedServerAddressWithCorrectMessage() throws Exception {
		clientLibrary.Send("Hi");
		Mockito.verify(mockMessenger, Mockito.only()).Send("server", new Message("client", "Hi", MessageType.NEW_MESSAGE));
	}
	
	@Test(expected= IllegalArgumentException.class)
	public void SendFailsOnInvaliPayload() throws Exception {
		clientLibrary.Send(null);
	}
	
	@Test(expected= IllegalArgumentException.class)
	public void SendAndAwaitReplyFailsOnInvaliPayload() throws Exception {
		clientLibrary.SendAndAwaitReply(null);
	}
	
	@Test
	public void stopLibraryKillsMessenger() throws Exception {
		clientLibrary.stop();
		Mockito.verify(mockMessenger, Mockito.only()).kill();
	}
	
	@Test
	public void getAddressReturnsCorrectAddress() throws Exception {
		Mockito.when(mockMessenger.getAddress()).thenReturn("client");
		assertEquals("client", clientLibrary.getAddress());
	}
	
	@Test
	public void sendAndAwaitReplyReturnsMessageContents() throws Exception {
		Mockito.when(mockMessenger.SendAndAwaitReply("server", new Message("client", "Hi", MessageType.NEW_MESSAGE))).thenReturn("result");
		assertEquals("result", clientLibrary.SendAndAwaitReply("Hi"));
	}
}