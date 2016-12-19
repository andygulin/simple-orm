
package simple.orm.jdbc.ext;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.*;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.Assert;
import simple.orm.meta.EntityColumnMetadata;
import simple.orm.meta.EntityMetadata;
import simple.orm.utils.ObjectUtils;

import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NRowMapper<T> implements RowMapper<T> {

	protected final Log logger = LogFactory.getLog(getClass());

	private Class<T> mappedClass;

	private boolean checkFullyPopulated = false;

	private boolean primitivesDefaultedForNullValue = false;

	private Map<String, PropertyDescriptor> mappedFields;

	private Set<String> mappedProperties;

	public NRowMapper(LobHandler lobHandler, Class<T> mappedClass) {
		initialize(mappedClass);
		this.metadata = EntityMetadata.newInstance(mappedClass);
		this.lobHandler = lobHandler;
	}

	protected EntityMetadata<T> metadata;
	protected LobHandler lobHandler;

	public void setMappedClass(Class<T> mappedClass) {
		if (this.mappedClass == null) {
			initialize(mappedClass);
		} else {
			if (!this.mappedClass.equals(mappedClass)) {
				throw new InvalidDataAccessApiUsageException("The mapped class can not be reassigned to map to "
						+ mappedClass + " since it is already providing mapping for " + this.mappedClass);
			}
		}
	}

	protected void initialize(Class<T> mappedClass) {
		this.mappedClass = mappedClass;
		this.mappedFields = new HashMap<>();
		this.mappedProperties = new HashSet<>();
		PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(mappedClass);
		for (PropertyDescriptor pd : pds) {
			if (pd.getWriteMethod() != null) {
				this.mappedFields.put(pd.getName().toLowerCase(), pd);
				String underscoredName = underscoreName(pd.getName());
				if (!pd.getName().toLowerCase().equals(underscoredName)) {
					this.mappedFields.put(underscoredName, pd);
				}
				this.mappedProperties.add(pd.getName());
			}
		}
	}

	private String underscoreName(String name) {
		StringBuilder result = new StringBuilder();
		if (name != null && name.length() > 0) {
			result.append(name.substring(0, 1).toLowerCase());
			for (int i = 1; i < name.length(); i++) {
				String s = name.substring(i, i + 1);
				if (s.equals(s.toUpperCase())) {
					result.append("_");
					result.append(s.toLowerCase());
				} else {
					result.append(s);
				}
			}
		}
		return result.toString();
	}

	public final Class<T> getMappedClass() {
		return this.mappedClass;
	}

	public void setCheckFullyPopulated(boolean checkFullyPopulated) {
		this.checkFullyPopulated = checkFullyPopulated;
	}

	public boolean isCheckFullyPopulated() {
		return this.checkFullyPopulated;
	}

	public void setPrimitivesDefaultedForNullValue(boolean primitivesDefaultedForNullValue) {
		this.primitivesDefaultedForNullValue = primitivesDefaultedForNullValue;
	}

	public boolean isPrimitivesDefaultedForNullValue() {
		return primitivesDefaultedForNullValue;
	}

	private void processLob(T t, ResultSet rs, int i, EntityColumnMetadata md) throws SQLException {
		boolean isLazy = md.isLazy();
		if (!isLazy) {
			if (ClassUtils.isAssignable(md.getFieldType(), InputStream.class)) {
				ObjectUtils.setValue(t, md.getField(), this.lobHandler.getBlobAsBinaryStream(rs, i));
			} else if (ClassUtils.isAssignable(md.getFieldType(), Reader.class)) {
				ObjectUtils.setValue(t, md.getField(), this.lobHandler.getClobAsCharacterStream(rs, i));
			} else if (ClassUtils.isAssignable(md.getFieldType(), String.class)) {
				ObjectUtils.setValue(t, md.getField(), this.lobHandler.getClobAsString(rs, i));
			} else if (md.getFieldType().equals(byte[].class)) {
				ObjectUtils.setValue(t, md.getField(), this.lobHandler.getBlobAsBytes(rs, i));
			} else if (ClassUtils.isAssignable(md.getFieldType(), Serializable.class)) {
				InputStream tmpInput = this.lobHandler.getBlobAsBinaryStream(rs, i);
				Object foo = SerializationUtils.deserialize(tmpInput);
				ObjectUtils.setValue(t, md.getField(), foo);
			} else {
				Object foo = JSON.parseObject(this.lobHandler.getClobAsString(rs, i), md.getFieldType());
				ObjectUtils.setValue(t, md.getField(), foo);
			}
		} else {
			throw new UnsupportedOperationException("//TODO lazy load");
		}
	}

	public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
		Assert.state(this.mappedClass != null, "Mapped class was not specified");
		T mappedObject = BeanUtils.instantiate(this.mappedClass);
		BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(mappedObject);
		initBeanWrapper(bw);

		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		Set<String> populatedProperties = (isCheckFullyPopulated() ? new HashSet<String>() : null);

		for (int index = 1; index <= columnCount; index++) {
			String column = JdbcUtils.lookupColumnName(rsmd, index);

			// 处理大字段
			final EntityColumnMetadata columnMetadata = this.metadata.getColumnByField(column);
			if (columnMetadata.isLob()) {
				this.processLob(mappedObject, rs, index, columnMetadata);
				continue;
			}

			PropertyDescriptor pd = this.mappedFields.get(column.replaceAll(" ", "").toLowerCase());
			if (pd != null) {
				try {
					Object value = getColumnValue(rs, index, pd);
					if (logger.isDebugEnabled() && rowNumber == 0) {
						logger.debug("Mapping column '" + column + "' to property '" + pd.getName() + "' of type "
								+ pd.getPropertyType());
					}
					try {
						bw.setPropertyValue(pd.getName(), value);
					} catch (TypeMismatchException e) {
						if (value == null && primitivesDefaultedForNullValue) {
							logger.debug("Intercepted TypeMismatchException for row " + rowNumber + " and column '"
									+ column + "' with value " + value + " when setting property '" + pd.getName()
									+ "' of type " + pd.getPropertyType() + " on object: " + mappedObject);
						} else {
							throw e;
						}
					}
					if (populatedProperties != null) {
						populatedProperties.add(pd.getName());
					}
				} catch (NotWritablePropertyException ex) {
					throw new DataRetrievalFailureException(
							"Unable to map column " + column + " to property " + pd.getName(), ex);
				}
			}
		}

		if (populatedProperties != null && !populatedProperties.equals(this.mappedProperties)) {
			throw new InvalidDataAccessApiUsageException("Given ResultSet does not contain all fields "
					+ "necessary to populate object of class [" + this.mappedClass + "]: " + this.mappedProperties);
		}

		return mappedObject;
	}

	protected void initBeanWrapper(BeanWrapper bw) {
	}

	protected Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) throws SQLException {
		return JdbcUtils.getResultSetValue(rs, index, pd.getPropertyType());
	}

	public static <T> NRowMapper<T> newInstance(LobHandler lobHandler, Class<T> mappedClass) {
		return new NRowMapper<T>(lobHandler, mappedClass);
	}
}