package il.ac.technion.cs.sd.app.chat.exchange;

import il.ac.technion.cs.sd.app.chat.ExchangeVisitor;

/**
 * A response to a JoinRoomRequest, LeaveRoomRequest, and SendMessageRequest.
 * This response specifies whether the operation was successful. 
 */
public class OperationResponse implements Exchange {
	
	public final boolean isSuccessful;

	/**
	 * Create a new OperationResponse.
	 * @param isSuccessful whether the client has successfully joined the room.
	 */
	private OperationResponse(boolean isSuccessful) {
		this.isSuccessful = isSuccessful;
	}
	
	static final public OperationResponse SUCCESS = new OperationResponse(true);
	static final public OperationResponse FAILURE = new OperationResponse(false);

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
		OperationResponse other = (OperationResponse)obj;
		return isSuccessful == other.isSuccessful;
	}

}
