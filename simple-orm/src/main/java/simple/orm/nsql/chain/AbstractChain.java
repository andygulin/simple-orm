package simple.orm.nsql.chain;

import java.util.LinkedList;

public abstract class AbstractChain {

	protected LinkedList<AbstractChain> chains;

	protected AbstractChain previous;

	protected AbstractChain next;

	public AbstractChain() {
		this.chains = new LinkedList<>();
		this.chains.add(this);
	}

	public AbstractChain(AbstractChain previous) {
		this.chains = previous == null ? new LinkedList<AbstractChain>() : previous.chains;
		this.chains.add(this);
		this.previous = previous;
		previous.next = this;
	}

	protected abstract void appendTo(StringBuffer sb);

}
