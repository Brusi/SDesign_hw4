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

public class IntegrationTestJoinLeave {
	
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
	public void ExceptionThrownWhenTryingToJoinTwice() throws AlreadyInRoomException {
		ClientChatApplication client1 = buildClient("David"); 
		
		client1.joinRoom("Kings");
		exception.expect(AlreadyInRoomException.class);
		client1.joinRoom("Kings");
	}
	
	@Test
	public void ExceptionThrownWhenTryingToLeaveARoomWeDidNotJoin() throws NotInRoomException {
		ClientChatApplication client1 = buildClient("David"); 
		
		exception.expect(NotInRoomException.class);
		client1.leaveRoom("Kings");
	}
	
	@Test
	public void ExceptionThrownWhenTryingToLeaveARoomTwice() throws NotInRoomException, AlreadyInRoomException {
		ClientChatApplication client1 = buildClient("David"); 
		
		client1.joinRoom("Kings");
		client1.leaveRoom("Kings");
		exception.expect(NotInRoomException.class);
		client1.leaveRoom("Kings");
	}
}
