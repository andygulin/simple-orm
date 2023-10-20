package simple.orm.api.query;

import org.apache.commons.lang.StringUtils;

public class Order {
    private final boolean asc;
    private final String field;

    private Order(boolean isAsc, String field) {
        if (StringUtils.isBlank(field)) {
            throw new RuntimeException("create order error: field is blank.");
        }
        this.field = field;
        this.asc = isAsc;
    }

    public static final Order asc(final String field) {
        return new Order(true, field);
    }

    public static final Order desc(final String field) {
        return new Order(false, field);
    }

    public boolean isAsc() {
        return asc;
    }

    public String getField() {
        return field;
    }
}
