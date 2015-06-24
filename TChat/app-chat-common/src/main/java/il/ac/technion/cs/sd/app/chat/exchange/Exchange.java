package il.ac.technion.cs.sd.app.chat.exchange;

import il.ac.technion.cs.sd.app.chat.ExchangeVisitor;

/**
 * An exchange between the application server and client. 
 */
public interface Exchange {
	void accept(ExchangeVisitor v);
}
