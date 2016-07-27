package simple.orm.nsql.cmd;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import com.alibaba.fastjson.JSON;

import simple.orm.meta.EntityMetadata;

public class MappedSQLResult implements SQLResult {

	private List<Map<String, Object>> data;
	private Map<String, Class<?>> classMap;
	private boolean isSingleBean;

	private Class<?> singleClass;

	public MappedSQLResult(List<Map<String, Object>> data, Map<String, Class<?>> classMap) {
		this.data = data;
		this.classMap = classMap;
		this.isSingleBean = MapUtils.isNotEmpty(this.classMap) && this.classMap.size() == 1;
		if (isSingleBean) {
			this.singleClass = this.classMap.entrySet().iterator().next().getValue();
		}
	}

	private void singleCheck() {
		if (!isSingleBean) {
			throw new RuntimeException("convert single bean error: SQLResult contains multi beans.");
		}
	}

	@Override
	public <T> T[] asArray() {
		this.singleCheck();
		return (T[]) this.asArray(this.singleClass);
	}

	@Override
	public <T> List<T> asList() {
		this.singleCheck();
		return (List<T>) this.asList(this.singleClass);
	}

	@Override
	public <T> T asBean() {
		this.singleCheck();
		return (T) this.asBean(this.singleClass);
	}

	@Override
	public Map<String, Object> asMap() {
		return null;
	}

	private <T> T convert(Map<String, Object> map, Class<T> tClass) {
		EntityMetadata<T> em = EntityMetadata.newInstance(tClass);
		final Map<String, Object> fooMap = new HashMap<>();
		final String lTableName = em.getTableName().toLowerCase();
		for (Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String, Object> entry = it.next();
			final String key = entry.getKey();
			final Object value = entry.getValue();

			int pointIndex = key.indexOf(".");
			if (pointIndex == -1) {
				fooMap.put(em.getColumnByName(key).getField(), value);
			} else if (key.toLowerCase().startsWith(lTableName)) {
				fooMap.put(em.getColumnByName(key.substring(lTableName.length() + 1)).getField(), value);
			}
		}
		String json = JSON.toJSONString(fooMap);
		return JSON.parseObject(json, tClass);
	}

	@Override
	public <T> T asBean(Class<T> tClass) {
		return null;
	}

	@Override
	public <T> List<T> asList(Class<T> tClass) {
		List<T> tlist = new LinkedList<>();
		for (Map<String, Object> row : this.data) {
			T t = this.convert(row, tClass);
			tlist.add(t);
		}
		return tlist;
	}

	@Override
	public <T> T[] asArray(Class<T> tClass) {
		return null;
	}

	@Override
	public String toString() {
		boolean hasHeaderPrint = false;
		StringBuffer sb = new StringBuffer();
		for (Map<String, Object> map : data) {
			if (!hasHeaderPrint) {
				for (String key : map.keySet()) {
					sb.append(key).append("\t|");
				}
				sb.append("\n");
			}
			for (Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, Object> entry = it.next();
				sb.append(entry.getValue()).append("\t|");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}