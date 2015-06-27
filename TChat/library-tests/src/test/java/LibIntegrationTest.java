import static org.junit.Assert.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import il.ac.technion.cs.sd.msg.ClientCommunicationsLibrary;
import il.ac.technion.cs.sd.msg.ServerCommunicationsLibrary;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class LibIntegrationTest {
	
	private static final String serverAddress = "serverAddr";
	private static final String clientAddress = "clientAddr";
	
	BlockingQueue<String> serverMsgs = new LinkedBlockingQueue<String>();
	BlockingQueue<String> clientMsgs = new LinkedBlockingQueue<String>();
	
	ServerCommunicationsLibrary server;
	ClientCommunicationsLibrary client;
	

	@Before
	public void setUp() throws Exception {
		server = new ServerCommunicationsLibrary(serverAddress);
		server.start((sender, msg) -> serverMsgs.add(msg));
		client = new ClientCommunicationsLibrary(serverAddress, clientAddress,
				s -> clientMsgs.add(s));
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
		client.stop();
	}

	@Test
	public void testClientToServer() throws InterruptedException {
		client.send("Hello!");
		assertEquals("Hello!", serverMsgs.take());
	}
	
	@Test
	public void testServerToClient() throws InterruptedException {
		server.send(clientAddress, "Hi!");
		assertEquals("Hi!", clientMsgs.take());
	}
	
//	@Test
//	public void testWaitForReply() throws InterruptedException {
//		new Thread(() -> {
//			try {
//				Thread.sleep(400);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			server.sendReply(clientAddress, "Blue");
//		}).start();
//		assertEquals("Blue", client.sendAndAwaitReply("What is the color of the sky?"));
//	}

}
