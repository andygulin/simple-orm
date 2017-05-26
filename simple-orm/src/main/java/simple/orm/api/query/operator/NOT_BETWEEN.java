package simple.orm.api.query.operator;

/**
 * 不介于之间
 */
public enum NOT_BETWEEN implements Operator {
    singleton;

    @Override
    public String value() {
        return OperatorConsts.STR_NOT_BETWEEN;
    }

}
