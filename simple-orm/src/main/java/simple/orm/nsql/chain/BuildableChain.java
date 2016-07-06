package simple.orm.nsql.chain;

import simple.orm.nsql.cmd.SQLCommand;

public interface BuildableChain {

	public SQLCommand build();

}
