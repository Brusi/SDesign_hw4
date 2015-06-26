package il.ac.technion.cs.sd.app.chat;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.app.chat.RoomAnnouncement.Announcement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IntegrationTestAnnouncements {
	
	private ServerChatApplication server = new ServerChatApplication("Server");
	
	private Map<String, BlockingQueue<RoomAnnouncement>> announcements = new HashMap<String, BlockingQueue<RoomAnnouncement>>();
	private Map<String, BlockingQueue<ChatMessage>> messages = new HashMap<String, BlockingQueue<ChatMessage>>();
	
	private Set<ClientChatApplication> clients = new HashSet<ClientChatApplication>();
	
	private ClientChatApplication buildClient(String login) {
		ClientChatApplication $ = new ClientChatApplication(server.getAddress(), login);
		announcements.put(login, new LinkedBlockingQueue<RoomAnnouncement>());
		messages.put(login, new LinkedBlockingQueue<ChatMessage>());
		
		$.login(msg -> messages.get(login).add(msg),
				announcement -> announcements.get(login).add(announcement));
		
		clients.add($);
		
		return $;
		
	}
	
	@Before
	public void setp() {
		server.start(); // non-blocking
	}
	
	@After
	public void teardown() {
		server.stop();
		server.clean();
		
		for (ClientChatApplication client : clients) {
			client.stop();
		}
	}

	@Test
	public void joinRoomAnnouncement() throws AlreadyInRoomException, InterruptedException {
		ClientChatApplication client1 = buildClient("David"); 
		ClientChatApplication client2 = buildClient("Shaul");
		
		client1.joinRoom("Kings");
		client2.joinRoom("Kings");
		
		assertEquals(new RoomAnnouncement("Shaul", "Kings", Announcement.JOIN),
				announcements.get("David").take());
	}
	
	@Test
	public void leaveRoomAnnouncement() throws AlreadyInRoomException, InterruptedException, NotInRoomException {
		ClientChatApplication client1 = buildClient("David"); 
		ClientChatApplication client2 = buildClient("Shaul");
		
		client1.joinRoom("Kings");
		client2.joinRoom("Kings");
		client2.leaveRoom("Kings");
		
		assertEquals(new RoomAnnouncement("Shaul", "Kings", Announcement.JOIN),
				announcements.get("David").take());
		assertEquals(new RoomAnnouncement("Shaul", "Kings", Announcement.LEAVE),
				announcements.get("David").take());
	}
	
	@Test
	public void disconnectRoomAnnouncement() throws AlreadyInRoomException, InterruptedException {
		ClientChatApplication client1 = buildClient("David"); 
		ClientChatApplication client2 = buildClient("Shaul");
		
		client1.joinRoom("Kings");
		client2.joinRoom("Kings");
		client2.logout();
		
		assertEquals(new RoomAnnouncement("Shaul", "Kings", Announcement.JOIN),
				announcements.get("David").take());
		assertEquals(new RoomAnnouncement("Shaul", "Kings", Announcement.DISCONNECT),
				announcements.get("David").take());
	}
	
	@Test
	public void checkThatAnnouncementNotSentToSelf() throws AlreadyInRoomException, InterruptedException, NotInRoomException {
		ClientChatApplication client1 = buildClient("David"); 
		ClientChatApplication client2 = buildClient("Shaul");
		
		client1.joinRoom("Kings");
		
		client2.joinRoom("Kings");
		client2.leaveRoom("Kings");
		client2.joinRoom("Kings");
		client2.logout();
		
		assertTrue(announcements.get("Shaul").isEmpty());
	}
}
