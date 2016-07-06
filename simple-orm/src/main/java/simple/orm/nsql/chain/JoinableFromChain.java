package simple.orm.nsql.chain;

public class JoinableFromChain extends FromChain {

	public JoinableFromChain(SelectChain previous) {
		super(previous);
	}

	public JoinChain leftJoin(Class<?> clazz) {
		LeftJoinChain chain = new LeftJoinChain(this);
		chain.setClazz(clazz);
		return chain;
	}

	public JoinChain rightJoin(Class<?> clazz) {
		RightJoinChain chain = new RightJoinChain(this);
		chain.setClazz(clazz);
		return chain;
	}

}
