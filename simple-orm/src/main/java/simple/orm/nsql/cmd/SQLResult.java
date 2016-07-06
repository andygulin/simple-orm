package simple.orm.nsql.cmd;

import java.util.List;
import java.util.Map;

public interface SQLResult {

	<T> T[] asArray();

	<T> List<T> asList();

	<T> T asBean();

	Map<String, Object> asMap();

	<T> T asBean(Class<T> tClass);

	<T> List<T> asList(Class<T> tClass);

	<T> T[] asArray(Class<T> tClass);

}
