package simple.orm.api.join;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.BeanUtils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class JoinResult {

    private Map<Class<?>, Object> resultMap = new LinkedHashMap<Class<?>, Object>();

    public void add(Object obj) {
        this.resultMap.put(obj.getClass(), obj);
    }

    @SuppressWarnings("unchecked")
    public <A> A get(Class<A> clazz) {
        return (A) this.resultMap.get(clazz);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(resultMap);
    }

    public <T> T newBean(Class<T> clazz) {
        try {
            T foo = clazz.newInstance();
            for (Iterator<Entry<Class<?>, Object>> it = this.resultMap.entrySet().iterator(); it.hasNext(); ) {
                Entry<Class<?>, Object> bar = it.next();
                BeanUtils.copyProperties(bar.getValue(), foo);
            }
            return foo;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}