package simple.orm.api.query.operator;

/**
 * 包含
 */
public enum IN implements Operator {
	singleton;

	@Override
	public String value() {
		return OperatorConsts.STR_IN;
	}
}
