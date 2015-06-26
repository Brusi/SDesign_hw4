package il.ac.technion.cs.sd.app.chat;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.app.chat.RoomAnnouncement.Announcement;
import il.ac.technion.cs.sd.app.chat.exchange.AnnouncementRequest;
import il.ac.technion.cs.sd.app.chat.exchange.Exchange;
import il.ac.technion.cs.sd.app.chat.exchange.GetClientsInRoomResponse;
import il.ac.technion.cs.sd.app.chat.exchange.OperationResponse;
import il.ac.technion.cs.sd.msg.ClientCommunicationsLibrary;
import il.ac.technion.cs.sd.msg.ReliableMessenger;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.*;
import org.junit.rules.Timeout;
import org.mockito.Mockito;

public class ClientChatApplicationTest {

	private LinkedBlockingQueue<ChatMessage> messages = new LinkedBlockingQueue<>();
	private LinkedBlockingQueue<RoomAnnouncement> announcements = new LinkedBlockingQueue<>();
	private ClientCommunicationsLibrary mockConnection = Mockito.mock(ClientCommunicationsLibrary.class);
	private ClientChatApplication capp;
	private Codec<Exchange> myCodec = new XStreamCodec<>();
	
	@Rule public Timeout globaltime = Timeout.millis(1000L);
	
	
	@Before
	public void setUp() throws Exception {
		capp = new ClientChatApplication("server", "tester-client", mockConnection);
		capp.login(m -> messages.add(m), a -> announcements.add(a));
	}

	@After
	public void tearDown() throws Exception {
		if (null != capp) {
			try {
				capp.logout();
			} catch (Exception e) {}
			
			capp.stop();
		}
		
		capp = null;
		
		announcements.clear();
		messages.clear();
	}

	
	///////////////////////////////////////
	/////////// NEGATIVE TESTS ////////////
	///////////////////////////////////////
	
	@Test (expected=IllegalArgumentException.class)
	public void cantCreateAppWithNullServer() {
		new ClientChatApplication(null, "dummy");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void cantCreateAppWithEmptyServer() {
		new ClientChatApplication("", "dummy");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void cantCreateAppWithNullAddress() {
		new ClientChatApplication("dummy", null);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void cantCreateAppWithEmptyAddress() {
		new ClientChatApplication("dummy", "");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void cantLoginWithNullMessageConsumer() {
		capp.logout();
		capp.login(x -> {}, null);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void cantLoginWithNullAnnouncementConsumer() {
		capp.logout();
		capp.login(null, x -> {});
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void cantJoinNullRoom() throws Exception {
		capp.joinRoom(null);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void cantJoinEmptyNamedRoom() throws Exception {
		capp.joinRoom("");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void cantLeaveNullRoom() throws Exception {
		capp.leaveRoom(null);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void cantLeaveEmptyNamedRoom() throws Exception {
		capp.leaveRoom("");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void cantGetClientsOfNullRoom() throws Exception {
		capp.getClientsInRoom(null);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void cantGetClientsOfEmptyNamedRoom() throws Exception {
		capp.getClientsInRoom("");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void cantSendNullMessage() throws Exception {
		capp.sendMessage("room", null);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void cantSendEmptyMessage() throws Exception {
		capp.sendMessage("room", "");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void cantSendMessageToNullRoom() throws Exception {
		capp.sendMessage(null, "what");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void cantSendMessageToEmptyNamedRoom() throws Exception {
		capp.sendMessage("", "what");
	}
	
	@Test (expected=RuntimeException.class)
	public void cantSendMessageWhenNotLoggedIn() throws Exception {
		capp.logout();
		capp.sendMessage("room", "what");
	}
	
	@Test (expected=RuntimeException.class)
	public void cantJoinRoomWhenNotLoggedIn() throws Exception {
		capp.logout();
		capp.joinRoom("room");
	}
	
	@Test (expected=RuntimeException.class)
	public void cantLeaveRoomWhenNotLoggedIn() throws Exception {
		capp.logout();
		capp.leaveRoom("room");
	}
	
	@Test (expected=RuntimeException.class)
	public void cantGetJoinedRoomsWhenNotLoggedIn() {
		capp.logout();
		capp.getJoinedRooms();
	}
	
	@Test (expected=RuntimeException.class)
	public void cantGetAllRoomsWhenNotLoggedIn() {
		capp.logout();
		capp.getAllRooms();
	}
	
	@Test (expected=RuntimeException.class)
	public void cantGetClientsInRoomWhenNotLoggedIn() throws Exception {
		capp.logout();
		capp.getClientsInRoom("room");
	}

	// TODO exceptions on non-expected exchanges
	
	
	///////////////////////////////////////
	/////////// POSITIVE TESTS ////////////
	///////////////////////////////////////
	
	@Test
	public void clientReceivesOtherLeavingAnnouncements() {
		RoomAnnouncement leaveAnnouncement = new RoomAnnouncement("someone", "room", Announcement.LEAVE);
		sendAnnouncementToClient(leaveAnnouncement);
		
		assertTrue(announcements.contains(leaveAnnouncement));
	}
	
	@Test
	public void clientReceivesOtherJoiningAnnouncements() {
		RoomAnnouncement joinAnnouncement = new RoomAnnouncement("someone", "room", Announcement.JOIN);
		sendAnnouncementToClient(joinAnnouncement);
		
		assertTrue(announcements.contains(joinAnnouncement));
	}
	
	@Test
	public void clientReceivesOtherDisconnectingAnnouncements() {
		RoomAnnouncement joinAnnouncement = new RoomAnnouncement("someone", "room", Announcement.DISCONNECT);
		sendAnnouncementToClient(joinAnnouncement);
		
		assertTrue(announcements.contains(joinAnnouncement));
	}
	
	@Test
	public void stopKillsTheConnection() {
		capp.stop();
		
		// should be able to create a new messenger with previously used address
		ReliableMessenger m = new ReliableMessenger(capp.getUsername(), x -> {});
		m.kill();
	}
	
	/* OperationResponse methods */
	
	@Test (expected=NotInRoomException.class)
	public void failureToLeaveRoomThrowsException() throws Exception {
		getTimedOperationFailureInBackground(5L);
		
		capp.leaveRoom("not_in_room");
	}
	
	@Test (expected=NotInRoomException.class)
	public void failureToSendThrowsException() throws Exception {
		getTimedOperationFailureInBackground(5L);
		
		capp.sendMessage("room", "what");
		fail("should have thrown exception");
	}
	
	@Test (expected=AlreadyInRoomException.class)
	public void failureToJoinRoomThrowsException() throws Exception {
		getTimedOperationFailureInBackground(5L);
		
		capp.joinRoom("some_room");
	}
	
	/* List-Response methods */
	
	@Test (expected=NoSuchRoomException.class)
	public void failureToGetClientsInRoomThrowsException() throws Exception {
		getEmptyListInBackground(5L);
		
		capp.getClientsInRoom("some_room");
	}

	
	///////////////////////////////////////
	/////////// HELPER UTILITIES //////////
	///////////////////////////////////////
	
	private void getEmptyListInBackground(long delay) {
		new Thread(() -> {
			try {
				// let main thread try to leave room and block
				Thread.yield();
				Thread.sleep(delay);
			} catch (Exception e) {}
			
			// get false operation response
			capp.handleIncoming(myCodec.encode(new GetClientsInRoomResponse(new ArrayList<String>())));
		}).start();
	}

	private void sendAnnouncementToClient(RoomAnnouncement leaveAnnouncement) {
		Exchange ex = new AnnouncementRequest(leaveAnnouncement);
		capp.handleIncoming(myCodec.encode(ex));
	}
	
	

	private void getTimedOperationFailureInBackground(long delay) {
		new Thread(() -> {
			try {
				// let main thread try to leave room and block
				Thread.yield();
				Thread.sleep(delay);
			} catch (Exception e) {}
			
			// get false operation response
			capp.handleIncoming(myCodec.encode(OperationResponse.FAILURE));
		}).start();
	}
	
	/**
	 * interface to test:
	 * public ClientChatApplication(String serverAddress, String username, connection)
	 * public ClientChatApplication(String serverAddress, String username)
	 * public void login(Consumer<ChatMessage> chatMessageConsumer,	Consumer<RoomAnnouncement> announcementConsumer)
	 * public void joinRoom(String room) throws AlreadyInRoomException
	 * public void leaveRoom(String room) throws NotInRoomException
	 * public void logout()
	 * public void sendMessage(String room, String what) throws NotInRoomException
	 * public List<String> getJoinedRooms()
	 * public List<String> getAllRooms()
	 * public List<String> getClientsInRoom(String room) throws NoSuchRoomException
	 * public void stop()
	 */
	
}
