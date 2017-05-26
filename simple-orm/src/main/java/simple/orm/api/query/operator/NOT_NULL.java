package simple.orm.api.query.operator;

/**
 * 是否非空
 */
public enum NOT_NULL implements Operator {
    singleton;

    @Override
    public String value() {
        return OperatorConsts.STR_NOT_NULL;
    }
}
