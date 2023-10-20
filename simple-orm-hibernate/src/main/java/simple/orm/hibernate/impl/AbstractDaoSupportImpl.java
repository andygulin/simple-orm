package simple.orm.hibernate.impl;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import simple.orm.api.DaoSupport;
import simple.orm.api.Page;
import simple.orm.api.query.Order;
import simple.orm.api.query.QueryContext;
import simple.orm.api.query.QueryContextPiece;
import simple.orm.api.query.operator.Operator;
import simple.orm.exception.DaoException;
import simple.orm.utils.ObjectUtils;

import javax.persistence.Id;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Repository
@Transactional(rollbackFor = Exception.class)
public abstract class AbstractDaoSupportImpl<T> implements DaoSupport<T> {

    private static final Log log = LogFactory.getLog(AbstractDaoSupportImpl.class);

    @Autowired
    protected SessionFactory sessionFactory;
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @Autowired
    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private RowMapper<T> rowMapper;

    private Class<T> entityClazz;

    private String pkName;

    private Criteria buildCriteria(Map<String, Object> map, Order... orders) {
        Session session = this.getSession();
        return buildCriteria(map, session, orders);
    }

    private Criteria buildCriteria(Map<String, Object> map, Session session, Order... orders) {
        if (MapUtils.isEmpty(map)) {
            return null;
        }
        Criteria c = session.createCriteria(this.getEntityClass());
        for (Iterator<Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext(); ) {
            Entry<String, Object> entry = it.next();
            c.add(Restrictions.eq(entry.getKey(), entry.getValue()));
        }

        org.hibernate.criterion.Order[] hibernateOrders = this.buildOrder(orders);
        if (ArrayUtils.isNotEmpty(hibernateOrders)) {
            for (org.hibernate.criterion.Order foo : hibernateOrders) {
                c.addOrder(foo);
            }
        }
        return c;
    }

    private Criteria buildCriteria(QueryContext queryContext, Order... orders) throws DaoException {
        Session session = this.getSession();
        return this.buildCriteria(queryContext, session, orders);
    }

    private Criteria buildCriteria(QueryContext queryContext, Session session, Order... orders) throws DaoException {

        Criteria c = session.createCriteria(this.getEntityClass());

        if (queryContext == null) {
            return c;
        }

        for (Iterator<QueryContextPiece> it = queryContext.iterator(); it.hasNext(); ) {
            QueryContextPiece piece = it.next();
            Operator op = piece.getOperator();
            if (op == Operator.eq) {
                c.add(Restrictions.eq(piece.getField(), piece.getValues()[0]));
            } else if (op == Operator.notEQ) {
                c.add(Restrictions.ne(piece.getField(), piece.getValues()[0]));
            } else if (op == Operator.ge) {
                c.add(Restrictions.ge(piece.getField(), piece.getValues()[0]));
            } else if (op == Operator.gt) {
                c.add(Restrictions.gt(piece.getField(), piece.getValues()[0]));
            } else if (op == Operator.le) {
                c.add(Restrictions.le(piece.getField(), piece.getValues()[0]));
            } else if (op == Operator.lt) {
                c.add(Restrictions.lt(piece.getField(), piece.getValues()[0]));
            } else if (op == Operator.like) {
                String val = String.valueOf(piece.getValues()[0]);
                MatchMode mm = null;
                if (val.endsWith("%") && val.startsWith("%")) {
                    mm = MatchMode.ANYWHERE;
                    val = val.substring(1, val.length() - 1);
                } else if (val.endsWith("%")) {
                    mm = MatchMode.END;
                    val = val.substring(0, val.length() - 1);
                } else if (val.startsWith("%")) {
                    mm = MatchMode.START;
                    val = val.substring(1);
                } else {
                    mm = MatchMode.EXACT;
                }
                c.add(Restrictions.like(piece.getField(), val, mm));
            } else if (op == Operator.isNull) {
                c.add(Restrictions.isNull(piece.getField()));
            } else if (op == Operator.isNotNull) {
                c.add(Restrictions.isNotNull(piece.getField()));
            } else if (op == Operator.between) {
                c.add(Restrictions.between(piece.getField(), piece.getValues()[0], piece.getValues()[1]));
            } else if (op == Operator.notBetween) {
                SimpleExpression cl = Restrictions.lt(piece.getField(), piece.getValues()[0]);
                SimpleExpression cr = Restrictions.gt(piece.getField(), piece.getValues()[1]);
                c.add(Restrictions.or(cl, cr));
            } else if (op == Operator.in) {
                c.add(Restrictions.in(piece.getField(), piece.getValues()));
            } else if (op == Operator.notIn) {
                Criterion criterionIn = Restrictions.in(piece.getField(), piece.getValues());
                c.add(Restrictions.not(criterionIn));
            } else {
                throw new DaoException("unknow operator: " + op);
            }
        }

        org.hibernate.criterion.Order[] hibernateOrder = this.buildOrder(orders);
        if (ArrayUtils.isNotEmpty(hibernateOrder)) {
            for (org.hibernate.criterion.Order foo : hibernateOrder) {
                c.addOrder(foo);
            }
        }

        return c;
    }

    private Criteria buildCriteria(String field, Object value, Order... orders) {
        Session session = this.getSession();
        return this.buildCriteria(field, value, session, orders);
    }

    private Criteria buildCriteria(String field, Object value, Session session, Order... orders) {
        if (StringUtils.isBlank(field) || value == null) {
            throw new IllegalArgumentException("field or value is invalid.");
        }

        Criteria c = session.createCriteria(this.getEntityClass()).add(Restrictions.eq(field, value));
        org.hibernate.criterion.Order[] ho = this.buildOrder(orders);
        if (ArrayUtils.isNotEmpty(ho)) {
            for (org.hibernate.criterion.Order foo : ho) {
                c.addOrder(foo);
            }
        }
        return c;
    }

    private Criteria buildCriteria(String[] fields, Object[] values, Order... orders) {
        Session session = this.getSession();
        return this.buildCriteria(fields, values, session, orders);
    }

    private Criteria buildCriteria(String[] fields, Object[] values, Session session, Order... orders) {
        this.validateFieldsAndValues(fields, values);
        Criteria c = session.createCriteria(this.getEntityClass());
        for (int i = 0, len = fields.length; i < len; i++) {
            c.add(Restrictions.eq(fields[i], values[i]));
        }
        org.hibernate.criterion.Order[] ho = this.buildOrder(orders);

        if (ArrayUtils.isNotEmpty(ho)) {
            for (org.hibernate.criterion.Order foo : ho) {
                c.addOrder(foo);
            }
        }
        return c;
    }

    private org.hibernate.criterion.Order[] buildOrder(Order... orders) {
        if (ArrayUtils.isEmpty(orders)) {
            return null;
        }
        org.hibernate.criterion.Order[] result = new org.hibernate.criterion.Order[orders.length];

        for (int i = 0, len = orders.length; i < len; i++) {

            Order o = orders[i];
            result[i] = o.isAsc() ? org.hibernate.criterion.Order.asc(o.getField())
                    : org.hibernate.criterion.Order.desc(o.getField());
        }
        return result;
    }

    @Override
    public long count(Map<String, Object> map) throws DaoException {
        return (Long) this.buildCriteria(map).setProjection(Projections.rowCount()).uniqueResult();
    }

    @Override
    public long count(QueryContext context) throws DaoException {
        return (Long) this.buildCriteria(context).setProjection(Projections.rowCount()).uniqueResult();
    }

    @Override
    public long count(String field, Object value) throws DaoException {
        return (Long) this.buildCriteria(field, value).setProjection(Projections.rowCount()).uniqueResult();
    }

    @Override
    public long count(String[] fields, Object[] values) throws DaoException {
        return (Long) this.buildCriteria(fields, values).setProjection(Projections.rowCount()).uniqueResult();
    }

    @Override
    public long countAll() throws DaoException {
        return (Long) this.getSession().createCriteria(this.getEntityClass()).setProjection(Projections.rowCount())
                .uniqueResult();
    }

    private RowMapper<T> createRowMapper() {
        if (this.rowMapper == null) {
            this.rowMapper = BeanPropertyRowMapper.newInstance(this.getEntityClass());
        }
        return this.rowMapper;
    }

    @Override
    public void createTableIfNotExist() throws DaoException {
        throw new UnsupportedOperationException("method doesn't support.");
    }

    @Override
    @Transactional
    public void delete(Map<String, Object> map) throws DaoException {
        if (MapUtils.isEmpty(map)) {
            throw new IllegalArgumentException("delete by map failed: map is empty.");
        }
        Session session = this.getSession();
        Criteria c = session.createCriteria(this.getEntityClass());
        for (Iterator<Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext(); ) {
            Entry<String, Object> entry = it.next();
            c.add(Restrictions.eq(entry.getKey(), entry.getValue()));
        }
        List<?> li = c.list();
        for (Object foo : li) {
            session.delete(foo);
        }
    }

    @Override
    public void delete(QueryContext queryContext) throws DaoException {
        Session session = this.getSession();
        Criteria c = this.buildCriteria(queryContext, session);
        if (c == null) {
            return;
        }
        List<?> li = c.list();
        for (Object foo : li) {
            session.delete(foo);
        }
    }

    @Override
    @Transactional
    public void delete(Serializable pk) throws DaoException {
        Session session = this.getSession();
        Object pojo = session.load(this.getEntityClass(), pk);
        if (pojo != null) {
            session.delete(pojo);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional
    public void delete(String field, Object value) throws DaoException {
        Session session = this.getSession();
        final String hql = "from " + this.getEntityClass().getCanonicalName() + " foo where foo." + field + "=?";
        Query query = session.createQuery(hql);
        query.setParameter(0, value);
        List<T> li = query.list();
        for (T foo : li) {
            this.delete(foo);
        }
    }

    @Override
    @Transactional
    public void delete(String[] fields, Object[] values) throws DaoException {
        Session session = this.getSession();
        Criteria c = session.createCriteria(this.getEntityClass());
        for (int i = 0, len = fields.length; i < len; i++) {
            c.add(Restrictions.eq(fields[i], values[i]));
        }
        List<?> li = c.list();
        for (Object foo : li) {
            session.delete(foo);
        }
    }

    @Override
    public void delete(T t) throws DaoException {
        if (t == null) {
            return;
        }
        Session s = this.getSession();
        s.delete(t);
    }

    @Override
    @Transactional
    public void deleteAll() throws DaoException {
        Session session = this.getSession();
        Query q = session.createQuery("from " + this.getEntityClass().getCanonicalName());
        List<?> li = q.list();
        for (Object foo : li) {
            session.delete(foo);
        }
    }

    @Override
    public void executeSQL(String sql, Object... params) throws DaoException {
        if (StringUtils.strip(sql).toLowerCase().startsWith("select")) {
            return;
        }
        this.jdbcTemplate.update(sql, params);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getEntityClass() {
        if (entityClazz == null) {
            entityClazz = (Class<T>) this.getGenericType(0);
        }
        return this.entityClazz;
    }

    private Class<?> getGenericType(int index) {
        Type genType = this.getClass().getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            throw new RuntimeException("Index outof bounds");
        }
        if (!(params[index] instanceof Class)) {
            return Object.class;
        }
        return (Class<?>) params[index];
    }

    @Override
    public JdbcTemplate getJdbcTemplate() throws DaoException {
        return this.jdbcTemplate;
    }

    @Override
    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() throws DaoException {
        return this.namedParameterJdbcTemplate;
    }

    private Serializable getPrimaryValue(T t) {
        try {
            if (StringUtils.isNotBlank(this.pkName)) {
                Method md = this.getEntityClass().getMethod("get" + StringUtils.capitalize(pkName));
                if (md == null) {
                    md = this.getEntityClass().getMethod("is" + StringUtils.capitalize(pkName));
                }
                return (Serializable) md.invoke(t);
            }

            for (Method m : this.getEntityClass().getMethods()) {
                if (m.getAnnotation(Id.class) != null) {
                    String name = m.getName();
                    if (name.startsWith("is")) {
                        this.pkName = StringUtils.uncapitalize(name.substring(2));
                    } else if (name.startsWith("get")) {
                        this.pkName = StringUtils.uncapitalize(name.substring(3));
                    }
                    return (Serializable) m.invoke(t);
                }
            }
            return null;
        } catch (Throwable e) {
            throw new RuntimeException("get primary key value error: ", e);
        }

    }

    protected Session getSession() {
        return this.sessionFactory.getCurrentSession();
    }

    protected SessionFactory getSessionFactory() {
        return this.sessionFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T load(Serializable pkValue) throws DaoException {
        if (pkValue == null) {
            return null;
        }
        Session session = this.getSession();
        return (T) session.get(this.getEntityClass(), pkValue);
    }

    @Override
    public List<T> query(Map<String, Object> map, Order... order) throws DaoException {
        Criteria c = buildCriteria(map, order);
        return c.list();
    }

    @Override
    public List<T> query(QueryContext context, Order... order) throws DaoException {
        Criteria c = this.buildCriteria(context, order);
        return c.list();
    }

    @Override
    public List<T> query(String field, Object value, Order... order) throws DaoException {
        Criteria c = this.buildCriteria(field, value, order);
        return c.list();
    }

    @Override
    public List<T> query(String[] fields, Object[] values, Order... order) throws DaoException {
        Criteria c = this.buildCriteria(fields, values, order);
        return c.list();
    }

    @Override
    public List<T> queryAll(Order... order) throws DaoException {
        Session s = this.getSession();
        Criteria c = s.createCriteria(this.getEntityClass());
        org.hibernate.criterion.Order[] ho = this.buildOrder(order);
        for (org.hibernate.criterion.Order foo : ho) {
            c.addOrder(foo);
        }
        return c.list();
    }

    @Override
    public Number queryAvgValue(String field) throws DaoException {
        Session session = this.getSession();
        Criteria c = session.createCriteria(this.getEntityClass());
        c.setProjection(Projections.avg(field));
        return (Number) c.uniqueResult();
    }

    @Override
    public Number queryAvgValue(String field, QueryContext queryContext) throws DaoException {
        Criteria c = this.buildCriteria(queryContext);
        c.setProjection(Projections.avg(field));
        return (Number) c.uniqueResult();
    }

    @Override
    public Number queryAvgValue(String field, String whereField, Object whereValue) throws DaoException {
        Criteria c = this.buildCriteria(whereField, whereValue).setProjection(Projections.avg(field));
        return (Number) c.uniqueResult();
    }

    @Override
    public Number queryAvgValue(String field, String[] whereFields, Object[] whereValues) throws DaoException {
        Criteria c = this.buildCriteria(whereFields, whereValues).setProjection(Projections.avg(field));
        return (Number) c.uniqueResult();
    }

    @Override
    public List<T> queryBySQL(String sql, Object... params) throws DaoException {
        return this.jdbcTemplate.query(sql, params, this.createRowMapper());
    }

    @Override
    public T queryBySQLFirst(String sql, Object... params) throws DaoException {
        List<T> li = this.jdbcTemplate.query(sql, params, this.createRowMapper());
        return CollectionUtils.isNotEmpty(li) ? li.get(0) : null;
    }

    @Override
    public <E> E queryFieldFirst(String field, Class<E> fieldClass, Map<String, Object> map, Order... order)
            throws DaoException {
        return null;
    }

    @Override
    public <E> E queryFieldFirst(String field, Class<E> fieldClass, Order... order) throws DaoException {
        return null;
    }

    @Override
    public <E> E queryFieldFirst(String field, Class<E> fieldClass, QueryContext queryContext, Order... order)
            throws DaoException {
        return null;
    }

    @Override
    public <E> E queryFieldFirst(String field, Class<E> fieldClass, String whereField, Object whereValue,
                                 Order... order) throws DaoException {
        return null;
    }

    @Override
    public <E> E queryFieldFirst(String field, Class<E> fieldClass, String[] whereFields, Object[] whereValues,
                                 Order... order) throws DaoException {
        return null;
    }

    @Override
    public Object queryFieldFirst(String field, Map<String, Object> map, Order... order) throws DaoException {
        return null;
    }

    @Override
    public Object queryFieldFirst(String field, Order... order) throws DaoException {
        return null;
    }

    @Override
    public Object queryFieldFirst(String field, QueryContext queryContext, Order... order) throws DaoException {
        return null;
    }

    @Override
    public Object queryFieldFirst(String field, String whereField, Object whereValue, Order... order)
            throws DaoException {
        return null;
    }

    @Override
    public Object queryFieldFirst(String field, String[] whereFields, Object[] whereValues, Order... order)
            throws DaoException {
        return null;
    }

    @Override
    public <E> List<E> queryFieldValues(String field, Class<E> fieldClass, Map<String, Object> map, Order... order)
            throws DaoException {
        return null;
    }

    @Override
    public <E> List<E> queryFieldValues(String field, Class<E> fieldClass, QueryContext queryContext, Order... order)
            throws DaoException {
        return null;
    }

    @Override
    public <E> List<E> queryFieldValues(String field, Class<E> fieldClass, String whereField, Object whereValue,
                                        Order... order) throws DaoException {
        return null;
    }

    @Override
    public <E> List<E> queryFieldValues(String field, Class<E> fieldClass, String[] whereFields, Object[] whereValues,
                                        Order... order) throws DaoException {
        return null;
    }

    @Override
    public List<Object> queryFieldValues(String field, Map<String, Object> map, Order... order) throws DaoException {
        return null;
    }

    @Override
    public List<Object> queryFieldValues(String field, QueryContext queryContext, Order... order) throws DaoException {
        return null;
    }

    @Override
    public List<Object> queryFieldValues(String field, String whereField, Object whereValue, Order... order)
            throws DaoException {
        return null;
    }

    @Override
    public List<Object> queryFieldValues(String field, String[] whereFields, Object[] whereValues, Order... order)
            throws DaoException {
        return null;
    }

    @Override
    public T queryFirst(Map<String, Object> map, Order... order) throws DaoException {
        Criteria c = this.buildCriteria(map, order);
        return this.queryFirstByCriteria(c);
    }

    @Override
    public T queryFirst(QueryContext context, Order... order) throws DaoException {
        Criteria c = this.buildCriteria(context, order);
        return this.queryFirstByCriteria(c);
    }

    @Override
    public T queryFirst(String field, Object value, Order... order) throws DaoException {
        Criteria c = this.buildCriteria(field, value, order);
        return this.queryFirstByCriteria(c);
    }

    @Override
    public T queryFirst(String[] fields, Object[] values, Order... order) throws DaoException {
        Criteria c = this.buildCriteria(fields, values, order);
        return this.queryFirstByCriteria(c);
    }

    private T queryFirstByCriteria(Criteria c) {
        c.setMaxResults(1);
        c.setFirstResult(0);
        List li = c.list();
        return (T) (CollectionUtils.isEmpty(li) ? null : li.get(0));
    }

    @Override
    public List<T> queryLimit(int limitFrom, int limitSize, Order... order) throws DaoException {
        return null;
    }

    @Override
    public List<T> queryLimit(QueryContext context, int limitFrom, int limitSize, Order... order) throws DaoException {
        return null;
    }

    @Override
    public List<T> queryLimit(String field, Object value, int limitFrom, int limitSize, Order... order)
            throws DaoException {
        return null;
    }

    @Override
    public List<T> queryLimit(String[] fields, Object[] values, int limitFrom, int limitSize, Order... order)
            throws DaoException {
        return null;
    }

    @Override
    public Number queryMaxValue(String field) throws DaoException {
        return null;
    }

    @Override
    public Number queryMaxValue(String field, QueryContext queryContext) throws DaoException {
        return null;
    }

    @Override
    public Number queryMaxValue(String field, String whereField, Object whereValue) throws DaoException {
        return this.queryMaxValue(field, new String[]{whereField}, new Object[]{whereValue});
    }

    @Override
    public Number queryMaxValue(String field, String[] whereFields, Object[] whereValues) throws DaoException {
        this.validateFieldsAndValues(whereFields, whereValues);
        QueryContext context = new QueryContext();
        for (int i = 0, len = whereFields.length; i < len; i++) {
            context.andEquals(whereFields[i], whereValues[i]);
        }
        return this.queryMaxValue(field, context);
    }

    @Override
    public Number queryMinValue(String field) throws DaoException {
        return this.queryMinValue(field, null);
    }

    @Override
    public Number queryMinValue(String field, QueryContext queryContext) throws DaoException {
        Object obj = this.buildCriteria(queryContext).setProjection(Projections.min(field)).uniqueResult();
        return Double.valueOf(String.valueOf(obj));
    }

    @Override
    public Number queryMinValue(String field, String whereField, Object whereValue) throws DaoException {
        return this.queryMinValue(field, new String[]{whereField}, new Object[]{whereValue});
    }

    @Override
    public Number queryMinValue(String field, String[] whereFields, Object[] whereValues) throws DaoException {
        this.validateFieldsAndValues(whereFields, whereValues);
        QueryContext context = new QueryContext();
        for (int i = 0, len = whereFields.length; i < len; i++) {
            context.andEquals(whereFields[i], whereValues[i]);
        }
        return this.queryMinValue(field, context);
    }

    @Override
    public Number querySumValue(String field) throws DaoException {
        return (Number) this.getSession().createCriteria(this.getEntityClass()).setProjection(Projections.sum(field))
                .uniqueResult();
    }

    @Override
    public Number querySumValue(String field, String whereField, Object whereValue) throws DaoException {
        Criteria c = this.buildCriteria(whereField, whereValue).setProjection(Projections.sum(field));
        c.add(Restrictions.eq(whereField, whereValue));
        return (Number) c.uniqueResult();
    }

    @Override
    public Number querySumValue(String field, String[] whereFields, Object[] whereValues) throws DaoException {
        Criteria c = this.buildCriteria(whereFields, whereValues).setProjection(Projections.sum(field));
        return (Number) c.uniqueResult();
    }

    @Override
    public Page<T> queryWithPage(int pageNo, int pageSize, Order... order) throws DaoException {
        return this.queryWithPage(pageNo, pageSize, null, order);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Page<T> queryWithPage(int pageNo, int pageSize, QueryContext context, Order... order) throws DaoException {
        if (pageSize < 1) {
            throw new IllegalArgumentException("page size must be greater than zero.");
        }
        Criteria criteria = this.buildCriteria(context, order);
        long count = (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
        criteria.setProjection(null);

        int start = Math.max(0, (pageNo - 1) * pageSize - 1);
        int end = Math.max(1, pageNo) * pageSize - 1;
        criteria.setFirstResult(start);
        criteria.setMaxResults(end);

        List<T> li = criteria.list();
        return Page.build(li, pageNo, pageSize, count);
    }

    public Page<T> queryWithPage(int pageNo, int pageSize, String field, Object value, Order... order)
            throws DaoException {
        QueryContext q = new QueryContext().andEquals(field, value);
        return this.queryWithPage(pageNo, pageSize, q, order);
    }

    @Override
    public Page<T> queryWithPage(int pageNo, int pageSize, String[] fields, Object[] values, Order... order)
            throws DaoException {
        this.validateFieldsAndValues(fields, values);
        QueryContext q = new QueryContext();
        for (int i = 0, len = fields.length; i < len; i++) {
            q.andEquals(fields[i], values[i]);
        }
        return this.queryWithPage(pageNo, pageSize, q, order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(T... t) throws DaoException {
        Session session = this.getSession();
        for (T foo : t) {
            session.save(foo);
        }
    }

    @Override
    public void saveOrUpdate(Collection<T> t) throws DaoException {
        Session session = getSession();
        for (T foo : t) {
            session.saveOrUpdate(foo);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(T... t) throws DaoException {
        Session session = this.getSession();
        for (T foo : t) {
            Serializable pkv = this.getPrimaryValue(foo);
            T old = this.load(pkv);
            if (old == null || ObjectUtils.isZero(old)) {
                Serializable newPkv = session.save(foo);
                try {
                    PropertyUtils.setProperty(foo, this.pkName, newPkv);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            } else {
                this.update(foo);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(T... t) throws DaoException {
        Session session = this.getSession();
        for (T foo : t) {
            T old = this.load(this.getPrimaryValue(foo));
            BeanUtils.copyProperties(foo, old, this.pkName);
            session.update(old);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateField(Serializable pk, String field, Object value) throws DaoException {
        T pojo = this.load(pk);
        if (pojo == null) {
            throw new DaoException("entity[pk=" + pk + "] doesn't exist.");
        }
        try {
            PropertyUtils.setProperty(pojo, field, value);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        this.update(pojo);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateField(Serializable pk, String[] fields, Object[] values) throws DaoException {
        this.validateFieldsAndValues(fields, values);

        T pojo = this.load(pk);
        if (pojo == null) {
            throw new DaoException("entity[pk=" + pk + "] doesn't exist.");
        }

        for (int i = 0, len = fields.length; i < len; i++) {
            try {
                PropertyUtils.setProperty(pojo, fields[i], values[i]);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
        this.update(pojo);
    }

    private void validateFieldsAndValues(String[] fields, Object[] values) {

        if (fields == null || values == null) {
            throw new IllegalArgumentException("fields and values cannot be null.");
        }

        if (fields.length != values.length) {
            throw new IllegalArgumentException("fields.length must be equal with values.length.");
        }

    }

}
