package simple.orm.nsql.chain;

import simple.orm.meta.EntityMetadata;
import simple.orm.nsql.chain.condition.Case;

public class OnChain extends AbstractBuildableChain implements BuildableChain {

	protected String onField1;
	protected String onField2;

	public OnChain(JoinChain previous) {
		super(previous);
	}

	protected void setJoinCondition(String onField1, String onField2) {
		this.onField1 = onField1;
		this.onField2 = onField2;
	}

	public WhereChain where() {
		WhereChain chain = new WhereChain(this);
		return chain;
	}

	public WhereChain where(Case caze) {
		WhereChain chain = new WhereChain(this);
		chain.conditions.add(caze);
		return chain;
	}

	@Override
	protected void appendTo(StringBuffer sb) {
		sb.append(" on ");

		JoinChain joinChain = (JoinChain) this.previous;
		Class<?> clazz2 = joinChain.clazz;
		FromChain fromChain = (FromChain) joinChain.previous;
		Class<?> clazz1 = fromChain.clazz;
		EntityMetadata<?> emd1 = EntityMetadata.newInstance(clazz1);
		EntityMetadata<?> emd2 = EntityMetadata.newInstance(clazz2);
		sb.append(emd1.getQualifiedTableName()).append(".")
				.append(emd1.getColumnByField(this.onField1).getNameWithQuote());
		sb.append(" = ");
		sb.append(emd2.getQualifiedTableName()).append(".")
				.append(emd2.getColumnByField(this.onField2).getNameWithQuote());
		sb.append(" ");
	}
}
