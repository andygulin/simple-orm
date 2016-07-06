package simple.orm.nsql.chain;

public class LeftJoinChain extends JoinChain {

	public LeftJoinChain(FromChain previous) {
		super(previous, JoinChain.JoinEnum.LEFT);
	}

}
