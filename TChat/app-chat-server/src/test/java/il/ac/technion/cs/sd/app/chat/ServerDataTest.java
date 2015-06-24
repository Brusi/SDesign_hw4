package il.ac.technion.cs.sd.app.chat;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerDataTest {
	
	ServerData data;

	@Before
	public void setUp() throws Exception {
		data = new ServerData();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void noClientConnectedAtFirst() {
		assertFalse(data.isClientConnected("Haim"));
		assertFalse(data.isClientConnected("Zrubavel"));
	}

	@Test
	public void testConnectAndDisconnectClient() {
		assertFalse(data.isClientConnected("Moni"));
		data.connectClient("Moni");
		assertTrue(data.isClientConnected("Moni"));
		data.disconnectClient("Moni");
		assertFalse(data.isClientConnected("Moni"));
	}
	
	@Test
	public void testJoinRoom() {
		data.joinRoom("Moni", "ZehuZe");
		data.joinRoom("Moni", "Shorts");
		data.joinRoom("Shmulik", "Shorts");
		
		assertEquals(2, data.getActiveRooms().size());
		assertTrue(data.getActiveRooms().containsAll(Arrays.asList("ZehuZe", "Shorts")));				
		
		assertEquals(1, data.getClientsInRoom("ZehuZe").size());
		assertTrue(data.getClientsInRoom("ZehuZe").containsAll(Arrays.asList("Moni")));
		assertEquals(2, data.getClientsInRoom("Shorts").size());
		assertTrue(data.getClientsInRoom("Shorts").containsAll(Arrays.asList("Moni", "Shmulik")));
		
		assertEquals(1, data.getRoomsOfClient("Shmulik").size());
		assertTrue(data.getRoomsOfClient("Shmulik").containsAll(Arrays.asList("Shorts")));
		assertEquals(2, data.getRoomsOfClient("Moni").size());
		assertTrue(data.getRoomsOfClient("Moni").containsAll(Arrays.asList("ZehuZe", "Shorts")));
	}
	
	@Test
	public void testLeaveRoom() {
		data.joinRoom("Moni", "ZehuZe");
		data.joinRoom("Moni", "Shorts");
		
		assertEquals(2, data.getRoomsOfClient("Moni").size());
		assertTrue(data.getRoomsOfClient("Moni").containsAll(Arrays.asList("ZehuZe", "Shorts")));
		
		data.leaveRoom("Moni", "ZehuZe");
		assertEquals(1, data.getRoomsOfClient("Moni").size());
		assertTrue(data.getRoomsOfClient("Moni").containsAll(Arrays.asList("Shorts")));
		
		data.leaveRoom("Moni", "Shorts");
		assertEquals(0, data.getRoomsOfClient("Moni").size());
	}
	
	@Test
	public void roomNotActiveWhenAllClientsLeft() {
		data.joinRoom("Moni", "Shorts");
		data.joinRoom("Shmulik", "Shorts");
		
		assertEquals(2, data.getClientsInRoom("Shorts").size());
		assertTrue(data.getClientsInRoom("Shorts").containsAll(Arrays.asList("Moni", "Shmulik")));
		assertEquals(1, data.getActiveRooms().size());
		assertTrue(data.getActiveRooms().contains("Shorts"));
		
		data.leaveRoom("Moni", "Shorts");
		
		assertEquals(1, data.getClientsInRoom("Shorts").size());
		assertTrue(data.getClientsInRoom("Shorts").containsAll(Arrays.asList("Shmulik")));
		assertEquals(1, data.getActiveRooms().size());
		
		data.leaveRoom("Shmulik", "Shorts");
		assertEquals(0, data.getClientsInRoom("Shorts").size());
		assertEquals(0, data.getActiveRooms().size());
	}
	
	@Test
	public void getEmptyClientsForInactiveRoom() {
		assertEquals(0, data.getClientsInRoom("Roomy").size());
		
		data.joinRoom("Clienty", "Roomy");
		assertEquals(1, data.getClientsInRoom("Roomy").size());
		
		data.leaveRoom("Clienty", "Roomy");
		assertEquals(0, data.getClientsInRoom("Roomy").size());
		
	}
	
	@Test
	public void getEmptyRoomsForInactiveClients() {
		assertEquals(0, data.getRoomsOfClient("Clienty").size());
		
		data.joinRoom("Clienty", "Roomy");
		assertEquals(1, data.getRoomsOfClient("Clienty").size());
		
		data.leaveRoom("Clienty", "Roomy");
		assertEquals(0, data.getRoomsOfClient("Clienty").size());
		
	}
	
	@Test
	public void checkIfClientIsInRoom() {
		assertFalse(data.isClientInRoom("Moni", "ZehuZe"));
		data.joinRoom("Moni", "ZehuZe");
		assertTrue(data.isClientInRoom("Moni", "ZehuZe"));
		data.leaveRoom("Moni", "ZehuZe");
		assertFalse(data.isClientInRoom("Moni", "ZehuZe"));
	}
	
	@Test
	public void loggedOutClientIsOutOfAllRooms() {
		assertFalse(data.isClientInRoom("Moni", "ZehuZe"));
		data.joinRoom("Moni", "ZehuZe");
		assertTrue(data.isClientInRoom("Moni", "ZehuZe"));
		data.disconnectClient("Moni");
		assertFalse(data.isClientInRoom("Moni", "ZehuZe"));
	}
	
	@Test
	public void clientRemembersHisRooms() {
		assertFalse(data.isClientInRoom("Moni", "ZehuZe"));
		data.joinRoom("Moni", "ZehuZe");
		assertTrue(data.isClientInRoom("Moni", "ZehuZe"));
		data.disconnectClient("Moni");
		assertFalse(data.isClientInRoom("Moni", "ZehuZe"));
		data.connectClient("Moni");
		assertTrue(data.isClientInRoom("Moni", "ZehuZe"));
	}
}
