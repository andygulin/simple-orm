package simple.orm.api.query.operator;

/**
 * 不包含在
 */
public enum NOT_IN implements Operator {
	singleton;

	@Override
	public String value() {
		return OperatorConsts.STR_NOT_IN;
	}

}
