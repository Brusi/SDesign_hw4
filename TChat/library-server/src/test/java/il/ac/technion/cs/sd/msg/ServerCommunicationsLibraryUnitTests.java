package il.ac.technion.cs.sd.msg;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.msg.Message.MessageType;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ServerCommunicationsLibraryUnitTests {

	private final BlockingQueue<String>	incomingMessages	= new LinkedBlockingQueue<>();
	private ReliableMessenger mockMessenger= Mockito.mock(ReliableMessenger.class);
	private ServerCommunicationsLibrary serverLibrary= new ServerCommunicationsLibrary("server");

	@Before
	public void setUp(){
		if(serverLibrary.isStopped())
		serverLibrary.startWithMockMessenger(mockMessenger);
	}
	
	@Test(expected= RuntimeException.class)
	public void serverLibraryThrowsRuntimeExceptionWhenTryingToSendWhileItIsStopped() {
		serverLibrary.stop();
		serverLibrary.send("client", "hello");
	}
	
	@Test(expected= RuntimeException.class)
	public void serverLibraryThrowsRuntimeExceptionWhenTryingToStartWhileAlreadyStarted() {
		serverLibrary.start((sender, x)-> incomingMessages.add(x));
	}
	
	@Test
	public void stopLibraryKillsMessenger() throws Exception {
		serverLibrary.stop();
		Mockito.verify(mockMessenger, Mockito.only()).kill();
	}
	
	@Test
	public void startedLibraryReturnsIsStoppedAsFalse() throws Exception {
		assertFalse(serverLibrary.isStopped());
	}
	
	@Test
	public void stoppedLibraryReturnsIsStoppedAsTrue() throws Exception {
		serverLibrary.stop();
		assertTrue(serverLibrary.isStopped());
	}
	
	@Test
	public void serverLibrarySendsCorrectNewMessageWhenSendInvoked() {
		serverLibrary.send("client", "hello");
		Mockito.verify(mockMessenger,Mockito.only()).Send("client", new Message("server", "hello", MessageType.NEW_MESSAGE));
	}
	
	@Test
	public void serverLibrarySendsCorrectRepliedMessageWhenSendReplyInvoked() {
		serverLibrary.sendReply("client", "hello");
		Mockito.verify(mockMessenger,Mockito.only()).Send("client", new Message("server", "hello", MessageType.REPLIED_MESSAGE));
	}
	

}