package il.ac.technion.cs.sd.app.chat;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Holds all the server related persistent data: Rooms, Online clients, and
 * memberships of clients in rooms.
 */
public class ServerData {

	private final Set<String> onlineClients;
	private final Map<String, Set<String>> roomsOfClient;
	private final Map<String, Set<String>> clientsInRoom;

	/**
	 * Create a new ServerData.
	 */
	ServerData() {
		onlineClients = new HashSet<String>();
		roomsOfClient = new HashMap<String, Set<String>>();
		clientsInRoom = new HashMap<String, Set<String>>();
	}

	/**
	 * Set a client as connected to the server.
	 * 
	 * @param client
	 *            the client to set as connected.
	 */
	void connectClient(String client) {
		onlineClients.add(client);
		// Connect client to all rooms he is signed to:
		for (String room : getRoomsOfClient(client)) {
			clientsInRoom.get(room).add(client);
		}
	}

	/**
	 * Set a client as disconnected from the server.
	 * 
	 * @param client
	 *            the client to set as disconnected.
	 */
	void disconnectClient(String client) {
		onlineClients.remove(client);
		// Disconnect client to all rooms he is signed to:
		for (String room : getRoomsOfClient(client)) {
			clientsInRoom.get(room).remove(client);
		}
	}

	/**
	 * Check whether a client is connected
	 * 
	 * @param client
	 *            the client to check
	 * @return true if the client is connected.
	 */
	boolean isClientConnected(String client) {
		return onlineClients.contains(client);
	}
	
	/**
	 * Disconnect all logged in clients.
	 */
	void disconnectAllClients() {
		while (!onlineClients.isEmpty()) {
			disconnectClient(onlineClients.iterator().next());
		}
	}

	/**
	 * Mark a client as joined to a room.
	 * 
	 * @param client
	 *            the client to join the room
	 * @param room
	 *            the room to be joined by the client.
	 */
	void joinRoom(String client, String room) {
		if (!roomsOfClient.containsKey(client)) {
			roomsOfClient.put(client, new HashSet<String>());
		}
		if (!clientsInRoom.containsKey(room)) {
			clientsInRoom.put(room, new HashSet<String>());
		}

		roomsOfClient.get(client).add(room);
		clientsInRoom.get(room).add(client);
	}

	/**
	 * Mark a client as joined to a room.
	 * 
	 * @param client
	 *            the client to join the room
	 * @param room
	 *            the room to be joined by the client.
	 */
	void leaveRoom(String client, String room) {
		roomsOfClient.get(client).remove(room);
		clientsInRoom.get(room).remove(client);

		// Remove map entry if set is empty.
		if (roomsOfClient.get(client).isEmpty()) {
			roomsOfClient.remove(client);
		}
		if (clientsInRoom.get(room).isEmpty()) {
			clientsInRoom.remove(room);
		}
	}
	
	/**
	 * Returns whether a client is currently a member of a room.
	 * @param client the client to check.
	 * @param room the room to check.
	 */
	boolean isClientInRoom(String client, String room) {
		return getClientsInRoom(room).contains(client);
	}

	/**
	 * Get the rooms in which a client is a member.
	 * 
	 * @param client
	 *            the client to get its rooms.
	 * @return a set of all the client's rooms.
	 */
	Set<String> getRoomsOfClient(String client) {
		if (roomsOfClient.containsKey(client)) {
			return roomsOfClient.get(client);
		}
		return Collections.<String> emptySet();
	}

	/**
	 * Get all the clients who are members in a room.
	 * 
	 * @param room
	 *            the room to get its client.
	 * @return a set of all the room's clients.
	 */
	Set<String> getClientsInRoom(String room) {
		if (clientsInRoom.containsKey(room)) {
			return clientsInRoom.get(room);
		}
		return Collections.<String> emptySet();
	}

	/**
	 * Get all the rooms that have at least one client connected to.
	 * @return A set of all active rooms.
	 */
	Set<String> getActiveRooms() {
		return clientsInRoom.keySet();
	}
}
