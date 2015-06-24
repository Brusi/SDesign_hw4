package il.ac.technion.cs.sd.app.chat.exchange;

import il.ac.technion.cs.sd.app.chat.ChatMessage;
import il.ac.technion.cs.sd.app.chat.ExchangeVisitor;

/**
 * A client's request to send a message to a specific room. 
 */
public class SendMessageRequest implements Exchange {
	
	public final ChatMessage message;
	public final String room;
	
	/**
	 * Create a new SendMessageRequest.
	 * @param message the message to send.
	 * @param room the room to sent the message to.
	 */
	public SendMessageRequest(ChatMessage message, String room) {
		this.message = message;
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
		SendMessageRequest other = (SendMessageRequest)obj;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		
		return true;
	}

}
