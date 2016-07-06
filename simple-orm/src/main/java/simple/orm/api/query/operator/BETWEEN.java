package simple.orm.api.query.operator;

/**
 * 介于之间
 */
public enum BETWEEN implements Operator {
	singleton;

	@Override
	public String value() {
		return OperatorConsts.STR_BETWEEN;
	}

}
