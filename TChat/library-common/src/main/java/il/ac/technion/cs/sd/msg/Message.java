package il.ac.technion.cs.sd.msg;

import java.io.IOException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

public class Message {
	
	private String from;
	private String content;
	private MessageType messageType;
	
	public enum MessageType{
		NEW_MESSAGE,
		REPLIED_MESSAGE,
	}
	
	public Message(String from, String content, MessageType type) {
		super();
		this.from = from;
		this.content = content;
		this.messageType = type;
	}
	
	public Message() {
		super();
	}

	public String getFrom() {
		return from;
	}

	public String getContent() {
		return content;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Message other = (Message)obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		return true;
	}

	public MessageType getMessageType() {
		return messageType;
	}
	
	public String ToJsonString(){
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String jsonMessage = "";
		try {
			jsonMessage = ow.writeValueAsString(this);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
		}
		
		return jsonMessage;
	}
	
	public static Message FromJsonString(String json){
		ObjectMapper mapper = new ObjectMapper();
		Message reply = null;
		try {
			reply = mapper.readValue(json, Message.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		return reply;
	}
}
