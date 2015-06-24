package il.ac.technion.cs.sd.app.chat.exchange;

import il.ac.technion.cs.sd.app.chat.ExchangeVisitor;

/**
 * A client's request to join a chat room.
 */
public class JoinRoomRequest implements Exchange {
	
	final public String room;
	
	/**
	 * Create a new JoinRoomRequest.
	 * @param roomToJoin the chat room to join.
	 */
	public JoinRoomRequest(String roomToJoin) {
		this.room = roomToJoin;
	}

	@Override
	public void accept(ExchangeVisitor v) {
		v.visit(this);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JoinRoomRequest other = (JoinRoomRequest)obj;
		if (room == null) {
			if (other.room != null)
				return false;
		} else if (!room.equals(other.room))
			return false;
		
		return true;
	}

}
