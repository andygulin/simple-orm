package simple.orm.api.query;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import simple.orm.api.query.operator.BETWEEN;
import simple.orm.api.query.operator.EQ;
import simple.orm.api.query.operator.GE;
import simple.orm.api.query.operator.GT;
import simple.orm.api.query.operator.IN;
import simple.orm.api.query.operator.LE;
import simple.orm.api.query.operator.LIKE;
import simple.orm.api.query.operator.LT;
import simple.orm.api.query.operator.NOT_BETWEEN;
import simple.orm.api.query.operator.NOT_EQ;
import simple.orm.api.query.operator.NOT_IN;
import simple.orm.api.query.operator.NOT_NULL;
import simple.orm.api.query.operator.NULL;
import simple.orm.api.query.operator.Operator;

public class QueryContext implements Iterable<QueryContextPiece> {

	private Set<QueryContextPiece> pieces = new LinkedHashSet<QueryContextPiece>();

	public QueryContext clear() {
		this.pieces.clear();
		return this;
	}

	public QueryContext andEquals(final String field, final Object value) {
		return this.and(field, Operator.eq, value);
	}

	public QueryContext orEquals(final String field, final Object value) {
		return this.or(field, Operator.eq, value);
	}

	private void add(QueryContextPiece piece) {
		this.pieces.add(piece);
	}

	public QueryContext and(final String field, EQ operator, final Object value) {
		this.add(QueryContextPiece.and(field, operator, value));
		return this;
	}

	public QueryContext and(final String field, NOT_EQ operator, final Object value) {
		this.add(QueryContextPiece.and(field, operator, value));
		return this;
	}

	public QueryContext and(final String field, LT operator, final Object value) {
		this.add(QueryContextPiece.and(field, operator, value));
		return this;
	}

	public QueryContext and(final String field, GT operator, final Object value) {
		this.add(QueryContextPiece.and(field, operator, value));
		return this;
	}

	public QueryContext and(final String field, LE operator, final Object value) {
		this.add(QueryContextPiece.and(field, operator, value));
		return this;
	}

	public QueryContext and(final String field, GE operator, final Object value) {
		this.add(QueryContextPiece.and(field, operator, value));
		return this;
	}

	public QueryContext and(final String field, NULL operator) {
		this.add(QueryContextPiece.and(field, operator));
		return this;
	}

	public QueryContext and(final String field, NOT_NULL operator) {
		this.add(QueryContextPiece.and(field, operator));
		return this;
	}

	public QueryContext and(final String field, LIKE operator, final Object value) {
		this.add(QueryContextPiece.and(field, operator, value));
		return this;
	}

	public <Z extends Object> QueryContext and(final String field, IN operator, final Z... value) {
		this.add(QueryContextPiece.and(field, operator, value));
		return this;
	}

	public <Z extends Object> QueryContext and(final String field, NOT_IN operator, final Z... value) {
		this.add(QueryContextPiece.and(field, operator, value));
		return this;
	}

	public QueryContext and(final String field, BETWEEN operator, final Object value1, final Object value2) {
		this.add(QueryContextPiece.and(field, operator, value1, value2));
		return this;
	}

	public QueryContext and(final String field, NOT_BETWEEN operator, final Object value1, final Object value2) {
		this.add(QueryContextPiece.and(field, operator, value1, value2));
		return this;
	}

	public QueryContext or(final String field, EQ operator, final Object value) {
		this.add(QueryContextPiece.or(field, operator, value));
		return this;
	}

	public QueryContext or(final String field, NOT_EQ operator, final Object value) {
		this.add(QueryContextPiece.or(field, operator, value));
		return this;
	}

	public QueryContext or(final String field, LT operator, final Object value) {
		this.add(QueryContextPiece.or(field, operator, value));
		return this;
	}

	public QueryContext or(final String field, GT operator, final Object value) {
		this.add(QueryContextPiece.or(field, operator, value));
		return this;
	}

	public QueryContext or(final String field, LE operator, final Object value) {
		this.add(QueryContextPiece.or(field, operator, value));
		return this;
	}

	public QueryContext or(final String field, GE operator, final Object value) {
		this.add(QueryContextPiece.or(field, operator, value));
		return this;
	}

	public QueryContext or(final String field, NULL operator) {
		this.add(QueryContextPiece.or(field, operator));
		return this;
	}

	public QueryContext or(final String field, NOT_NULL operator) {
		this.add(QueryContextPiece.or(field, operator));
		return this;
	}

	public QueryContext or(final String field, LIKE operator, final Object value) {
		this.add(QueryContextPiece.or(field, operator, value));
		return this;
	}

	public <Z extends Object> QueryContext or(final String field, IN operator, final Z... value) {
		this.add(QueryContextPiece.or(field, operator, value));
		return this;
	}

	public <Z extends Object> QueryContext or(final String field, NOT_IN operator, final Z... value) {
		this.add(QueryContextPiece.or(field, operator, value));
		return this;
	}

	public QueryContext or(final String field, BETWEEN operator, final Object value1, final Object value2) {
		this.add(QueryContextPiece.or(field, operator, value1, value2));
		return this;
	}

	public QueryContext or(final String field, NOT_BETWEEN operator, final Object value1, final Object value2) {
		this.add(QueryContextPiece.or(field, operator, value1, value2));
		return this;
	}

	@Override
	public Iterator<QueryContextPiece> iterator() {
		return this.pieces.iterator();
	}

}
