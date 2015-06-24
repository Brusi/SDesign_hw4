package il.ac.technion.cs.sd.app.chat.exchange;

import il.ac.technion.cs.sd.app.chat.ExchangeVisitor;

public class ConnectRequest implements Exchange {

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

		return true;
	}
}
