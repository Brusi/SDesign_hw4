package il.ac.technion.cs.sd.app.chat;

import il.ac.technion.cs.sd.app.chat.exchange.AnnouncementRequest;
import il.ac.technion.cs.sd.app.chat.exchange.ConnectRequest;
import il.ac.technion.cs.sd.app.chat.exchange.DisconnectRequest;
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

/**
 * A visitor for the exchange class. Each class that uses the Exchange class
 * (client and server application classes) will implement this visitor in order
 * to handle different types requests and responses.
 */
public interface ExchangeVisitor {
	/**
	 * Accept and handle ConnectRequest.
	 * @param request the ConnectRequest to handle.
	 */
	void visit(ConnectRequest request);
	
	/**
	 * Accept and handle DisconnectRequest.
	 * @param request the DisconnectRequest to handle.
	 */
	void visit(DisconnectRequest request);
	
	/**
	 * Accept and handle SendMessageRequest.
	 * @param request the SendMessageRequest to handle.
	 */
	void visit(SendMessageRequest request);

	/**
	 * Accept and handle JoinRoomRequest.
	 * @param request the JoinRoomRequest to handle.
	 */
	void visit(JoinRoomRequest joinRoomRequest);
	
	/**
	 * Accept and handle LeaveRoomRequest.
	 * @param request the LeaveRoomRequest to handle.
	 */
	void visit(LeaveRoomRequest leaveRoomRequest);

	/**
	 * Accept and handle OperationResponse.
	 * @param request the OperationResponse to handle.
	 */
	void visit(OperationResponse joinRoomResponse);

	/**
	 * Accept and handle GetJoinedRoomsRequest.
	 * @param request the GetJoinedRoomsRequest to handle.
	 */
	void visit(GetJoinedRoomsRequest getJoinedRoomsRequest);

	/**
	 * Accept and handle GetJoinedRoomsResponse.
	 * @param request the GetJoinedRoomsResponse to handle.
	 */
	void visit(GetJoinedRoomsResponse getJoinedRoomsResponse);

	/**
	 * Accept and handle GetAllRoomsRequest.
	 * @param request the GetAllRoomsRequest to handle.
	 */
	void visit(GetAllRoomsRequest getAllRoomsRequest);
	
	/**
	 * Accept and handle GetAllRoomsResponse.
	 * @param request the GetAllRoomsResponse to handle.
	 */
	void visit(GetAllRoomsResponse getAllRoomsRequest);

	/**
	 * Accept and handle GetClientsInRoomRequest.
	 * @param request the GetClientsInRoomRequest to handle.
	 */
	void visit(GetClientsInRoomRequest getClientsInRoomRequest);

	/**
	 * Accept and handle GetClientsInRoomResponse.
	 * @param request the GetClientsInRoomResponse to handle.
	 */
	void visit(GetClientsInRoomResponse getClientsInRoomResponse);

	/**
	 * Accept and handle AnnouncementRequest.
	 * @param request the AnnouncementRequest to handle.
	 */
	void visit(AnnouncementRequest announcementRequest);
}
