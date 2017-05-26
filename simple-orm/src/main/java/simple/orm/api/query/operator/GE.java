package simple.orm.api.query.operator;

/**
 * 大于等于
 */
public enum GE implements Operator {
    singleton;

    @Override
    public String value() {
        return OperatorConsts.STR_GE;
    }
}
