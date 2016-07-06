package simple.orm.nsql.chain;

public class RightJoinChain extends JoinChain {

	public RightJoinChain(FromChain previous) {
		super(previous, JoinChain.JoinEnum.RIGHT);
	}

}
