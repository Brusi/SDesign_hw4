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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class IntegrationTestMessages {
	
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
	
	
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void messageSendInRoom() throws AlreadyInRoomException, InterruptedException, NotInRoomException {
		ClientChatApplication client1 = buildClient("David"); 
		ClientChatApplication client2 = buildClient("Shaul");
		ClientChatApplication client3 = buildClient("Shlomo");
		
		client1.joinRoom("Kings");
		client2.joinRoom("Kings");
		client3.joinRoom("Kings");
		
		client1.sendMessage("Kings", "Hello there");
		
		ChatMessage expected = new ChatMessage("David", "Kings", "Hello there"); 
		
		assertEquals(expected, announcements.get("Shaul").take());
		assertEquals(expected, announcements.get("Shlomo").take());
	}
	
	@Test
	public void messageSendOnlyToClientsInRoom() throws AlreadyInRoomException, NotInRoomException, InterruptedException {
		ClientChatApplication client1 = buildClient("David"); 
		ClientChatApplication client2 = buildClient("Shaul");
		ClientChatApplication client3 = buildClient("Shmuel");
		
		client1.joinRoom("Kings");
		client2.joinRoom("Kings");
		client3.joinRoom("Prophets");
		
		client1.sendMessage("Kings", "Hello only kings!");
		
		ChatMessage expected = new ChatMessage("David", "Kings", "Hello only kings!"); 
		
		assertEquals(expected, announcements.get("Shaul").take());
		assertTrue(announcements.get("Shmuel").isEmpty());
	}
	
	@Test
	public void exceptionThrownWhenSendingMessageToUnjoinedRoom() throws AlreadyInRoomException, NotInRoomException {
		ClientChatApplication client1 = buildClient("David"); 
		ClientChatApplication client2 = buildClient("Shaul");
		ClientChatApplication client3 = buildClient("Shmuel");
		
		client1.joinRoom("Kings");
		client2.joinRoom("Kings");
		client3.joinRoom("Prophets");
		
		exception.expect(NotInRoomException.class);
		client3.sendMessage("Kings", "But i'm not a king!");
	}
	
	@Test
	public void messageNotSentToClientsWhoLeftRoom() throws AlreadyInRoomException, NotInRoomException, InterruptedException {
		ClientChatApplication client1 = buildClient("David"); 
		ClientChatApplication client2 = buildClient("Shaul");
		
		client1.joinRoom("Kings");
		client2.joinRoom("Kings");
		
		client2.leaveRoom("Kings");
		
		client1.sendMessage("Kings", "Hello only kings!");
		
		assertTrue(messages.get("Shaul").isEmpty());
	}
	
	@Test
	public void messageNotSentToDisconnectedClients() throws AlreadyInRoomException, NotInRoomException, InterruptedException {
		ClientChatApplication client1 = buildClient("David"); 
		ClientChatApplication client2 = buildClient("Shaul");
		
		client1.joinRoom("Kings");
		client2.joinRoom("Kings");
		
		client2.logout();
		
		client1.sendMessage("Kings", "Hello only kings!");
		
		assertTrue(messages.get("Shaul").isEmpty());
	}
	
	
	@Test
	public void messageSentOnlyInRoom() throws AlreadyInRoomException, NotInRoomException, InterruptedException {
		ClientChatApplication client1 = buildClient("David"); 
		ClientChatApplication client2 = buildClient("Shaul");
		ClientChatApplication client3 = buildClient("Ron");
		
		client1.joinRoom("Kings");
		client2.joinRoom("Kings");
		
		client1.joinRoom("Redheads");
		client3.joinRoom("Redheads");
		
		
		client1.sendMessage("Kings", "Hello kings!");
		client1.sendMessage("Kings", "Hello redheads!");
		
		ChatMessage expected1 = new ChatMessage("David", "Kings", "Hello kings!"); 
		ChatMessage expected2 = new ChatMessage("David", "Redheads", "Hello redheads!");
		
		assertEquals(expected1, messages.get("Shaul").take());
		assertEquals(expected2, messages.get("Ron").take());
	}
}
