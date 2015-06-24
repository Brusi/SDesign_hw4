package il.ac.technion.cs.sd.app.chat;

import il.ac.technion.cs.sd.app.chat.exchange.AnnouncementRequest;
import il.ac.technion.cs.sd.app.chat.exchange.ConnectRequest;
import il.ac.technion.cs.sd.app.chat.exchange.DisconnectRequest;
import il.ac.technion.cs.sd.app.chat.exchange.Exchange;
import il.ac.technion.cs.sd.app.chat.exchange.GetAllRoomsRequest;
import il.ac.technion.cs.sd.app.chat.exchange.GetAllRoomsResponse;
import il.ac.technion.cs.sd.app.chat.exchange.GetClientsInRoomRequest;
import il.ac.technion.cs.sd.app.chat.exchange.GetClientsInRoomResponse;
import il.ac.technion.cs.sd.app.chat.exchange.GetJoinedRoomsRequest;
import il.ac.technion.cs.sd.app.chat.exchange.GetJoinedRoomsResponse;
import il.ac.technion.cs.sd.app.chat.exchange.JoinRoomRequest;
import il.ac.technion.cs.sd.app.chat.exchange.LeaveRoomRequest;
import il.ac.technion.cs.sd.app.chat.exchange.OperationResponse;
import il.ac.technion.cs.sd.app.chat.exchange.SendMessageRequest;
import il.ac.technion.cs.sd.msg.ClientCommunicationsLibrary;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

/**
 * The client side of the TChat application. Allows sending and getting messages
 * to and from other clients using a server. <br>
 * You should implement all the methods in this class
 */
public class ClientChatApplication {

	// INSTANCE MEMBERS
	private final String myUsername;
	private final String myServerAddress;
	private Consumer<ChatMessage> chatMessageConsumer;
	private Consumer<RoomAnnouncement> announcementConsumer;
	private final Semaphore responseSemaphore;
	private final Codec<Exchange> myCodec;
	private List<String> rooms;
	private boolean opResponse;
	private boolean isLoggedIn = false;
	private List<String> clients;
	private ClientCommunicationsLibrary connection = null;
	private Visitor myVisitor = new Visitor();
	
	private void assertNotNullOrEmpty(String s) {
		if (null == s || "".equals(s)) {
			throw new IllegalArgumentException();
		}
	}
	

	private void syncSend(Exchange ex) {
		this.connection.send(myCodec.encode(ex));
		
		try {
			// Wait until login is responded and all pending messages handled.
			responseSemaphore.acquire();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void loggedInOrException() {
		if (!this.isLoggedIn) {
			throw new RuntimeException("Cannot perform requested operation when not logged in");
		}
	}
	
	/**
	 * Creates a new application, tied to a single user
	 * 
	 * @param serverAddress The address of the server to connect to for sending and
	 *            receiving messages
	 * @param username The username that will be sending and accepting the messages
	 *            using this object
	 */
	public ClientChatApplication(String serverAddress, String username) {
		assertNotNullOrEmpty(username);
		assertNotNullOrEmpty(serverAddress);
		
		this.myUsername = username;
		this.myServerAddress = serverAddress;
		
		this.responseSemaphore = new Semaphore(0);
		this.myCodec = new XStreamCodec<Exchange>();
	}

	/**
	 * Logs the user to the server. The user automatically joins all the rooms he was
	 * joined to to before logging out. All the <i>other</i> clients in the rooms will receive a message upon joining.
	 * The server can only reply to this message using an empty message.
	 * Client receive their own messages and announcements, e.g., a client also receives his own messages. 
	 * @param chatMessageConsumer The consumer of chat messages 
	 * 		(See {@link ClientChatApplication#sendMessage(String, String)}) 
	 * @param announcementConsumer The consumer of room announcements
	 * 		(See {@link RoomAnnouncement.Announcement})
	 */
	public void login(Consumer<ChatMessage> chatMessageConsumer,
			Consumer<RoomAnnouncement> announcementConsumer) {

		if (null == chatMessageConsumer || null == announcementConsumer) {
			throw new IllegalArgumentException("consumers can't be empty");
		}
		
		this.chatMessageConsumer = chatMessageConsumer;
		this.announcementConsumer = announcementConsumer;
		
		if (null == this.connection) {
			this.connection = new ClientCommunicationsLibrary(myServerAddress, myUsername, s -> handleIncoming(s));
		}
		
		syncSend(new ConnectRequest());
		this.isLoggedIn = true;
	}

	
	private Object handleIncoming(String s) {
		Exchange ex = myCodec.decode(s);
		ex.accept(myVisitor);
		
		return null;
	}


	/**
	 * Joins the room. If the room does not exist, it will be created. 
         * All the <i>other</i> clients in the room will receive a message.
	 * @param room The room to join
	 * @throws AlreadyInRoomException If the client isn't currently in the room
	 */
	public void joinRoom(String room) throws AlreadyInRoomException {
		assertNotNullOrEmpty(room);
		loggedInOrException();
		
		syncSend(new JoinRoomRequest(room));
		
		if (!this.opResponse) {
			throw new AlreadyInRoomException();
		}
	}


	/**
	 * Leaves the room. All the <i>other</i> clients in the room will receive a message.
	 * @param room The room to leave
	 * @throws NotInRoomException If the client isn't currently in the room
	 */
	public void leaveRoom(String room) throws NotInRoomException {
		assertNotNullOrEmpty(room);
		loggedInOrException();
		
		syncSend(new LeaveRoomRequest(room));
		
		if (!this.opResponse) {
			throw new NotInRoomException();
		}
	}

	
	/**
	 * Logs the user out of chat application. A logged out client cannot perform any tasks other than logging in.
	 * A logged out message will be sent to all the <i>other</i> clients in rooms with the client.
	 */
	public void logout() {
		loggedInOrException();
		syncSend(new DisconnectRequest());
		this.isLoggedIn = false;
	}

	
	/**
	 * Broadcasts a message to all <i>other</i> clients in the room. 
	 * @param room The room to broadcast the message to.
	 * @param what The message to broadcast.
	 * @throws NotInRoomException If the client isn't currently in the room
	 */
	public void sendMessage(String room, String what) throws NotInRoomException {
		loggedInOrException();
		assertNotNullOrEmpty(room);
		assertNotNullOrEmpty(what); // TODO validate in piazza, that "" is invalid message
		
		ChatMessage msg = new ChatMessage(myUsername, room, what);
		SendMessageRequest request = new SendMessageRequest(msg, room);
		
		connection.send(myCodec.encode(request));
	}

	
	/**
	 * @return All the rooms the client joined
	 */
	public List<String> getJoinedRooms() {
		loggedInOrException();
		syncSend(new GetJoinedRoomsRequest());
		return new ArrayList<String>(this.rooms); // defensive copying
	}

	
	/**
	 * @return all rooms that have clients currently online, i.e., logged in
	 */
	public List<String> getAllRooms() {
		loggedInOrException();
		syncSend(new GetAllRoomsRequest());
		return new ArrayList<String>(this.rooms); // defensive copying
	}

	
	/**
	 * Gets all the clients that joined the room and are currently logged in. A client
	 * does not have to be in a room to get a list of its clients.
	 * @param room The room to check
	 * @return A list of all the online clients in the room
	 * @throws NoSuchRoomException If the room doesn't exist, or no clients are currently in it (i.e., are logged out)
	 */
	public List<String> getClientsInRoom(String room) throws NoSuchRoomException {
		loggedInOrException();
		assertNotNullOrEmpty(room);
		
		syncSend(new GetClientsInRoomRequest(room));
		return new ArrayList<String>(this.clients);
	}

	/**
	 * Stops the client, freeing up any resources used.
	 * You can assume that {@link ClientChatApplication#logout()} was called before this method if the client
	 * was logged in.
	 */
	public void stop() {
		connection.stop();
	}

	
	class Visitor implements ExchangeVisitor {

		@Override
		public void visit(ConnectRequest request) {
			 throw new UnsupportedOperationException("The client should not get ConnectRequest.");
		}

		@Override
		public void visit(DisconnectRequest request) {
			throw new UnsupportedOperationException("The client should not get DisconnectRequest.");			
		}

		@Override
		public void visit(SendMessageRequest request) {
			chatMessageConsumer.accept(request.message);
		}

		@Override
		public void visit(JoinRoomRequest request) {
			throw new UnsupportedOperationException("The client should not get JoinRoomRequest.");			
		}

		@Override
		public void visit(LeaveRoomRequest request) {
			throw new UnsupportedOperationException("The client should not get LeaveRoomRequest.");
		}

		@Override
		public void visit(OperationResponse response) {
			opResponse = response.isSuccessful;
			responseSemaphore.release();
		}

		@Override
		public void visit(GetJoinedRoomsRequest request) {
			throw new UnsupportedOperationException("The client should not get GetJoinedRoomsRequest.");			
		}

		@Override
		public void visit(GetJoinedRoomsResponse response) {
			rooms = response.joinedRooms;
			responseSemaphore.release();
		}

		@Override
		public void visit(GetAllRoomsRequest request) {
			throw new UnsupportedOperationException("The client should not get GetAllRoomsRequest.");			
		}

		@Override
		public void visit(GetAllRoomsResponse response) {
			rooms = response.allRooms;
			responseSemaphore.release();
		}

		@Override
		public void visit(GetClientsInRoomRequest request) {
			throw new UnsupportedOperationException("The client should not get GetClientsInRoomRequest.");			
		}

		@Override
		public void visit(GetClientsInRoomResponse response) {
			clients = response.clients;
			responseSemaphore.release();
		}

		@Override
		public void visit(AnnouncementRequest request) {
			announcementConsumer.accept(request.announcement);
		}
		
	}
}
