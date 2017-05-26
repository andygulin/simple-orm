package simple.orm.api.query.operator;

/**
 * 小于
 */
public enum LT implements Operator {
    singleton;

    @Override
    public String value() {
        return OperatorConsts.STR_LT;
    }
}
