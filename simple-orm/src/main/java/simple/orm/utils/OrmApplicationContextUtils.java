package simple.orm.utils;

import org.springframework.context.ApplicationContext;

public class OrmApplicationContextUtils {

	private static ApplicationContext APPLICATION_CONTEXT;

	static void setApplicationContext(ApplicationContext applicationContext) {
		APPLICATION_CONTEXT = applicationContext;
	}

	public static ApplicationContext getApplicationContext() {
		return APPLICATION_CONTEXT;
	}
}