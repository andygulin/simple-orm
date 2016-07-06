package simple.orm.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.MapUtils;

import simple.orm.api.CommonDao;
import simple.orm.api.DaoSupport;

public class DaoUtils {

	@SuppressWarnings("rawtypes")
	private static transient final Map<Class, DaoSupport> daocache = new ConcurrentHashMap<Class, DaoSupport>();

	public static final CommonDao getCommonDao() {
		return OrmApplicationContextUtils.getApplicationContext().getBean(CommonDao.class);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final <T> DaoSupport<T> getDaoSupport(Class<T> clazz) {

		if (MapUtils.isEmpty(daocache)) {
			Map<String, DaoSupport> tmpMap = OrmApplicationContextUtils.getApplicationContext()
					.getBeansOfType(DaoSupport.class);
			for (DaoSupport foo : tmpMap.values()) {
				daocache.put(foo.getEntityClass(), foo);
			}
		}
		return daocache.get(clazz);
	}

}
