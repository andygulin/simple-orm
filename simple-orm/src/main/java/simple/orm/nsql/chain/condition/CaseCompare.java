package simple.orm.nsql.chain.condition;

public class CaseCompare extends AbstractFieldCase {

	public static enum Comparator {
		equal("="), greater(">"), less("<"), equalOrGreater(">="), equalOrLess("<="), like("like");

		private String val;

		Comparator(String val) {
			this.val = val;
		}

		public String getVal() {
			return this.val;
		}
	}

	private Comparator comparator;
	private Object value;

	public Comparator getComparator() {
		return comparator;
	}

	public void setComparator(Comparator comparator) {
		this.comparator = comparator;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
