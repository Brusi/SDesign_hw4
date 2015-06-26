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
import org.junit.rules.Timeout;

public class IntegrationTestGetClients {
	
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
	public void exceptionThrownWhenRoomDoesNotExist() throws AlreadyInRoomException, NoSuchRoomException {
		ClientChatApplication client1 = buildClient("David");
		
		exception.expect(NoSuchRoomException.class);
		client1.getClientsInRoom("someRoom");
	}
	
	@Test
	public void exceptionThrownWhenAllClientsLeftRoom() throws AlreadyInRoomException, NoSuchRoomException, NotInRoomException {
		ClientChatApplication client1 = buildClient("David");
		ClientChatApplication client2 = buildClient("Shaul");
		
		client1.joinRoom("Kings");
		client2.joinRoom("Kings");
		assertEquals(2, client1.getClientsInRoom("Kings").size());
		client1.leaveRoom("Kings");
		client2.leaveRoom("Kings");
		
		exception.expect(NoSuchRoomException.class);
		client1.getClientsInRoom("Kings");
	}
	
	@Test
	public void exceptionThrownWhenAllClientsDisconnected() throws AlreadyInRoomException, NoSuchRoomException, NotInRoomException {
		ClientChatApplication client1 = buildClient("David");
		ClientChatApplication client2 = buildClient("Shaul");
		
		client1.joinRoom("Kings");
		client2.joinRoom("Kings");
		assertEquals(2, client1.getClientsInRoom("Kings").size());
		client1.logout();
		client2.logout();
		
		ClientChatApplication client3 = buildClient("Nobody");
		
		exception.expect(NoSuchRoomException.class);
		client3.getClientsInRoom("Kings");
	}
	
	@Test
	public void testGetClients() throws AlreadyInRoomException, NoSuchRoomException { 
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
			List<String> kings = client1.getClientsInRoom("Kings");
			assertEquals(2, kings.size());
			assertTrue(kings.containsAll(Arrays.asList("David", "Shaul")));
		}
		{
			List<String> redheads = client1.getClientsInRoom("Redheads");
			assertEquals(2, redheads.size());
			assertTrue(redheads.containsAll(Arrays.asList("David", "Ron")));
		}
	}
	
	@Test
	public void ThrowExceptionWhenAllClientsLeftRoom() throws AlreadyInRoomException, NotInRoomException, NoSuchRoomException { 
		ClientChatApplication client1 = buildClient("David"); 
		ClientChatApplication client2 = buildClient("Shaul");
		
		client1.joinRoom("Kings");
		client2.joinRoom("Kings");
		
		assertEquals(2, client1.getClientsInRoom("Kings").size());
		
		client1.leaveRoom("Kings");
		client2.leaveRoom("Kings");
		
		exception.expect(NoSuchRoomException.class);
		client1.getClientsInRoom("Kings");
	}
	
	@Test
	public void LeaveAndJoinAgain() throws AlreadyInRoomException, NotInRoomException, NoSuchRoomException { 
		ClientChatApplication client1 = buildClient("David"); 
		ClientChatApplication client2 = buildClient("Shaul");
		
		client1.joinRoom("Kings");
		client2.joinRoom("Kings");
		
		assertEquals(2, client1.getClientsInRoom("Kings").size());
		
		client1.leaveRoom("Kings");
		client2.leaveRoom("Kings");
		
		exception = ExpectedException.none();
		
		client1.joinRoom("Kings");
		client2.joinRoom("Kings");
		
		assertEquals(2, client1.getClientsInRoom("Kings").size());
	}
	
	@Test
	public void ThrowExceptionWhenAllClientsLoggedOut() throws AlreadyInRoomException, NotInRoomException, NoSuchRoomException { 
		ClientChatApplication client1 = buildClient("David"); 
		ClientChatApplication client2 = buildClient("Shaul");
		
		client1.joinRoom("Kings");
		client2.joinRoom("Kings");
		
		assertEquals(2, client1.getClientsInRoom("Kings").size());
		
		client1.logout();
		
		assertEquals(1, client2.getClientsInRoom("Kings").size());
		
		client2.logout();
		
		restartServer();  // Add a persistency factor because why not?
		
		ClientChatApplication neutralClient = buildClient("Nobody");
		
		exception.expect(NoSuchRoomException.class);
		neutralClient.getClientsInRoom("Kings");
	}
	
	@Test
	public void LogoutAndLogin() throws AlreadyInRoomException, NotInRoomException, NoSuchRoomException { 
		ClientChatApplication client1 = buildClient("David"); 
		ClientChatApplication client2 = buildClient("Shaul");
		
		client1.joinRoom("Kings");
		client2.joinRoom("Kings");
		
		assertEquals(2, client1.getClientsInRoom("Kings").size());
		
		client1.logout();
		
		assertEquals(1, client2.getClientsInRoom("Kings").size());
		
		client2.logout();
		
		restartServer();  // Add a persistency factor because why not?

		ClientChatApplication neutralClient = buildClient("Nobody");

		loginClient(client1, "David");
		assertEquals(1, client1.getClientsInRoom("Kings").size());
		loginClient(client2, "Shaul");
		assertEquals(2, client1.getClientsInRoom("Kings").size());
	}
}
