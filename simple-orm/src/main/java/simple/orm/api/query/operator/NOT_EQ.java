package simple.orm.api.query.operator;

/**
 * 不等于
 */
public enum NOT_EQ implements Operator {
    singleton;

    @Override
    public String value() {
        return OperatorConsts.STR_NOT_EQ;
    }
}
