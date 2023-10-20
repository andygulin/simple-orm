package simple.orm.utils;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class ObjectUtils {

    private static final Class<?>[] BASIC_NUMBER_CLASSES = new Class[]{short.class, int.class, long.class,
            float.class, double.class};

    @SuppressWarnings("unchecked")
    public static final <T> T clone(T t) {
        if (t == null) {
            return null;
        }
        if (t instanceof Serializable) {
            return (T) SerializationUtils.clone((Serializable) t);
        }
        T result = null;
        if (t instanceof Cloneable) {
            try {
                result = (T) org.apache.commons.lang.ObjectUtils.clone(t);
            } catch (Throwable e) {
            }
        }
        if (result == null) {
            String json = JSON.toJSONString(t);
            result = (T) JSON.parseObject(json, t.getClass());
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    private static final Object getFieldValue(Object obj, String field) throws Exception {
        Object result = null;
        if (obj instanceof Map) {
            return ((Map) obj).get(field);
        }

        if (obj == null) {
            return null;
        }

        Method getterMethod = null;
        try {
            getterMethod = obj.getClass().getMethod("get" + StringUtils.capitalize(field));
        } catch (Exception e) {
        }
        if (getterMethod == null) {
            try {
                getterMethod = obj.getClass().getMethod("is" + StringUtils.capitalize(field));
            } catch (Exception e) {
            }
        }
        if (getterMethod == null) {
            Field privateField;
            try {
                privateField = obj.getClass().getDeclaredField(field);
                privateField.setAccessible(true);
                result = privateField.get(obj);
            } catch (Exception e) {
                throw new Exception("field[" + field + "] doesn't exist.");
            }
        } else {
            try {
                result = getterMethod.invoke(obj);
            } catch (Exception e) {
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static final <T> T getValue(Object obj, Class<T> clazz, String path) {
        Object o = getValue(obj, path);
        return o == null ? null : (T) o;
    }

    public static final Object getValue(Object obj, String path) {
        if (obj == null || StringUtils.isBlank(path)) {
            return null;
        }
        String[] arr = StringUtils.split(path, ".");
        Object o = obj;
        for (int i = 0, len = arr.length; i < len; i++) {
            final String field = StringUtils.strip(arr[i]);
            try {
                o = getFieldValue(o, field);
            } catch (Exception e) {
                o = null;
            }
        }
        return o;
    }

    public static final boolean isNumberType(Object obj) {
        if (obj == null) {
            throw new RuntimeException("object is null.");
        }
        if (obj instanceof Number) {
            return true;
        } else {
            for (Class<?> clazz : BASIC_NUMBER_CLASSES) {
                if (obj.getClass().equals(clazz)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static final boolean isZero(Object obj) {
        if (!isNumberType(obj)) {
            return false;
        }
        final String foo = String.valueOf(obj);
        return StringUtils.equals(foo, "0") || StringUtils.equals(foo, "0.0");
    }

    public static final <T> T fromMap(final Map<String, Object> map, Class<T> clazz) {
        return JSON.parseObject(JSON.toJSONString(map), clazz);
    }

    public static final Map<String, Object> toMap(final Object object) {
        return JSON.parseObject(JSON.toJSONString(object));
    }

    @SuppressWarnings("rawtypes")
    public static final <T> void setValue(final Object object, final String field, final T value,
                                          final Class paramType) {
        try {
            Method md = object.getClass().getMethod("set" + StringUtils.capitalize(field), paramType);
            if (md != null) {
                md.invoke(object, value);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static final <T> void setValue(final Object object, final String field, final T value) {
        try {
            for (Method method : object.getClass().getMethods()) {
                if (StringUtils.equals(method.getName(), "set" + StringUtils.capitalize(field))) {
                    method.invoke(object, value);
                    break;
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}