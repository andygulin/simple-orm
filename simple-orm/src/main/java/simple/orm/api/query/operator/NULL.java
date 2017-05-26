package simple.orm.api.query.operator;

/**
 * 是否空
 */
public enum NULL implements Operator {
    singleton;

    @Override
    public String value() {
        return OperatorConsts.STR_NULL;
    }
}
