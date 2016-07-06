package simple.orm.nsql.chain;

public class DeleteChain extends AbstractChain {

	public FromChain from(Class<?> clazz) {
		FromChain chain = new FromChain(this);
		chain.setClazz(clazz);
		return chain;
	}

	@Override
	protected void appendTo(StringBuffer sb) {
		sb.append("delete ");
	}
}
