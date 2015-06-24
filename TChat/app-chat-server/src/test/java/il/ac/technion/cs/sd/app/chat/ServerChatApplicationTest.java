package il.ac.technion.cs.sd.app.chat;

import static org.junit.Assert.*;

import java.util.function.BiConsumer;

import il.ac.technion.cs.sd.app.chat.RoomAnnouncement.Announcement;
import il.ac.technion.cs.sd.app.chat.exchange.AnnouncementRequest;
import il.ac.technion.cs.sd.app.chat.exchange.ConnectRequest;
import il.ac.technion.cs.sd.app.chat.exchange.Exchange;
import il.ac.technion.cs.sd.app.chat.exchange.JoinRoomRequest;
import il.ac.technion.cs.sd.msg.ServerCommunicationsLibrary;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ServerChatApplicationTest {
	
	private static final String serverAddress = "ServerAddress";

	ServerChatApplication server;
	
	BiConsumer<String, String> serverConsumer;
	ServerCommunicationsLibrary connection;
	
	private static Codec<Exchange> codec = new XStreamCodec<Exchange>();

	@Before
	public void setUp() throws Exception {
		connection = Mockito.mock(ServerCommunicationsLibrary.class);
		server = new ServerChatApplication(serverAddress);
		
		// Get the client's consumer sent to connection's start method.
		Mockito.doAnswer(invocation -> {
			serverConsumer = (BiConsumer<String, String>) invocation.getArguments()[0];
			return null;
		}).when(connection).start(Mockito.any());
		
		server.startWithMockConnection(connection);
		Mockito.verify(connection).start(Mockito.any());
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
		server.clean();
		Mockito.verify(connection).stop();
	}
	
	private void sendToServer(String sender, Exchange exchange) {
		String payload = codec.encode(exchange);
		serverConsumer.accept(sender, payload);
	}
	
	@Test
	public void testClientLoginRoomAnnouncements() {
		sendToServer("David", new ConnectRequest());
		sendToServer("Shaul", new ConnectRequest());
		
		sendToServer("David", new JoinRoomRequest("Kings"));
		sendToServer("Shaul", new JoinRoomRequest("Kings"));

		String expected = codec.encode(new AnnouncementRequest(
				new RoomAnnouncement("Shaul", "Kings", Announcement.JOIN)));
		Mockito.verify(connection).Send("David", expected);
	}

}
