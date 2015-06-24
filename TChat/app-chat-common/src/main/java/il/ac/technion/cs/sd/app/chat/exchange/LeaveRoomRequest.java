package il.ac.technion.cs.sd.app.chat.exchange;

import il.ac.technion.cs.sd.app.chat.ExchangeVisitor;

/**
 * A client's request to join a chat room.
 */
public class LeaveRoomRequest implements Exchange {
	
	final public String room;
	
	/**
	 * Create a new LeaveRoomRequest.
	 * @param roomToleave the chat room to leave.
	 */
	public LeaveRoomRequest(String roomToleave) {
		this.room = roomToleave;
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
		LeaveRoomRequest other = (LeaveRoomRequest)obj;
		if (room == null) {
			if (other.room != null)
				return false;
		} else if (!room.equals(other.room))
			return false;
		
		return true;
	}

}
