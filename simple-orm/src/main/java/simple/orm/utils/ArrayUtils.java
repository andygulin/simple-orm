package simple.orm.utils;

import java.lang.reflect.Array;
import java.util.Collection;

public class ArrayUtils {

	@SuppressWarnings("unchecked")
	public static final <T> T[] toArray(final Collection<T> collection, final Class<T> clazz) {
		if (collection == null) {
			return null;
		}
		final T[] arr = (T[]) Array.newInstance(clazz, collection.size());
		return collection.toArray(arr);
	}
}
