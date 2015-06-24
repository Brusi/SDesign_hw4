package il.ac.technion.cs.sd.app.chat.exchange;

import java.util.Set;

import il.ac.technion.cs.sd.app.chat.ExchangeVisitor;


/**
 * The server's response to GetClientsInRoomRequest
 */
public class GetClientsInRoomResponse implements Exchange {
	
	public final Set<String> clients;
	
	/**
	 * Create a new GetClientsInRoomResponse
	 * @param clients the clients in the requested room.
	 */
	GetClientsInRoomResponse(Set<String> clients) {
		this.clients = clients;
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
