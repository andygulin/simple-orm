package simple.orm.nsql.chain;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import simple.orm.meta.EntityMetadata;
import simple.orm.nsql.chain.condition.Case;
import simple.orm.nsql.chain.condition.CaseBetween;
import simple.orm.nsql.chain.condition.CaseCompare;
import simple.orm.nsql.chain.condition.CaseGroup;
import simple.orm.nsql.chain.condition.CaseIn;
import simple.orm.nsql.chain.condition.CaseIsNull;

public class WhereChain extends AbstractBuildableChain implements BuildableChain {

	protected LinkedList<Case> conditions = new LinkedList<>();

	private Map<Case, String> opMap = new HashMap<>();

	protected List<Object> params;

	public WhereChain(FromChain previous) {
		super(previous);
	}

	public WhereChain(OnChain previous) {
		super(previous);
	}

	public WhereChain and(Case condition) {
		this.conditions.add(condition);
		this.opMap.put(condition, "and");
		return this;
	}

	public WhereChain or(Case condition) {
		this.conditions.add(condition);
		this.opMap.put(condition, "or");
		return this;
	}

	public OrderChain order() {
		OrderChain chain = new OrderChain(this);
		return chain;
	}

	@Override
	protected void appendTo(StringBuffer sb) {
		sb.append(" where ");

		if (CollectionUtils.isEmpty(this.conditions)) {
			sb.append(" 1=1 ");
			return;
		}

		AbstractChain p = this.previous;

		Class<?> clazz1 = null;
		Class<?> clazz2 = null;

		boolean hasJoin = false;

		if (p instanceof FromChain) {
			clazz1 = ((FromChain) p).clazz;
		} else if (p instanceof OnChain) {
			hasJoin = true;
			JoinChain joinChain = (JoinChain) ((OnChain) p).previous;
			clazz2 = joinChain.clazz;
			clazz1 = ((FromChain) joinChain.previous).clazz;
		}
		List<Object> params = new LinkedList<>();
		for (Case c : this.conditions) {
			sb.append(" ").append(this.opMap.containsKey(c) ? this.opMap.get(c) : StringUtils.EMPTY).append(" (");
			this.parseCase(c, clazz1, sb, params);
			sb.append(") ");
		}

		this.params = params;
	}

	private void parseCase(Case c, Class<?> clazz, StringBuffer sb, List<Object> params) {
		EntityMetadata<?> emd = EntityMetadata.newInstance(clazz);

		if (c instanceof CaseCompare) {
			CaseCompare foo = (CaseCompare) c;
			sb.append(emd.getQualifiedTableName()).append(".")
					.append(emd.getColumnByField(foo.getField()).getNameWithQuote());
			sb.append(foo.getComparator().getVal());
			sb.append(" ?");
			params.add(foo.getValue());
		} else if (c instanceof CaseBetween) {
			CaseBetween foo = (CaseBetween) c;
			sb.append(emd.getQualifiedTableName()).append(".")
					.append(emd.getColumnByField(foo.getField()).getNameWithQuote());
			sb.append(" between ? and ?");
			params.add(foo.getFrom());
			params.add(foo.getTo());
		} else if (c instanceof CaseIn) {
			CaseIn foo = (CaseIn) c;
			sb.append(emd.getQualifiedTableName()).append(".")
					.append(emd.getColumnByField(foo.getField()).getNameWithQuote());
			sb.append(" in (");

			Set<Object> values = foo.getValues();

			for (int i = 0, len = values.size(); i < len; i++) {
				sb.append(i != len - 1 ? "?," : "?)");
			}
			params.addAll(values);
		} else if (c instanceof CaseIsNull) {
			CaseIsNull foo = (CaseIsNull) c;
			sb.append(emd.getQualifiedTableName()).append(".")
					.append(emd.getColumnByField(foo.getField()).getNameWithQuote());
			sb.append(" is null");
		} else if (c instanceof CaseGroup) {

		} else {

		}
	}
}