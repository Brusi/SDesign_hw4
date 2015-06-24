package il.ac.technion.cs.sd.app.chat.exchange;

import il.ac.technion.cs.sd.app.chat.ExchangeVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * The server's response to GetClientsInRoomRequest
 */
public class GetClientsInRoomResponse implements Exchange {
	
	public final List<String> clients;
	
	/**
	 * Create a new GetClientsInRoomResponse
	 * @param clients the clients in the requested room.
	 */
	public GetClientsInRoomResponse(Collection<String> clients) {
		this.clients = new ArrayList<String>(clients);
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
		GetClientsInRoomResponse other = (GetClientsInRoomResponse)obj;
		if (clients == null) {
			if (other.clients != null)
				return false;
		} else if (!clients.equals(other.clients))
			return false;
		
		return true;
	}

}
