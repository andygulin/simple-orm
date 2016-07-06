package simple.orm.nsql.chain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import simple.orm.meta.EntityColumnMetadata;
import simple.orm.meta.EntityMetadata;

public class SelectChain extends AbstractChain {

	private String[] fields;

	public String[] getFields() {
		return fields;
	}

	public void setFields(String[] fields) {
		this.fields = fields;
	}

	public JoinableFromChain from(Class<?> clazz) {
		JoinableFromChain chain = new JoinableFromChain(this);
		chain.setClazz(clazz);
		return chain;
	}

	@Override
	protected void appendTo(StringBuffer sb) {
		sb.append("select ");

		boolean hasJoin = false;

		AbstractChain ne = this.next;
		while (ne != null) {
			if (ne instanceof JoinChain) {
				hasJoin = true;
				break;
			}
			ne = ne.next;
		}

		if (hasJoin) {
			this.appendToWithJoin(sb);
		} else {
			this.appendToWithoutJoin(sb);
		}
		sb.append(" ");
	}

	private void appendToWithJoin(StringBuffer sb) {
		FromChain fromChain = (FromChain) this.next;
		JoinChain joinChain = (JoinChain) fromChain.next;

		EntityMetadata<?> emd1 = EntityMetadata.newInstance(fromChain.clazz);
		EntityMetadata<?> emd2 = EntityMetadata.newInstance(joinChain.clazz);

		if (ArrayUtils.isEmpty(this.fields)) {
			Set<String> cols = emd1.getAllColumnNamesWithQuote();
			List<String> cols2 = new ArrayList<>();
			for (String foo : cols) {
				cols2.add(emd1.getQualifiedTableName() + "." + foo);
			}
			sb.append(StringUtils.join(cols2, ","));
			sb.append(",");

			cols = emd2.getAllColumnNamesWithQuote();
			cols2.clear();
			for (String foo : cols) {
				cols2.add(emd2.getQualifiedTableName() + "." + foo);
			}
			sb.append(StringUtils.join(cols2, ","));
		} else {

		}
	}

	private void appendToWithoutJoin(StringBuffer sb) {
		JoinableFromChain fromChain = (JoinableFromChain) this.next;
		EntityMetadata<?> foo = EntityMetadata.newInstance(fromChain.clazz);
		if (ArrayUtils.isEmpty(this.fields)) {
			sb.append(StringUtils.join(foo.getAllColumnNamesWithQuote(), ","));
			return;
		}

		for (int i = 0, len = this.fields.length; i < len; i++) {
			String field = this.fields[i];
			EntityColumnMetadata col = foo.getColumnByField(field);
			sb.append(col.getNameWithQuote());
			sb.append(i != len - 1 ? ", " : " ");
		}
	}

}
