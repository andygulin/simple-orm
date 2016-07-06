package simple.orm.nsql.chain;

import simple.orm.meta.EntityMetadata;

public abstract class JoinChain extends AbstractChain {

	protected JoinEnum type;
	protected Class<?> clazz;

	public JoinChain(FromChain previous, JoinEnum joinType) {
		super(previous);
		this.type = joinType;
	}

	protected void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	public OnChain on(String field1, String field2) {
		OnChain chain = new OnChain(this);
		chain.setJoinCondition(field1, field2);
		return chain;
	}

	public static enum JoinEnum {
		LEFT("left"), RIGHT("right");

		private String val;

		JoinEnum(String val) {
			this.val = val;
		}

		public String getVal() {
			return val;
		}
	}

	@Override
	protected void appendTo(StringBuffer sb) {
		EntityMetadata<?> emd = EntityMetadata.newInstance(this.clazz);
		sb.append(" ").append(this.type.getVal()).append(" join ").append(emd.getQualifiedTableName()).append(" ");
	}
}
