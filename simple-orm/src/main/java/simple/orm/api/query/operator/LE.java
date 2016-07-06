package simple.orm.api.query.operator;

/**
 * 小于等于
 */
public enum LE implements Operator {
	singleton;

	@Override
	public String value() {
		return OperatorConsts.STR_LE;
	}
}
