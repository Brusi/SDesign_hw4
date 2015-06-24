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
	private ServerCommunicationsLibrary serverLibrary= new ServerCommunicationsLibrary("server", x-> incomingMessages.add(x));

	@Before
	public void setUp(){
		if(serverLibrary.isStopped())
		serverLibrary.start(mockMessenger);
	}
	
	@Test(expected= RuntimeException.class)
	public void serverLibraryThrowsRuntimeExceptionWhenTryingToSendWhileItIsStopped() {
		serverLibrary.stop();
		serverLibrary.Send("client", "hello");
	}
	
	@Test(expected= RuntimeException.class)
	public void serverLibraryThrowsRuntimeExceptionWhenTryingToStartWhileAlreadyStarted() {
		serverLibrary.start();
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
		serverLibrary.Send("client", "hello");
		Mockito.verify(mockMessenger,Mockito.only()).Send("client", new Message("server", "hello", MessageType.NEW_MESSAGE));
	}
	
	@Test
	public void serverLibrarySendsCorrectRepliedMessageWhenSendReplyInvoked() {
		serverLibrary.SendReply("client", "hello");
		Mockito.verify(mockMessenger,Mockito.only()).Send("client", new Message("server", "hello", MessageType.REPLIED_MESSAGE));
	}
	

}