package simple.orm.nsql.chain.condition;

import java.util.HashSet;
import java.util.Set;

public class CaseGroup implements Case {

	protected boolean isConnByAnd = true;

	protected Set<Case> cases = new HashSet<>();

}
