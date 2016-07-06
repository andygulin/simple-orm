package simple.orm.nsql.chain.condition;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class CaseIn extends AbstractFieldCase {

	public Set<Object> getValues() {
		return values;
	}

	protected Set<Object> values = new LinkedHashSet<>();

	public void append(Object value) {
		this.values.add(value);
	}

	public void appendAll(Collection<?> values) {
		this.values.addAll(values);
	}

	public void appendAll(Object[] values) {
		for (Object foo : values) {
			this.append(foo);
		}
	}

}
