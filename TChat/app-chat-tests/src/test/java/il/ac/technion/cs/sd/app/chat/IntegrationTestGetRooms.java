package il.ac.technion.cs.sd.app.chat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
import org.junit.rules.Timeout;

public class IntegrationTestGetRooms {
	
	private static final String serverAddress = "servAddr";
	
	private ServerChatApplication server = new ServerChatApplication(serverAddress);
	
	private Map<String, BlockingQueue<RoomAnnouncement>> announcements = new HashMap<String, BlockingQueue<RoomAnnouncement>>();
	private Map<String, BlockingQueue<ChatMessage>> messages = new HashMap<String, BlockingQueue<ChatMessage>>();
	
	private Set<ClientChatApplication> clients = new HashSet<ClientChatApplication>();
	
	@Rule
    public Timeout globalTimeout = Timeout.seconds(5); // 10 seconds max per method tested
	
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
	public void testGetRooms() throws AlreadyInRoomException, NotInRoomException { 
		ClientChatApplication client1 = buildClient("David"); 
		ClientChatApplication client2 = buildClient("Shaul");
		ClientChatApplication client3 = buildClient("Ron");
		
		assertTrue(client1.getJoinedRooms().isEmpty());
		assertTrue(client2.getJoinedRooms().isEmpty());
		assertTrue(client3.getJoinedRooms().isEmpty());
		assertTrue(client3.getAllRooms().isEmpty());
		
		client1.joinRoom("Kings");
		client2.joinRoom("Kings");
		
		client1.joinRoom("Redheads");
		client3.joinRoom("Redheads");
		
		{
			List<String> rooms1 = client1.getJoinedRooms();
			assertEquals(2, rooms1.size());
			assertTrue(rooms1.containsAll(Arrays.asList("Kings", "Redheads")));
		}
		{
			List<String> rooms2 = client2.getJoinedRooms();
			assertEquals(1, rooms2.size());
			assertTrue(rooms2.containsAll(Arrays.asList("Kings")));
		}
		{
			List<String> rooms3 = client3.getJoinedRooms();
			assertEquals(1, rooms3.size());
			assertTrue(rooms3.containsAll(Arrays.asList("Redheads")));
		}
		
		ClientChatApplication client4 = buildClient("Harry");
		client4.joinRoom("Nobodies");
		{
			List<String> allRooms = client4.getAllRooms();
			assertEquals(3, allRooms.size());
			assertTrue(allRooms.containsAll(Arrays.asList("Redheads", "Kings", "Nobodies")));
		}
	}
	
	@Test
	public void LeaveAndJoinAgain() throws AlreadyInRoomException, NotInRoomException { 
		ClientChatApplication client1 = buildClient("David"); 
		ClientChatApplication client2 = buildClient("Shaul");
		
		client1.joinRoom("Kings");
		client2.joinRoom("Kings");
		
		client1.leaveRoom("Kings");
		client2.leaveRoom("Kings");
		
		assertTrue(client1.getAllRooms().isEmpty());
		assertTrue(client1.getJoinedRooms().isEmpty());
		assertTrue(client2.getJoinedRooms().isEmpty());
		
		client1.joinRoom("Kings");
		client2.joinRoom("Kings");
		
		assertEquals(1, client1.getJoinedRooms().size());
		assertEquals(1, client2.getJoinedRooms().size());
		assertEquals(1, client1.getAllRooms().size());
	}
	
	@Test
	public void LogoutAndLogin() throws AlreadyInRoomException, NotInRoomException { 
		ClientChatApplication client1 = buildClient("David"); 
		ClientChatApplication client2 = buildClient("Shaul");
		
		client1.joinRoom("Kings");
		client2.joinRoom("Kings");
		
		client1.logout();
		
		assertFalse(client2.getAllRooms().isEmpty());
		
		client2.logout();
		
		restartServer();  // Add a persistency factor because why not?
		
		ClientChatApplication neutralClient = buildClient("Nobody");
		
		assertTrue(neutralClient.getAllRooms().isEmpty());
		
		loginClient(client1, "David");
		assertEquals(1, client1.getJoinedRooms().size());
		assertEquals("Kings", client1.getJoinedRooms().get(0));
		assertEquals(1, client1.getAllRooms().size());
		assertEquals("Kings", client1.getAllRooms().get(0));
	}
}
