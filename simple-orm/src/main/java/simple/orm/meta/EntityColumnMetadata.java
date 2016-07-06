package simple.orm.meta;

import com.alibaba.fastjson.JSON;

public class EntityColumnMetadata {

	private String field;// 实体类成员名
	private String name;// 表字段名
	private Class<?> fieldType;// 类型
	private int length;
	private int scale;
	private boolean require = false;
	private boolean unique = false;
	private boolean lazy = false;
	private boolean lob = false;
	private boolean primaryKey = false;
	/**
	 * 是否自动创建主键
	 */
	private boolean auto = false;

	public String getField() {
		return field;
	}

	public Class<?> getFieldType() {
		return fieldType;
	}

	public int getLength() {
		return length;
	}

	public String getName() {
		return name;
	}

	public String getNameWithQuote() {
		return "`" + this.name + "`";
	}

	public int getScale() {
		return scale;
	}

	public boolean isAuto() {
		return auto;
	}

	public boolean isLazy() {
		return lazy;
	}

	public boolean isLob() {
		return lob;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public boolean isRequire() {
		return require;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setAuto(boolean auto) {
		this.auto = auto;
	}

	public void setField(String field) {
		this.field = field;
	}

	public void setFieldType(Class<?> fieldType) {
		this.fieldType = fieldType;
	}

	public void setLazy(boolean lazy) {
		this.lazy = lazy;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void setLob(boolean lob) {
		this.lob = lob;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	public void setRequire(boolean require) {
		this.require = require;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}
