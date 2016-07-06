
package simple.orm.nsql;

import org.apache.commons.lang.ArrayUtils;

import simple.orm.nsql.chain.DeleteChain;
import simple.orm.nsql.chain.SelectChain;

public class SQLBuilder {

	public static SelectChain select(String[] fields) {
		SelectChain chain = new SelectChain();
		if (ArrayUtils.isNotEmpty(fields)) {
			chain.setFields(fields);
		}
		return chain;
	}

	public static SelectChain select() {
		return select(null);
	}

	public static DeleteChain delete() {
		DeleteChain chain = new DeleteChain();
		return chain;
	}

}
