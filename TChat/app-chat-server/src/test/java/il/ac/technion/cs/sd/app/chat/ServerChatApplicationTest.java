package il.ac.technion.cs.sd.app.chat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import il.ac.technion.cs.sd.app.chat.RoomAnnouncement.Announcement;
import il.ac.technion.cs.sd.app.chat.exchange.*;
import il.ac.technion.cs.sd.msg.ServerCommunicationsLibrary;

import java.util.Arrays;
import java.util.function.BiConsumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class ServerChatApplicationTest {
	
	private static final String serverAddress = "ServerAddress";

	ServerChatApplication server;
	
	BiConsumer<String, String> serverConsumer;
	ServerCommunicationsLibrary connection;
	
	private static Codec<Exchange> codec = new XStreamCodec<Exchange>();

	@SuppressWarnings("unchecked")
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
		Mockito.verify(connection, Mockito.atLeastOnce()).stop();
	}
	
	private void sendToServer(String sender, Exchange exchange) {
		String payload = codec.encode(exchange);
		serverConsumer.accept(sender, payload);
	}
	
	private void restartServer() {
		server.stop();
		server = new ServerChatApplication(serverAddress);
		server.startWithMockConnection(connection);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void checkNameNotNull() {
		new ServerChatApplication(null); 
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void checkNameNotEmpty() {
		new ServerChatApplication(""); 
	}
	
	@Test
	public void testClientLoginRoomAnnouncements() throws InterruptedException {
		sendToServer("David", new ConnectRequest());
		sendToServer("Shaul", new ConnectRequest());
		
		sendToServer("David", new JoinRoomRequest("Kings"));
		sendToServer("Shaul", new JoinRoomRequest("Kings"));

		String expected = codec.encode(new AnnouncementRequest(
				new RoomAnnouncement("Shaul", "Kings", Announcement.JOIN)));
		Mockito.verify(connection).send("David", expected);
	}
	
	@Test
	public void testClientLogoutRoomAnnouncement() {
		sendToServer("David", new ConnectRequest());
		sendToServer("Shaul", new ConnectRequest());
		
		sendToServer("David", new JoinRoomRequest("Kings"));
		sendToServer("Shaul", new JoinRoomRequest("Kings"));
		sendToServer("David", new LeaveRoomRequest("Kings"));

		String expected = codec.encode(new AnnouncementRequest(
				new RoomAnnouncement("David", "Kings", Announcement.LEAVE)));
		Mockito.verify(connection).send("Shaul", expected);
	}
	
	@Test
	public void testClientDisconnectRoomAnnouncement() {
		sendToServer("David", new ConnectRequest());
		sendToServer("Shaul", new ConnectRequest());
		
		sendToServer("David", new JoinRoomRequest("Kings"));
		sendToServer("Shaul", new JoinRoomRequest("Kings"));
		sendToServer("David", new DisconnectRequest());

		String expected = codec.encode(new AnnouncementRequest(
				new RoomAnnouncement("David", "Kings", Announcement.DISCONNECT)));
		Mockito.verify(connection).send("Shaul", expected);
	}
	
	@Test
	public void testSendMessageInRoom() {
		sendToServer("David", new ConnectRequest());
		sendToServer("Shaul", new ConnectRequest());
		
		sendToServer("David", new JoinRoomRequest("Kings"));
		sendToServer("Shaul", new JoinRoomRequest("Kings"));
		
		Exchange request = new SendMessageRequest(new ChatMessage("Shaul", "Kings", "We kings!"));
		sendToServer("Shaul", request);

		String expected = codec.encode(request);
		Mockito.verify(connection).send("David", expected);
	}
	
	
	@Test
	public void MessageIsSentToSeveralClientsInRoom() {
		sendToServer("David", new ConnectRequest());
		sendToServer("Shaul", new ConnectRequest());
		sendToServer("Shlomo", new ConnectRequest());
		
		sendToServer("David", new JoinRoomRequest("Kings"));
		sendToServer("Shaul", new JoinRoomRequest("Kings"));
		sendToServer("Shlomo", new JoinRoomRequest("Kings"));
		
		sendToServer("Shaul", new SendMessageRequest(new ChatMessage("Shaul", "Kings", "We kings!")));

		Exchange request = new SendMessageRequest(new ChatMessage("Shaul", "Kings", "We kings!"));
		sendToServer("Shaul", request);

		String expected = codec.encode(request);
		Mockito.verify(connection, Mockito.atLeastOnce()).send("David", expected);
		Mockito.verify(connection, Mockito.atLeastOnce()).send("Shlomo", expected);
	}
	
	@Test
	public void AnnouncementIsSentToSeveralClientsInRoom() {
		sendToServer("David", new ConnectRequest());
		sendToServer("Shaul", new ConnectRequest());
		sendToServer("Shlomo", new ConnectRequest());
		
		sendToServer("David", new JoinRoomRequest("Kings"));
		sendToServer("Shaul", new JoinRoomRequest("Kings"));
		sendToServer("Shlomo", new JoinRoomRequest("Kings"));
		
		String expected = codec.encode(new AnnouncementRequest(
				new RoomAnnouncement("Shlomo", "Kings", Announcement.JOIN)));
		Mockito.verify(connection, Mockito.atLeastOnce()).send("David", expected);
		Mockito.verify(connection, Mockito.atLeastOnce()).send("Shaul", expected);
	}
	
	@Test
	public void CannotLeaveARoomWithoutJoiningFirst() {
		sendToServer("David", new ConnectRequest());
	
		sendToServer("David", new LeaveRoomRequest("Kings"));
		Mockito.verify(connection).send("David", codec.encode(OperationResponse.FAILURE));
	}
	
	@Test
	public void CannotJoinARoomTwice() {
		sendToServer("David", new ConnectRequest());
		
		// Succeed to join at first.
		sendToServer("David", new JoinRoomRequest("Kings"));
		Mockito.verify(connection).send("David", codec.encode(OperationResponse.SUCCESS));
		
		// Don't succeed to join to a room you are already in.
		sendToServer("David", new JoinRoomRequest("Kings"));
		Mockito.verify(connection).send("David", codec.encode(OperationResponse.FAILURE));
	}
	
	@Test
	public void CannotLeaveARoomTwice() {
		sendToServer("David", new ConnectRequest());
		
		sendToServer("David", new JoinRoomRequest("Kings"));
		sendToServer("David", new LeaveRoomRequest("Kings"));
		Mockito.verify(connection, Mockito.times(2)).send("David", codec.encode(OperationResponse.SUCCESS));
		
		// Can't leave twice in a row.
		sendToServer("David", new LeaveRoomRequest("Kings"));
		Mockito.verify(connection).send("David", codec.encode(OperationResponse.FAILURE));
	}
	
	@Test
	public void testGetJoinedRooms() {
		sendToServer("David", new ConnectRequest());
		
		sendToServer("David", new JoinRoomRequest("Kings"));
		sendToServer("David", new JoinRoomRequest("Humans"));
		sendToServer("David", new JoinRoomRequest("Ushpizin"));
		sendToServer("Shaul", new JoinRoomRequest("Kings"));
		sendToServer("Shaul", new JoinRoomRequest("Humbles"));
		
		sendToServer("David", new GetJoinedRoomsRequest());

		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		Mockito.verify(connection, Mockito.atLeastOnce()).send(
				Mockito.eq("David"), argument.capture());
		GetJoinedRoomsResponse response = (GetJoinedRoomsResponse) codec.decode(argument.getValue());
		assertEquals(3, response.joinedRooms.size());
		assertTrue(response.joinedRooms.containsAll(Arrays.asList("Kings", "Humans", "Ushpizin")));
	}
	
	@Test
	public void testGetJoinedRoomsWithoutJoiningAnyRooms() {
		sendToServer("David", new ConnectRequest());
		
		sendToServer("David", new GetJoinedRoomsRequest());

		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		Mockito.verify(connection, Mockito.atLeastOnce()).send(
				Mockito.eq("David"), argument.capture());
		GetJoinedRoomsResponse response = (GetJoinedRoomsResponse) codec.decode(argument.getValue());
		assertEquals(0, response.joinedRooms.size());
	}
	
	@Test
	public void testGetAllRooms() {
		sendToServer("David", new ConnectRequest());
		
		sendToServer("David", new JoinRoomRequest("Kings"));
		sendToServer("David", new JoinRoomRequest("Humans"));
		sendToServer("David", new JoinRoomRequest("Ushpizin"));
		sendToServer("Shaul", new JoinRoomRequest("Kings"));
		sendToServer("Shaul", new JoinRoomRequest("Humbles"));
		
		sendToServer("David", new GetAllRoomsRequest());

		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		Mockito.verify(connection, Mockito.atLeastOnce()).send(
				Mockito.eq("David"), argument.capture());
		GetAllRoomsResponse response = (GetAllRoomsResponse) codec.decode(argument.getValue());
		assertEquals(4, response.allRooms.size());
		assertTrue(response.allRooms.containsAll(Arrays.asList("Kings", "Humans", "Ushpizin", "Humbles")));
	}
	
	@Test
	public void VerifyNonActiveRoomAreNotReturnedInGetAllRooms() {
		sendToServer("David", new ConnectRequest());
		
		sendToServer("David", new JoinRoomRequest("Kings"));
		sendToServer("David", new JoinRoomRequest("Humans"));
		sendToServer("David", new JoinRoomRequest("Ushpizin"));
		sendToServer("Shaul", new JoinRoomRequest("Kings"));
		sendToServer("Shaul", new JoinRoomRequest("Humbles"));
		
		sendToServer("Shaul", new LeaveRoomRequest("Humbles"));
		sendToServer("David", new LeaveRoomRequest("Kings"));
		sendToServer("David", new LeaveRoomRequest("Humans"));
		
		sendToServer("David", new GetAllRoomsRequest());

		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		Mockito.verify(connection, Mockito.atLeastOnce()).send(
				Mockito.eq("David"), argument.capture());
		GetAllRoomsResponse response = (GetAllRoomsResponse) codec.decode(argument.getValue());
		assertEquals(2, response.allRooms.size());
		assertTrue(response.allRooms.containsAll(Arrays.asList("Kings", "Ushpizin")));
	}
	
	@Test
	public void testGetClientsInRoom() {
		sendToServer("David", new ConnectRequest());
		
		sendToServer("David", new JoinRoomRequest("Kings"));
		sendToServer("David", new JoinRoomRequest("Humans"));
		sendToServer("David", new JoinRoomRequest("Ushpizin"));
		sendToServer("Shaul", new JoinRoomRequest("Kings"));
		sendToServer("Shaul", new JoinRoomRequest("Humbles"));
		
		sendToServer("David", new GetClientsInRoomRequest("Kings"));

		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		Mockito.verify(connection, Mockito.atLeastOnce()).send(
				Mockito.eq("David"), argument.capture());
		GetClientsInRoomResponse response = (GetClientsInRoomResponse) codec.decode(argument.getValue());
		assertEquals(2, response.clients.size());
		assertTrue(response.clients.containsAll(Arrays.asList("David", "Shaul")));
	}
	
	@Test
	public void GetClientsInNonExistentRoomReturnsEmptySet() {
		sendToServer("David", new ConnectRequest());
		
		sendToServer("David", new JoinRoomRequest("Kings"));
		sendToServer("David", new JoinRoomRequest("Humans"));
		sendToServer("David", new JoinRoomRequest("Ushpizin"));
		sendToServer("Shaul", new JoinRoomRequest("Kings"));
		sendToServer("Shaul", new JoinRoomRequest("Humbles"));
		
		sendToServer("David", new GetClientsInRoomRequest("HabbaHabba"));

		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		Mockito.verify(connection, Mockito.atLeastOnce()).send(
				Mockito.eq("David"), argument.capture());
		GetClientsInRoomResponse response = (GetClientsInRoomResponse) codec.decode(argument.getValue());
		assertEquals(0, response.clients.size());
	}
	
	@Test
	public void DisconnectedClientsAreNotInRooms() {
		sendToServer("David", new ConnectRequest());
		sendToServer("Shaul", new ConnectRequest());
		sendToServer("Shlomo", new ConnectRequest());
		
		sendToServer("David", new JoinRoomRequest("Kings"));
		sendToServer("Shaul", new JoinRoomRequest("Kings"));
		sendToServer("Shlomo", new JoinRoomRequest("Kings"));
		
		sendToServer("Shaul", new DisconnectRequest());
		
		sendToServer("David", new GetClientsInRoomRequest("Kings"));

		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		Mockito.verify(connection, Mockito.atLeastOnce()).send(
				Mockito.eq("David"), argument.capture());
		GetClientsInRoomResponse response = (GetClientsInRoomResponse) codec.decode(argument.getValue());
		assertEquals(2, response.clients.size());
		assertTrue(response.clients.containsAll(Arrays.asList("David", "Shlomo")));
	}
	
	@Test
	public void ReconnectedClientsAreInRooms() {
		sendToServer("David", new ConnectRequest());
		sendToServer("Shaul", new ConnectRequest());
		sendToServer("Shlomo", new ConnectRequest());
		
		sendToServer("David", new JoinRoomRequest("Kings"));
		sendToServer("Shaul", new JoinRoomRequest("Kings"));
		sendToServer("Shlomo", new JoinRoomRequest("Kings"));
		
		// Disconnect and reconnect Shaul.
		sendToServer("Shaul", new DisconnectRequest());
		sendToServer("Shaul", new ConnectRequest());
		
		sendToServer("David", new GetClientsInRoomRequest("Kings"));

		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		Mockito.verify(connection, Mockito.atLeastOnce()).send(
				Mockito.eq("David"), argument.capture());
		GetClientsInRoomResponse response = (GetClientsInRoomResponse) codec.decode(argument.getValue());
		assertEquals(3, response.clients.size());
		assertTrue(response.clients.containsAll(Arrays.asList("David", "Shlomo", "Shaul")));
	}
	
	@Test
	public void RoomsOfClientArePersistent() {
		sendToServer("David", new ConnectRequest());
		
		sendToServer("David", new JoinRoomRequest("Kings"));
		sendToServer("David", new JoinRoomRequest("Ushpizin"));
		sendToServer("David", new DisconnectRequest());
		
		restartServer();
		
		sendToServer("David", new ConnectRequest());
		
		sendToServer("David", new GetJoinedRoomsRequest());

		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		Mockito.verify(connection, Mockito.atLeastOnce()).send(
				Mockito.eq("David"), argument.capture());
		GetJoinedRoomsResponse response = (GetJoinedRoomsResponse) codec.decode(argument.getValue());
		assertEquals(2, response.joinedRooms.size());
		assertTrue(response.joinedRooms.containsAll(Arrays.asList("Kings", "Ushpizin")));
	}
}
