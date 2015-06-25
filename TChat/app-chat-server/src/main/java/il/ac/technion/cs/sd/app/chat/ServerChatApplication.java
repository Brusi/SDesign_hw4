package il.ac.technion.cs.sd.app.chat;

import java.util.Optional;
import java.util.function.Function;

import il.ac.technion.cs.sd.app.chat.RoomAnnouncement.Announcement;
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
import il.ac.technion.cs.sd.app.chat.exchange.OperationResponse;
import il.ac.technion.cs.sd.app.chat.exchange.LeaveRoomRequest;
import il.ac.technion.cs.sd.app.chat.exchange.SendMessageRequest;
import il.ac.technion.cs.sd.msg.ServerCommunicationsLibrary;


/**
 * The server side of the TMail application. <br>
 * This class is mainly used in our tests to start, stop, and clean the server
 */
public class ServerChatApplication {
	
	private ServerData data;
	private DataSaver<ServerData> dataSaver;
	
	final private String serverAddress;
	final static private Codec<Exchange> codec = new XStreamCodec<Exchange>();
	
	private ServerCommunicationsLibrary connection;
	
    /**
     * Starts a new mail server. Servers with the same name retain all their information until
     * {@link ServerChatApplication#clean()} is called.
     *
     * @param name The name of the server by which it is known.
     */

	public ServerChatApplication(String name) {
		this.serverAddress = name;
		this.dataSaver = new XStreamDataSaver<ServerData>(serverAddress);
	}
	
	/**
	 * @return the server's address; this address will be used by clients connecting to the server
	 */
	public String getAddress() {
		return serverAddress;
	}
	
	/**
	 * Starts the server; any previously sent mails, data and indices are loaded.
	 * This should be a <b>non-blocking</b> call.
	 */
	public void start() {
		this.connection = new ServerCommunicationsLibrary(serverAddress);
		startConnection();
		loadData();
	}
	
	private void loadData() {
		Optional<ServerData> loaded_data = dataSaver.load();
		if (loaded_data.isPresent()) {
			data = loaded_data.get();
		} else {
			data = new ServerData();
		}
	}

	/**
	 * Starts the server app using a mock communication library. Used for unit testing.
	 * @param mockConnection
	 */
	void startWithMockConnection(ServerCommunicationsLibrary mockConnection) {
		this.connection = mockConnection; 
		startConnection();
		loadData();
	}
	
	/**
	 * Start the inner connection, providing it a consumer to decode and handle messages.
	 */
	private void startConnection() {
		this.connection.start((sender, payload) -> {
			Exchange exchange = codec.decode(payload);
			exchange.accept(new Visitor(sender));
		});
	}
	
	/**
	 * Stops the server. A stopped server can't accept messages, but doesn't delete any data (messages that weren't received).
	 */
	public void stop() {
		this.connection.stop();
		data.disconnectAllClients();
		dataSaver.save(data);
	}
	
	/**
	 * Deletes <b>all</b> previously saved data. This method will be used between tests to assure that each test will
	 * run on a new, clean server. you may assume the server is stopped before this method is called.
	 */
	public void clean() {
		data = new ServerData();
		dataSaver.clean();
	}
	
	
	/**
	 * Send an announcement to all the client's rooms. The announcement may be
	 * different for each room, and is defined by the function roomAnnouncement,
	 * which maps room name too the the appropriate announcement.
	 * 
	 * @param client the client to send the announcement to all its rooms.
	 * @param roomAnnouncementMap a function that maps the room name to the wanted announcement.
	 */
	private void announceInAllRoomsOfAClient(String client, Function<String, AnnouncementRequest> roomAnnouncementMap) {
		for (String room : data.getRoomsOfClient(client)) {
			broadcastToRoom(room, roomAnnouncementMap.apply(room));
		}
	}
	
	/**
	 * Broadcast an exchange to all (online) members of a room.
	 * @param room the room to send the message to its members.
	 * @param exchange the message to send.
	 */
	private void broadcastToRoom(String room, Exchange exchange) {
		for (String client : data.getClientsInRoom(room)) {
			sendIfOnline(client, exchange);
		}
	}
	
	/**
	 * Send an exchange to the client if it is online. Otherwise, discard the message.
	 * @param client the client to send the message to.
	 * @param exchange the message to send.
	 */
	private void sendIfOnline(String client, Exchange exchange) {
		if (!data.isClientConnected(client)) {
			return;
		}
		System.out.println(codec.encode(exchange));
		connection.send(client, codec.encode(exchange));
	}
	
	private class Visitor implements ExchangeVisitor {
		
		final private String client;

		Visitor(String client) {
			this.client = client;
		}
		
		@Override
		public void visit(ConnectRequest request) {
			data.connectClient(client);
			announceInAllRoomsOfAClient(client, room -> new AnnouncementRequest(
					new RoomAnnouncement(client, room, Announcement.JOIN)));
		}

		@Override
		public void visit(DisconnectRequest request) {
			data.disconnectClient(client);
			announceInAllRoomsOfAClient(client, room -> new AnnouncementRequest(
					new RoomAnnouncement(client, room, Announcement.DISCONNECT)));
			
			// TODO: check self announcements.
		}

		@Override
		public void visit(SendMessageRequest request) {
			// If the client is not in the requested room, return that the sending has failed.
			if (!data.isClientInRoom(client, request.message.room)) {
				sendIfOnline(client, OperationResponse.FAILURE);
				return;
			}
			// Broadcast message to all room members.
			broadcastToRoom(request.message.room, request);	
			// Send a successful response to the client.
			sendIfOnline(client, OperationResponse.SUCCESS);
		}
		
		@Override
		public void visit(JoinRoomRequest request) {
			// If the client is already in the room, return failure.
			if (data.isClientInRoom(client, request.room)) {
				sendIfOnline(client, OperationResponse.FAILURE);
				return;
			}
			data.joinRoom(client, request.room);
			sendIfOnline(client, OperationResponse.SUCCESS);
			broadcastToRoom(request.room, new AnnouncementRequest(
					new RoomAnnouncement(client, request.room,Announcement.JOIN)));
		}

		@Override
		public void visit(LeaveRoomRequest request) {
			// If the client is not in the room, return failure.
			if (!data.isClientInRoom(client, request.room)) {
				sendIfOnline(client, OperationResponse.FAILURE);
				return;
			}
			data.leaveRoom(client, request.room);
			sendIfOnline(client, OperationResponse.SUCCESS);
			broadcastToRoom(request.room, new AnnouncementRequest(
					new RoomAnnouncement(client, request.room, Announcement.LEAVE)));
		}

		@Override
		public void visit(OperationResponse response) {
			throw new UnsupportedOperationException("The server should not get an OperationResponse.");
		}

		@Override
		public void visit(GetJoinedRoomsRequest request) {
			sendIfOnline(client, new GetJoinedRoomsResponse(data.getRoomsOfClient(client)));
		}

		@Override
		public void visit(GetJoinedRoomsResponse response) {
			throw new UnsupportedOperationException("The server should not get a GetJoinedRoomsResponse.");
			
		}

		@Override
		public void visit(GetAllRoomsRequest request) {
			sendIfOnline(client, new GetAllRoomsResponse(data.getActiveRooms()));
		}

		@Override
		public void visit(GetAllRoomsResponse response) {
			throw new UnsupportedOperationException("The server should not get a GetAllRoomsResponse.");
		}

		@Override
		public void visit(GetClientsInRoomRequest request) {
			sendIfOnline(client, new GetClientsInRoomResponse(data.getClientsInRoom(request.room)));
		}

		@Override
		public void visit(GetClientsInRoomResponse response) {
			throw new UnsupportedOperationException("The server should not get a GetClientsInRoomResponse.");			
		}

		@Override
		public void visit(AnnouncementRequest request) {
			throw new UnsupportedOperationException("The server should not get an AnnouncementRequest.");
		}
	}
}
