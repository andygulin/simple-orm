package simple.orm.nsql.chain;

import simple.orm.meta.EntityMetadata;
import simple.orm.nsql.chain.condition.Case;

public class FromChain extends AbstractBuildableChain implements BuildableChain {

	protected Class<?> clazz;

	public FromChain(DeleteChain previous) {
		super(previous);
	}

	public FromChain(SelectChain previous) {
		super(previous);
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	public WhereChain where(Case condition) {
		WhereChain chain = new WhereChain(this);
		chain.conditions.add(condition);
		return chain;
	}

	public WhereChain where() {
		WhereChain chain = new WhereChain(this);
		return chain;
	}

	@Override
	protected void appendTo(StringBuffer sb) {
		String tableName = EntityMetadata.newInstance(this.clazz).getQualifiedTableName();
		sb.append(" from ").append(tableName);
	}
}
