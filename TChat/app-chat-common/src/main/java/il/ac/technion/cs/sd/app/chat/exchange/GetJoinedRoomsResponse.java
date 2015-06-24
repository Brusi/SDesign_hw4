package il.ac.technion.cs.sd.app.chat.exchange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import il.ac.technion.cs.sd.app.chat.ExchangeVisitor;

/**
 * The server's response to GetJoinedRoomsRequest
 */
public class GetJoinedRoomsResponse implements Exchange {
	
	public final List<String> joinedRooms;
	
	/**
	 * Create a new GetJoinedRoomsResponse.
	 * @param joinedRooms the rooms that the requesting client has joined.
	 */
	public GetJoinedRoomsResponse(Collection<String> joinedRooms) {
		this.joinedRooms = new ArrayList<String>(joinedRooms);
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
		GetJoinedRoomsResponse other = (GetJoinedRoomsResponse)obj;
		if (joinedRooms == null) {
			if (other.joinedRooms != null)
				return false;
		} else if (!joinedRooms.equals(other.joinedRooms))
			return false;
		
		return true;
	}

}
