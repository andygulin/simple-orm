package simple.orm.api;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import simple.orm.api.query.Order;
import simple.orm.api.query.QueryContext;
import simple.orm.exception.DaoException;

public interface DaoSupport<T> {

	JdbcTemplate getJdbcTemplate() throws DaoException;

	NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() throws DaoException;

	long count(Map<String, Object> map) throws DaoException;

	long count(QueryContext context) throws DaoException;

	long count(String field, Object value) throws DaoException;

	long count(String[] fields, Object[] values) throws DaoException;

	long countAll() throws DaoException;

	void createTableIfNotExist() throws DaoException;

	void delete(Serializable primaryfield) throws DaoException;

	void delete(String field, Object value) throws DaoException;

	void delete(String[] fields, Object[] values) throws DaoException;

	void delete(Map<String, Object> map) throws DaoException;

	void delete(QueryContext queryContext) throws DaoException;

	void delete(T t) throws DaoException;

	void deleteAll() throws DaoException;

	void executeSQL(String sql, Object... params) throws DaoException;

	Class<T> getEntityClass();

	T load(Serializable pkValue) throws DaoException;

	List<T> query(Map<String, Object> map, Order... order) throws DaoException;

	List<T> query(QueryContext context, Order... order) throws DaoException;

	List<T> query(String field, Object value, Order... order) throws DaoException;

	List<T> query(String[] fields, Object[] values, Order... order) throws DaoException;

	List<T> queryAll(Order... order) throws DaoException;

	Number queryAvgValue(String field) throws DaoException;

	Number queryAvgValue(String field, QueryContext queryContext) throws DaoException;

	Number queryAvgValue(String field, String whereField, Object whereValue) throws DaoException;

	Number queryAvgValue(String field, String[] whereFields, Object[] whereValues) throws DaoException;

	List<T> queryBySQL(String sql, Object... params) throws DaoException;

	T queryBySQLFirst(String sql, Object... params) throws DaoException;

	<E> E queryFieldFirst(String field, Class<E> fieldClass, Map<String, Object> map, Order... order)
			throws DaoException;

	<E> E queryFieldFirst(String field, Class<E> fieldClass, Order... order) throws DaoException;

	<E> E queryFieldFirst(String field, Class<E> fieldClass, QueryContext queryContext, Order... order)
			throws DaoException;

	<E> E queryFieldFirst(String field, Class<E> fieldClass, String whereField, Object whereValue, Order... order)
			throws DaoException;

	<E> E queryFieldFirst(String field, Class<E> fieldClass, String[] whereFields, Object[] whereValues, Order... order)
			throws DaoException;

	Object queryFieldFirst(String field, Map<String, Object> map, Order... order) throws DaoException;

	Object queryFieldFirst(String field, Order... order) throws DaoException;

	Object queryFieldFirst(String field, QueryContext queryContext, Order... order) throws DaoException;

	Object queryFieldFirst(String field, String whereField, Object whereValue, Order... order) throws DaoException;

	Object queryFieldFirst(String field, String[] whereFields, Object[] whereValues, Order... order)
			throws DaoException;

	<E> List<E> queryFieldValues(String field, Class<E> fieldClass, Map<String, Object> map, Order... order)
			throws DaoException;

	<E> List<E> queryFieldValues(String field, Class<E> fieldClass, QueryContext queryContext, Order... order)
			throws DaoException;

	<E> List<E> queryFieldValues(String field, Class<E> fieldClass, String whereField, Object whereValue,
			Order... order) throws DaoException;

	<E> List<E> queryFieldValues(String field, Class<E> fieldClass, String[] whereFields, Object[] whereValues,
			Order... order) throws DaoException;

	List<Object> queryFieldValues(String field, Map<String, Object> map, Order... order) throws DaoException;

	List<Object> queryFieldValues(String field, QueryContext queryContext, Order... order) throws DaoException;

	List<Object> queryFieldValues(String field, String whereField, Object whereValue, Order... order)
			throws DaoException;

	List<Object> queryFieldValues(String field, String[] whereFields, Object[] whereValues, Order... order)
			throws DaoException;

	T queryFirst(Map<String, Object> map, Order... order) throws DaoException;

	T queryFirst(QueryContext context, Order... order) throws DaoException;

	T queryFirst(String field, Object value, Order... order) throws DaoException;

	T queryFirst(String[] fields, Object[] values, Order... order) throws DaoException;

	List<T> queryLimit(int limitFrom, int limitSize, Order... order) throws DaoException;

	List<T> queryLimit(QueryContext context, int limitFrom, int limitSize, Order... order) throws DaoException;

	List<T> queryLimit(String field, Object value, int limitFrom, int limitSize, Order... order) throws DaoException;

	List<T> queryLimit(String[] fields, Object[] values, int limitFrom, int limitSize, Order... order)
			throws DaoException;

	Number queryMaxValue(String field) throws DaoException;

	Number queryMaxValue(String field, QueryContext queryContext) throws DaoException;

	Number queryMaxValue(String field, String whereField, Object whereValue) throws DaoException;

	Number queryMaxValue(String field, String[] whereFields, Object[] whereValues) throws DaoException;

	Number queryMinValue(String field) throws DaoException;

	Number queryMinValue(String field, QueryContext queryContext) throws DaoException;

	Number queryMinValue(String field, String whereField, Object whereValue) throws DaoException;

	Number queryMinValue(String field, String[] whereFields, Object[] whereValues) throws DaoException;

	Number querySumValue(String field) throws DaoException;

	Number querySumValue(String field, String whereField, Object whereValue) throws DaoException;

	Number querySumValue(String field, String[] whereFields, Object[] whereValues) throws DaoException;

	Page<T> queryWithPage(int pageNo, int pageSize, Order... order) throws DaoException;

	Page<T> queryWithPage(int pageNo, int pageSize, QueryContext context, Order... order) throws DaoException;

	Page<T> queryWithPage(int pageNo, int pageSize, String field, Object value, Order... order) throws DaoException;

	Page<T> queryWithPage(int pageNo, int pageSize, String[] fields, Object[] values, Order... order)
			throws DaoException;

	void save(T... t) throws DaoException;

	void saveOrUpdate(Collection<T> t) throws DaoException;

	void saveOrUpdate(T... t) throws DaoException;

	void update(T... t) throws DaoException;

	void updateField(Serializable pk, String field, Object value) throws DaoException;

	void updateField(Serializable pk, String[] fields, Object[] values) throws DaoException;

}
