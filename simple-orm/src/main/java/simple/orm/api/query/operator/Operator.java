package simple.orm.api.query.operator;

public interface Operator {
	String value();

	/**
	 * 等于 =
	 */
	static final EQ eq = EQ.singleton;
	/**
	 * 不等于 <>
	 */
	static final NOT_EQ notEQ = NOT_EQ.singleton;
	/**
	 * between
	 */
	static final BETWEEN between = BETWEEN.singleton;
	/**
	 * not between
	 */
	static final NOT_BETWEEN notBetween = NOT_BETWEEN.singleton;
	/**
	 * 大于 >
	 */
	static final GT gt = GT.singleton;
	/**
	 * 小于 <
	 */
	static final LT lt = LT.singleton;
	/**
	 * 大于等于 >=
	 */
	static final GE ge = GE.singleton;
	/**
	 * 小于等于 <=
	 */
	static final LE le = LE.singleton;
	/**
	 * like
	 */
	static final LIKE like = LIKE.singleton;
	/**
	 * in
	 */
	static final IN in = IN.singleton;
	/**
	 * not in
	 */
	static final NOT_IN notIn = NOT_IN.singleton;
	/**
	 * is null
	 */
	static final NULL isNull = NULL.singleton;
	/**
	 * is not null
	 */
	static final NOT_NULL isNotNull = NOT_NULL.singleton;

}
