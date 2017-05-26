package simple.orm.api.query;

import simple.orm.api.query.operator.Operator;

public class QueryContextPiece {

    /**
     * 字段
     */
    private String field;
    /**
     * 运算值
     */
    private Object[] values;
    /**
     * 运算操作符
     */
    private Operator operator;
    /**
     * 拼接逻辑
     */
    private LogicEnum appendType;
    public QueryContextPiece(LogicEnum appendType, String field, Operator operator, Object[] values) {
        this.field = field;
        this.values = values;
        this.operator = operator;
        this.appendType = appendType;
    }

    public static final <Z extends Object> QueryContextPiece and(String field, Operator operator, Z... value) {
        return new QueryContextPiece(LogicEnum.AND, field, operator, value);
    }

    public static final <Z extends Object> QueryContextPiece or(String field, Operator operator, Z... value) {
        return new QueryContextPiece(LogicEnum.OR, field, operator, value);
    }

    public LogicEnum getAppendType() {
        return appendType;
    }

    public String getField() {
        return field;
    }

    public Operator getOperator() {
        return operator;
    }

    public Object[] getValues() {
        return values;
    }

}
