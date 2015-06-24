package il.ac.technion.cs.sd.app.chat.exchange;

import il.ac.technion.cs.sd.app.chat.ExchangeVisitor;
import il.ac.technion.cs.sd.app.chat.RoomAnnouncement;

/**
 * An announcement that the server sends to the clients.
 */
public class AnnouncementRequest implements Exchange {
	
	public final RoomAnnouncement announcement;
	
	public AnnouncementRequest(RoomAnnouncement message) {
		this.announcement = message;
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
		AnnouncementRequest other = (AnnouncementRequest) obj;
		if (announcement == null) {
			if (other.announcement != null)
				return false;
		} else if (!announcement.equals(other.announcement))
			return false;
		return true;
	}

}
