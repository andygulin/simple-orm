package simple.orm.meta;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;

public class EntityMetadata<T> {

	@SuppressWarnings("rawtypes")
	private static final Map<Class, EntityMetadata> pool = new ConcurrentHashMap<Class, EntityMetadata>();

	@SuppressWarnings("unchecked")
	public static final <E> EntityMetadata<E> newInstance(Class<E> clazz) {
		EntityMetadata<E> foo;
		if (!pool.containsKey(clazz)) {
			foo = new EntityMetadata<E>(clazz);
			pool.put(clazz, foo);
			return foo;
		}
		return pool.get(clazz);
	}

	private Class<T> clazz;
	private String tableSchema;
	private String tableName;
	private String tableCatalog;
	private Map<String, EntityColumnMetadata> fieldMap = new LinkedHashMap<String, EntityColumnMetadata>();
	private Map<String, EntityColumnMetadata> columnMap = new LinkedHashMap<String, EntityColumnMetadata>();
	private Set<String> fields;
	private Set<String> columns;
	private Set<String> columnsWithQuote;
	private Set<EntityColumnMetadata> entityColumnMetadatas;
	private String primaryKey;
	private String qualifiedTableName;

	public EntityMetadata(Class<T> clazz) {
		if (clazz == null) {
			throw new RuntimeException("create entity metadata error: class cannot be null.");
		}
		this.clazz = clazz;
		this.init();
	}

	public Class<T> getClazz() {
		return clazz;
	}

	public EntityColumnMetadata getColumnByField(String fieldName) {
		return this.fieldMap.get(fieldName);
	}

	public EntityColumnMetadata getColumnByName(String columnName) {
		return this.columnMap.get(columnName);
	}

	public Set<EntityColumnMetadata> getAllColumnMetadatas() {
		return this.entityColumnMetadatas;
	}

	public Set<EntityColumnMetadata> getAllColumnMetadatasNoPK() {
		Set<EntityColumnMetadata> foo = new LinkedHashSet<EntityColumnMetadata>(this.getAllColumnMetadatas());
		foo.remove(this.getPrimaryKey());
		return foo;
	}

	public Set<String> getAllFields() {
		return this.fields;
	}

	public Set<String> getAllColumnNames() {
		return this.columns;
	}

	public Set<String> getAllColumnNamesWithQuote() {
		return this.columnsWithQuote;
	}

	public Set<String> getFieldsNoPrimaryKey() {
		Set<String> foo = new LinkedHashSet<String>(this.getAllFields());
		foo.remove(this.getPrimaryKey().getField());
		return foo;
	}

	public Set<String> getColumnNamesNoPrimaryKey() {
		Set<String> foo = new LinkedHashSet<String>(this.getAllColumnNames());
		foo.remove(this.getPrimaryKey().getName());
		return foo;
	}

	public Set<String> getColumnNamesWithQuoteNoPrimaryKey() {
		Set<String> foo = new LinkedHashSet<String>(this.getAllColumnNamesWithQuote());
		foo.remove(this.getPrimaryKey().getNameWithQuote());
		return foo;
	}

	public String getTableCatalog() {
		return tableCatalog;
	}

	public String getTableName() {
		return tableName;
	}

	public String getTableSchema() {
		return tableSchema;
	}

	private void init() {
		Entity entity = clazz.getAnnotation(Entity.class);
		if (entity == null) {
			throw new RuntimeException("class[" + clazz.getCanonicalName() + "] hasn't "
					+ Entity.class.getCanonicalName() + " annotation.");
		}
		Table annotationOfTable = clazz.getAnnotation(Table.class);
		this.tableName = annotationOfTable.name();
		this.tableSchema = annotationOfTable.schema();
		this.tableCatalog = annotationOfTable.catalog();

		Field[] declaredFields = this.clazz.getDeclaredFields();

		EntityColumnMetadata tempColumn = null;
		for (Field declaredField : declaredFields) {
			tempColumn = this.parseColumnMetadata(declaredField);
			if (tempColumn == null) {
				continue;
			} else if (tempColumn.isPrimaryKey()) {
				this.primaryKey = tempColumn.getField();
			}
			this.columnMap.put(tempColumn.getName(), tempColumn);
			this.fieldMap.put(tempColumn.getField(), tempColumn);
		}
		for (Method foo : this.clazz.getMethods()) {
			if (foo.getName().startsWith("get") && foo.getParameterTypes().length == 0
					&& foo.getReturnType() != Void.TYPE) {
				tempColumn = this.parseColumnMetadata(foo);
				if (tempColumn == null) {
					continue;
				} else if (tempColumn.isPrimaryKey()) {
					this.primaryKey = tempColumn.getField();
				}
				this.columnMap.put(tempColumn.getName(), tempColumn);
				this.fieldMap.put(tempColumn.getField(), tempColumn);
			}
		}

		this.fields = this.fieldMap.keySet();
		this.columns = this.columnMap.keySet();
		this.columnsWithQuote = new LinkedHashSet<String>();
		for (String foo : columns) {
			this.columnsWithQuote.add(foo);
		}
		this.entityColumnMetadatas = new HashSet<EntityColumnMetadata>(this.fieldMap.values());
		this.qualifiedTableName = this.createQualifiedTableName();
	}

	private String createQualifiedTableName() {
		final List<String> li = new ArrayList<String>();
		if (StringUtils.isNotBlank(this.tableSchema)) {
			li.add("`" + this.tableSchema + "`");
		}
		if (StringUtils.isNotBlank(this.tableCatalog)) {
			li.add("`" + this.tableCatalog + "`");
		}
		li.add("`" + this.tableName + "`");
		return StringUtils.join(li, ".");
	}

	public String getQualifiedTableName() {
		return this.qualifiedTableName;
	}

	public EntityColumnMetadata getPrimaryKey() {
		return this.getColumnByField(this.primaryKey);
	}

	private EntityColumnMetadata parseColumnMetadata(Method f) {
		if (f == null || f.getAnnotation(Transient.class) != null) {
			return null;
		}

		Id annoId = f.getAnnotation(Id.class);
		Column annoColumn = f.getAnnotation(Column.class);
		Lob annoLob = f.getAnnotation(Lob.class);

		if (annoColumn == null) {
			return null;
		}

		final EntityColumnMetadata result = new EntityColumnMetadata();
		result.setPrimaryKey(annoId != null);

		result.setField(StringUtils.uncapitalize(f.getName().substring(3)));
		result.setFieldType(f.getReturnType());

		result.setName(annoColumn.name());
		result.setLength(annoColumn.length());
		result.setScale(annoColumn.scale());
		result.setRequire(!annoColumn.nullable());
		result.setUnique(annoColumn.unique());
		result.setLob(annoLob != null);

		if (result.isPrimaryKey()) {
			GeneratedValue annoGen = f.getAnnotation(GeneratedValue.class);
			if (annoGen != null) {
				GenerationType strategy = annoGen.strategy();
				if (strategy == GenerationType.AUTO) {
					result.setAuto(true);
				}
			}
		}

		return result;
	}

	private EntityColumnMetadata parseColumnMetadata(Field f) {
		if (f == null || f.getAnnotation(Transient.class) != null) {
			return null;
		}

		Id annoId = f.getAnnotation(Id.class);
		Column annoColumn = f.getAnnotation(Column.class);
		Lob annoLob = f.getAnnotation(Lob.class);

		if (annoColumn == null) {
			return null;
		}

		final EntityColumnMetadata result = new EntityColumnMetadata();
		result.setPrimaryKey(annoId != null);

		result.setField(f.getName());
		result.setFieldType(f.getType());

		result.setName(annoColumn.name());
		result.setLength(annoColumn.length());
		result.setScale(annoColumn.scale());
		result.setRequire(!annoColumn.nullable());
		result.setUnique(annoColumn.unique());
		result.setLob(annoLob != null);

		if (result.isPrimaryKey()) {
			GeneratedValue annoGen = f.getAnnotation(GeneratedValue.class);
			if (annoGen != null) {
				GenerationType strategy = annoGen.strategy();
				if (strategy == GenerationType.AUTO) {
					result.setAuto(true);
				}
			}
		}

		return result;
	}

}
