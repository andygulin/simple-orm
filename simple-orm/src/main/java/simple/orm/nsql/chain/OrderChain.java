package simple.orm.nsql.chain;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import simple.orm.meta.EntityMetadata;

public class OrderChain extends AbstractBuildableChain implements BuildableChain {

	private List<OrderPicec> orderList = new ArrayList<>();

	public OrderChain(FromChain previous) {
		super(previous);
	}

	public OrderChain(WhereChain previous) {
		super(previous);
	}

	@Override
	protected void appendTo(StringBuffer sb) {
		if (CollectionUtils.isEmpty(this.orderList)) {
			return;
		}
		sb.append(" order by ");

		boolean hasJoin = false;

		AbstractChain prev = this.previous;

		Class<?> defaultClazz = null;

		while (prev != null) {
			if (prev instanceof JoinChain) {
				hasJoin = true;
				break;
			} else {
				prev = prev.previous;
			}

			if (prev instanceof FromChain) {
				defaultClazz = ((FromChain) prev).clazz;
			}

		}

		List<String> fooArr = new LinkedList<>();
		for (OrderPicec op : orderList) {
			StringBuffer tmpSb = new StringBuffer();
			EntityMetadata<?> emd = EntityMetadata.newInstance(op.getClazz() == null ? defaultClazz : op.getClazz());
			tmpSb.append(" ");
			if (hasJoin) {
				tmpSb.append(emd.getQualifiedTableName()).append(".");
			}
			tmpSb.append(emd.getColumnByField(op.getField()).getNameWithQuote());
			tmpSb.append(" ").append(op.isAsc() ? "asc" : "desc");
			fooArr.add(tmpSb.toString());
		}
		sb.append(StringUtils.join(fooArr, ","));
	}

	public OrderChain asc(Class<?> clazz, String field) {
		this.orderList.add(new OrderPicec(clazz, field, true));
		return this;
	}

	public OrderChain asc(String field) {
		this.orderList.add(new OrderPicec(null, field, true));
		return this;
	}

	public OrderChain desc(Class<?> clazz, String field) {
		this.orderList.add(new OrderPicec(clazz, field, false));
		return this;
	}

	public OrderChain desc(String field) {
		this.orderList.add(new OrderPicec(null, field, false));
		return this;
	}

	private static class OrderPicec {

		private Class<?> clazz;

		private String field;
		private boolean asc;

		public OrderPicec(Class<?> clazz, String field, boolean asc) {
			this.clazz = clazz;
			this.field = field;
			this.asc = asc;
		}

		public Class<?> getClazz() {
			return clazz;
		}

		public String getField() {
			return field;
		}

		public boolean isAsc() {
			return asc;
		}

	}

}
