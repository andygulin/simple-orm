package simple.orm.api.query.operator;

/**
 * 等于
 */
public enum EQ implements Operator {
	singleton;

	@Override
	public String value() {
		return OperatorConsts.STR_EQ;
	}

}
