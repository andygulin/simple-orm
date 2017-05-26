package simple.orm.api.query.operator;

/**
 * 大于
 */
public enum GT implements Operator {
    singleton;

    @Override
    public String value() {
        return OperatorConsts.STR_GT;
    }
}
