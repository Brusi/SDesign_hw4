package il.ac.technion.cs.sd.app.chat.exchange;

import il.ac.technion.cs.sd.app.chat.ExchangeVisitor;

/**
 * A client's request to get all the clients in a given room.
 */
public class GetClientsInRoomRequest implements Exchange {
	
	public final String room;

	/**
	 * Create a new GetClientsInRoomRequest.
	 * @param room the room to get the clients in.
	 */
	GetClientsInRoomRequest(String room) {
		this.room = room;
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
		GetClientsInRoomRequest other = (GetClientsInRoomRequest) obj;
		if (room == null) {
			if (other.room != null)
				return false;
		} else if (!room.equals(other.room))
			return false;

		return true;
	}
}
