package il.ac.technion.cs.sd.app.chat.exchange;

import il.ac.technion.cs.sd.app.chat.ChatMessage;
import il.ac.technion.cs.sd.app.chat.ExchangeVisitor;

/**
 * A client's request to send a message to a specific room. 
 */
public class SendMessageRequest implements Exchange {
	
	public final ChatMessage message;
	
	/**
	 * Create a new SendMessageRequest.
	 * @param message the message to send.
	 */
	public SendMessageRequest(ChatMessage message) {
		this.message = message;
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
