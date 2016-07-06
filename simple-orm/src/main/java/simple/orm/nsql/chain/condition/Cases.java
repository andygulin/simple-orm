package simple.orm.nsql.chain.condition;

import java.util.Collection;

import simple.orm.nsql.chain.condition.CaseCompare.Comparator;

public abstract class Cases {

	public static final CompareBuilder compare(String field) {
		return new CompareBuilder(field);
	}

	public static final IsNullBuilder isNull(String field) {
		return new IsNullBuilder(field);
	}

	public static final InBuilder in(String field) {
		return new InBuilder(field);
	}

	public static final BetweenBuilder between(String field) {
		return new BetweenBuilder(field);
	}

	public static final GroupBuilder group(boolean isAnd) {
		return new GroupBuilder(isAnd);
	}

	public static final class GroupBuilder {

		private CaseGroup ca = new CaseGroup();

		public GroupBuilder(boolean isAnd) {
			this.ca.isConnByAnd = isAnd;
		}

		public GroupBuilder append(Case caze) {
			this.ca.cases.add(caze);
			return this;
		}

		public GroupBuilder appendAll(Collection<Case> cazes) {
			this.ca.cases.addAll(cazes);
			return this;
		}

		public GroupBuilder appendAll(Case[] cazes) {
			for (Case foo : cazes) {
				this.ca.cases.add(foo);
			}
			return this;
		}

		public CaseGroup build() {
			return this.ca;
		}

	}

	public static final class BetweenBuilder {

		private CaseBetween ca = new CaseBetween();

		public BetweenBuilder(String field) {
			this.ca.field = field;
		}

		public BetweenBuilder from(Object from) {
			this.ca.from = from;
			return this;
		}

		public BetweenBuilder to(Object to) {
			this.ca.to = to;
			return this;
		}

		public CaseBetween build() {
			return this.ca;
		}

	}

	public static final class InBuilder {

		private CaseIn ca = new CaseIn();

		public InBuilder(String field) {
			this.ca.field = field;
		}

		public InBuilder append(Object... values) {
			for (Object foo : values) {
				this.ca.append(foo);
			}
			return this;
		}

		public InBuilder appendAll(Collection<?> values) {
			this.ca.appendAll(values);
			return this;
		}

		public InBuilder appendAll(Object[] values) {
			this.ca.appendAll(values);
			return this;
		}

		public CaseIn build() {
			return this.ca;
		}

	}

	public static final class IsNullBuilder {

		private CaseIsNull caze;

		public IsNullBuilder(String field) {
			this.caze = new CaseIsNull();
			this.caze.field = field;
		}

		public CaseIsNull build() {
			return caze;
		}

	}

	public static final class CompareBuilder {

		private CaseCompare caze = new CaseCompare();

		public CompareBuilder(String field) {
			this.caze.field = field;
		}

		public CompareBuilder greaterThan(Object value) {
			this.caze.setComparator(Comparator.greater);
			this.caze.setValue(value);
			return this;
		}

		public CompareBuilder equal(Object value) {
			this.caze.setComparator(Comparator.equal);
			this.caze.setValue(value);
			return this;
		}

		public CompareBuilder equalOrGreaterThan(Object value) {
			this.caze.setComparator(Comparator.equalOrGreater);
			this.caze.setValue(value);
			return this;
		}

		public CompareBuilder lessThan(Object value) {
			this.caze.setComparator(Comparator.less);
			this.caze.setValue(value);
			return this;
		}

		public CompareBuilder equalOrLessThan(Object value) {
			this.caze.setComparator(Comparator.equalOrLess);
			this.caze.setValue(value);
			return this;
		}

		public CompareBuilder like(String value) {
			this.caze.setComparator(Comparator.like);
			this.caze.setValue(value);
			return this;
		}

		public CaseCompare build() {
			return this.caze;
		}

	}

}
