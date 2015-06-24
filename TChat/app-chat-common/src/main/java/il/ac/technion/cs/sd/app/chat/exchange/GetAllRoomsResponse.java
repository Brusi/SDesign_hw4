package il.ac.technion.cs.sd.app.chat.exchange;

import il.ac.technion.cs.sd.app.chat.ExchangeVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GetAllRoomsResponse implements Exchange {
	
	public final List<String> allRooms;
	
	public GetAllRoomsResponse(Collection<String> joinedRooms) {
		this.allRooms = new ArrayList<String>(joinedRooms);
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
