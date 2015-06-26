package il.ac.technion.cs.sd.app.chat;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.app.chat.RoomAnnouncement.Announcement;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class IntegrationTestPersistency {
	
	private static final String serverAddress = "servAddr";
	
	private ServerChatApplication server = new ServerChatApplication(serverAddress);
	
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
	
	private void loginClient(ClientChatApplication client, String name) {
		client.login(msg -> messages.get(name).add(msg),
				announcement -> announcements.get(name).add(announcement));
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

	private void restartServer() {
		server.stop();
		server = new ServerChatApplication(serverAddress);
		server.start();
	}
	
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void serverRemembersClientsRooms() throws AlreadyInRoomException, InterruptedException, NotInRoomException, NoSuchRoomException {
		ClientChatApplication client1 = buildClient("David"); 
		ClientChatApplication client2 = buildClient("Shaul");
		
		client1.joinRoom("Kings");
		client1.joinRoom("Redheads");
		client2.joinRoom("Kings");
		
		client1.logout();
		client2.logout();
		
		restartServer();
		
		Thread.sleep(100);
		
		loginClient(client1, "David");
		loginClient(client2, "Shaul");
		
		List<String> kings = client1.getClientsInRoom("Kings");
		assertEquals(2, kings.size());
		assertTrue(kings.containsAll(Arrays.asList("David", "Shaul")));
		
		List<String> rooms1 = client1.getJoinedRooms();
		assertEquals(2, rooms1.size());
		assertTrue(rooms1.containsAll(Arrays.asList("Kings", "Redheads")));
		
		List<String> rooms2 = client2.getJoinedRooms();
		assertEquals(1, rooms2.size());
		assertTrue(rooms2.containsAll(Arrays.asList("Kings")));
	}
}
