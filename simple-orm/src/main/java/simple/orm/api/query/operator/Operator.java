package simple.orm.api.query.operator;

public interface Operator {
    /**
     * 等于 =
     */
    EQ eq = EQ.singleton;
    /**
     * 不等于 <>
     */
    NOT_EQ notEQ = NOT_EQ.singleton;
    /**
     * between
     */
    BETWEEN between = BETWEEN.singleton;
    /**
     * not between
     */
    NOT_BETWEEN notBetween = NOT_BETWEEN.singleton;
    /**
     * 大于 >
     */
    GT gt = GT.singleton;
    /**
     * 小于 <
     */
    LT lt = LT.singleton;
    /**
     * 大于等于 >=
     */
    GE ge = GE.singleton;
    /**
     * 小于等于 <=
     */
    LE le = LE.singleton;
    /**
     * like
     */
    LIKE like = LIKE.singleton;
    /**
     * in
     */
    IN in = IN.singleton;
    /**
     * not in
     */
    NOT_IN notIn = NOT_IN.singleton;
    /**
     * is null
     */
    NULL isNull = NULL.singleton;
    /**
     * is not null
     */
    NOT_NULL isNotNull = NOT_NULL.singleton;

    String value();

}
