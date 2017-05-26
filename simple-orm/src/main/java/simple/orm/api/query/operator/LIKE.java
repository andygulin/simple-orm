package simple.orm.api.query.operator;

/**
 * 模糊匹配
 */
public enum LIKE implements Operator {
    singleton;

    @Override
    public String value() {
        return OperatorConsts.STR_LIKE;
    }
}
