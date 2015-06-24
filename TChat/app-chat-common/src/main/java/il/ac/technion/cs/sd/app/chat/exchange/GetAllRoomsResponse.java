package il.ac.technion.cs.sd.app.chat.exchange;

import java.util.Set;

import il.ac.technion.cs.sd.app.chat.ExchangeVisitor;

public class GetAllRoomsResponse implements Exchange {
	
	public final Set<String> allRooms;
	
	public GetAllRoomsResponse(Set<String> joinedRooms) {
		this.allRooms = joinedRooms;
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
		GetAllRoomsResponse other = (GetAllRoomsResponse)obj;
		if (allRooms == null) {
			if (other.allRooms != null)
				return false;
		} else if (!allRooms.equals(other.allRooms))
			return false;
		
		return true;
	}

}
