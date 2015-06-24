package il.ac.technion.cs.sd.app.chat;

import static org.junit.Assert.fail;
import il.ac.technion.cs.sd.msg.ClientCommunicationsLibrary;

import java.util.concurrent.LinkedBlockingQueue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ClientChatApplicationTest {

	private LinkedBlockingQueue<ChatMessage> messages = new LinkedBlockingQueue<>();
	private LinkedBlockingQueue<RoomAnnouncement> announcements = new LinkedBlockingQueue<>();
	private ClientCommunicationsLibrary mockConnection = Mockito.mock(ClientCommunicationsLibrary.class);
	private ClientChatApplication capp;
	
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
	
	@Test
	public void cantCreateAppWithNullServer() {
		fail("Not yet implemented");
	}
	
	@Test
	public void cantCreateAppWithEmptyServer() {
		fail("Not yet implemented");
	}
	
	@Test
	public void cantCreateAppWithNullAddress() {
		fail("Not yet implemented");
	}
	
	@Test
	public void cantCreateAppWithEmptyAddress() {
		fail("Not yet implemented");
	}
	
	@Test
	public void cantLoginWithNullMessageConsumer() {
		fail("Not yet implemented");
	}
	
	@Test
	public void cantLoginWithNullAnnouncementConsumer() {
		fail("Not yet implemented");
	}
	
	@Test
	public void failureToJoinRoomThrowsException() {
		fail("Not yet implemented");
	}
	
	@Test
	public void cantJoinNullRoom() {
		fail("Not yet implemented");
	}
	
	@Test
	public void cantJoinEmptyNamedRoom() {
		fail("Not yet implemented");
	}
	
	@Test
	public void cantLeaveNullRoom() {
		fail("Not yet implemented");
	}
	
	@Test
	public void cantLeaveEmptyNamedRoom() {
		fail("Not yet implemented");
	}
	
	@Test
	public void failureToLeaveRoomThrowsException() {
		fail("Not yet implemented");
	}
	
	@Test
	public void stopKillsTheConnection() {
		fail("Not yet implemented");
	}
	
	@Test
	public void failureToGetClientsInRoomThrowsException() {
		fail("Not yet implemented");
	}
	
	@Test
	public void cantGetClientsOfNullRoom() {
		fail("Not yet implemented");
	}
	
	@Test
	public void cantGetClientsOfEmptyNamedRoom() {
		fail("Not yet implemented");
	}
	
	@Test
	public void cantSendNullMessage() {
		fail("Not yet implemented");
	}
	
	@Test
	public void cantSendEmptyMessage() {
		fail("Not yet implemented");
	}
	
	@Test
	public void cantSendMessageToNullRoom() {
		fail("Not yet implemented");
	}
	
	@Test
	public void cantSendMessageToEmptyNamedRoom() {
		fail("Not yet implemented");
	}
	
	@Test
	public void cantSendMessageWhenNotLoggedIn() {
		fail("Not yet implemented");
	}
	
	@Test
	public void cantJoinRoomWhenNotLoggedIn() {
		fail("Not yet implemented");
	}
	
	@Test
	public void cantLeaveRoomWhenNotLoggedIn() {
		fail("Not yet implemented");
	}
	
	@Test
	public void cantGetJoinedRoomsWhenNotLoggedIn() {
		fail("Not yet implemented");
	}
	
	@Test
	public void cantGetAllRoomsWhenNotLoggedIn() {
		fail("Not yet implemented");
	}
	
	@Test
	public void cantGetClientsInRoomWhenNotLoggedIn() {
		fail("Not yet implemented");
	}
	
	
	///////////////////////////////////////
	/////////// POSITIVE TESTS ////////////
	///////////////////////////////////////

	@Test
	public void sendMessageIsNonBlocking() {
		fail("Not yet implemented");
	}
	
	@Test
	public void clientReceivesHisOwnSendMessage() {
		fail("Not yet implemented");
	}
	
	@Test
	public void clientReceivesOtherLeavingAnnouncements() {
		fail("Not yet implemented");
	}
	
	@Test
	public void clientReceivesOtherJoiningAnnouncements() {
		fail("Not yet implemented");
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
