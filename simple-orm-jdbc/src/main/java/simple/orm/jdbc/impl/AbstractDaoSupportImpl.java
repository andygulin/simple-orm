package simple.orm.jdbc.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import simple.orm.api.DaoSupport;
import simple.orm.api.Page;
import simple.orm.api.query.Order;
import simple.orm.api.query.QueryContext;
import simple.orm.api.query.QueryContextPiece;
import simple.orm.api.query.operator.*;
import simple.orm.exception.DaoException;
import simple.orm.jdbc.ext.InsertCreator;
import simple.orm.jdbc.ext.NRowMapper;
import simple.orm.meta.EntityColumnMetadata;
import simple.orm.meta.EntityMetadata;
import simple.orm.utils.ObjectUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;

@Repository
public abstract class AbstractDaoSupportImpl<T> implements DaoSupport<T> {

    /**
     * Constant <code>CONST_EQ="="</code>
     */
    protected static final String CONST_EQ = "=";
    /**
     * Constant <code>CONST_AND=" and "</code>
     */
    protected static final String CONST_AND = " and ";
    /**
     * Constant <code>CONST_OR=" or "</code>
     */
    protected static final String CONST_OR = " or ";
    /**
     * Constant <code>CONST_WHERE=" where "</code>
     */
    protected static final String CONST_WHERE = " where ";
    /**
     * Constant <code>CONST_FROM=" from "</code>
     */
    protected static final String CONST_FROM = " from ";
    /**
     * Constant <code>CONST_SELECT="select "</code>
     */
    protected static final String CONST_SELECT = "select ";
    /**
     * Constant <code>CONST_UPDATE="update "</code>
     */
    protected static final String CONST_UPDATE = "update ";
    /**
     * Constant <code>CONST_COUNT=" count(*) "</code>
     */
    protected static final String CONST_COUNT = " count(*) ";
    /**
     * Constant <code>CONST_ONE=" 1=1 "</code>
     */
    protected static final String CONST_ONE = " 1=1 ";
    /**
     * Constant <code>CONST_ORDER_BY=" order by "</code>
     */
    protected static final String CONST_ORDER_BY = " order by ";
    /**
     * Constant <code>CONST_ORDER_BY_ASC=" asc "</code>
     */
    protected static final String CONST_ORDER_BY_ASC = " asc ";
    /**
     * Constant <code>CONST_ORDER_BY_DESC=" desc "</code>
     */
    protected static final String CONST_ORDER_BY_DESC = " desc ";
    /**
     * Constant <code>CONST_SPACE=" "</code>
     */
    protected static final String CONST_SPACE = " ";
    /**
     * Constant <code>CONST_DELETE="delete "</code>
     */
    protected static final String CONST_DELETE = "delete ";
    private static final Log log = LogFactory.getLog(AbstractDaoSupportImpl.class);
    protected EntityMetadata<T> metadata;

    protected String selectHead;

    protected String updateHead;

    protected String updateHeadNoPK;

    protected String allFields;

    protected String allColumns;

    protected String allFieldsNoPK;

    protected String allColumnsNoPK;

    @Autowired
    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected DefaultLobHandler defaultLobHandler;

    private RowMapper<T> rowMapper;

    public AbstractDaoSupportImpl() {
        super();
        this.metadata = new EntityMetadata<>(this.getEntityClass());
        StringBuffer sb;
        List<String> tmpArr = new ArrayList<>();
        for (EntityColumnMetadata foo : this.metadata.getAllColumnMetadatas()) {
            sb = new StringBuffer();
            tmpArr.add(sb.append("`").append(foo.getName()).append("` as `").append(foo.getField()).append("`")
                    .toString());
        }
        this.selectHead = StringUtils.join(tmpArr, ",");

        List<String> liWithPK = new ArrayList<>();
        List<String> liWithOutPK = new ArrayList<>();
        for (EntityColumnMetadata col : this.metadata.getAllColumnMetadatas()) {
            sb = new StringBuffer();
            sb.append("`").append(col.getName()).append("`=:").append(col.getField()).toString();
            liWithPK.add(sb.toString());
            if (!col.isPrimaryKey()) {
                liWithOutPK.add(sb.toString());
            }
        }
        this.updateHead = StringUtils.join(liWithPK, ",");
        this.updateHeadNoPK = StringUtils.join(liWithOutPK, ",");

        this.allFields = StringUtils.join(this.metadata.getAllFields(), ",");
        this.allColumns = "`" + StringUtils.join(this.metadata.getAllColumnNames(), "`,`") + "`";
        this.allColumnsNoPK = "`" + StringUtils.join(this.metadata.getColumnNamesNoPrimaryKey(), "`,`") + "`";
        this.allFieldsNoPK = StringUtils.join(this.metadata.getFieldsNoPrimaryKey(), ",");

        if (log.isDebugEnabled()) {
            log.debug(MessageFormat.format("+++++ Initialize DaoSupport for class[{0}] SUCCESS! +++++",
                    this.getEntityClass().getCanonicalName()));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public long count(Map<String, Object> map) throws DaoException {
        StringBuffer sb = new StringBuffer();
        sb.append(CONST_SELECT).append(CONST_COUNT).append(CONST_FROM).append(this.metadata.getQualifiedTableName())
                .append(CONST_WHERE).append(CONST_ONE);

        if (MapUtils.isNotEmpty(map)) {
            for (Iterator<?> it = map.entrySet().iterator(); it.hasNext(); ) {
                Entry<String, ?> entry = (Entry<String, ?>) it.next();
                if (StringUtils.isBlank(entry.getKey())) {
                    continue;
                }
                sb.append(CONST_AND).append(this.metadata.getColumnByField(entry.getKey()).getNameWithQuote())
                        .append(CONST_EQ).append(":").append(entry.getKey());
            }
        }
        final String sql = sb.toString();
        try {
            return this.namedParameterJdbcTemplate.queryForObject(sql, map, long.class);
        } catch (Throwable e) {
            throw new DaoException(e);
        }
    }

    @Override
    public long count(QueryContext context) throws DaoException {
        StringBuffer sb = new StringBuffer();
        sb.append(CONST_SELECT).append("count(*)").append(CONST_FROM).append(this.metadata.getQualifiedTableName())
                .append(CONST_WHERE).append(CONST_ONE);
        List<Object> params = new ArrayList<>();
        this.parseQueryContext(sb, context, params);

        final String sql = sb.toString();
        try {
            return this.jdbcTemplate.queryForObject(sql, params.toArray(new Object[params.size()]), long.class);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }

    @Override
    public long count(String field, Object value) throws DaoException {
        final Map<String, Object> map = new HashMap<>(1);
        map.put(field, value);
        return this.count(map);
    }

    @Override
    public long count(String[] field, Object[] values) throws DaoException {
        final boolean isArrNotEmpty = ArrayUtils.isNotEmpty(field) && ArrayUtils.isNotEmpty(values);
        Map<String, Object> map = null;
        if (isArrNotEmpty) {
            if (field.length != values.length) {
                throw new DaoException("length of field array[" + field.length
                        + "] doesn't equal with length of value array[" + values.length + "].");
            } else {
                map = new HashMap<>();
                for (int i = 0; i < field.length; i++) {
                    map.put(field[i], values[i]);
                }
            }
        }
        return this.count(map);
    }

    @Override
    public long countAll() throws DaoException {
        StringBuffer sb = new StringBuffer();
        sb.append(CONST_SELECT).append("count(*)").append(CONST_FROM).append(this.metadata.getQualifiedTableName());
        try {
            return this.jdbcTemplate.queryForObject(sb.toString(), long.class);
        } catch (Throwable e) {
            throw new DaoException(e);
        }
    }

    protected RowMapper<T> createRowMapper() {
        if (this.rowMapper == null) {
            this.rowMapper = new NRowMapper<>(this.defaultLobHandler, this.getEntityClass());
        }
        return this.rowMapper;
    }

    private SqlParameterSource createSqlParameterSource(T t) {
        return new BeanPropertySqlParameterSource(t);
    }

    @Override
    public void createTableIfNotExist() throws DaoException {

        StringBuffer sb = new StringBuffer();
        sb.append("create table if not exists ").append(this.getMetadata().getQualifiedTableName()).append("(");

        EntityColumnMetadata[] tmpColArr = this.metadata.getAllColumnMetadatas()
                .toArray(new EntityColumnMetadata[this.metadata.getAllColumnMetadatas().size()]);
        for (int i = 0, len = tmpColArr.length; i < len; i++) {
            EntityColumnMetadata foo = tmpColArr[i];
            sb.append(foo.getNameWithQuote()).append(CONST_SPACE);
            if (foo.isLob()) {
                if (foo.getFieldType().equals(String.class)) {
                    sb.append("text");
                } else {
                    sb.append("blob");
                }
            } else if (foo.getFieldType().equals(String.class)) {
                sb.append("varchar(").append(foo.getLength() < 1 ? 64 : foo.getLength()).append(")");
            } else if (foo.getFieldType().equals(int.class) || foo.getFieldType().equals(Integer.class)) {
                sb.append("int(").append(foo.getLength() < 1 ? 11 : foo.getLength()).append(")");
            } else if (foo.getFieldType().equals(Long.class) || foo.getFieldType().equals(long.class)) {
                sb.append("int(").append(foo.getLength() < 1 ? 19 : foo.getLength()).append(")");
            } else if (foo.getFieldType().equals(Double.class) || foo.getFieldType().equals(double.class)
                    || foo.getFieldType().equals(Float.class) || foo.getFieldType().equals(float.class)) {
                sb.append("double(").append(foo.getLength() < 1 ? 19 : foo.getLength()).append(",")
                        .append(foo.getScale() < 1 ? 2 : foo.getScale()).append(")");
            } else if (foo.getFieldType().equals(java.util.Date.class)
                    || foo.getFieldType().equals(java.sql.Date.class)) {
                sb.append("datetime");
            } else if (foo.getFieldType().equals(java.sql.Timestamp.class)) {
                sb.append("timestamp default current_timestamp on update current_timestamp");
            } else {
                throw new UnsupportedOperationException("unsupported field type:" + foo.getFieldType());
            }

            if (foo.isPrimaryKey()) {
                sb.append(" primary key ");
            }
            if (foo.isAuto()) {
                sb.append(" auto_increment ");
            }

            if (foo.isRequire()) {
                sb.append(" not null ");
            }

            if (foo.isUnique()) {
                sb.append(" unique ");
            }

            sb.append(i == len - 1 ? ") ENGINE=InnoDB DEFAULT CHARSET=utf8" : ",");
        }
        System.out.println(sb);
        this.jdbcTemplate.execute(sb.toString());
    }

    @Override
    public void delete(Serializable primaryfield) throws DaoException {
        if (primaryfield == null) {
            throw new DaoException("cannot delete entity: primary value is null.");
        }
        final String sql = CONST_DELETE + CONST_FROM + this.metadata.getQualifiedTableName() + CONST_WHERE +
                this.metadata.getPrimaryKey().getName() + CONST_EQ + "?";
        try {
            this.jdbcTemplate.update(sql, primaryfield);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }

    @Override
    public void delete(String field, Object value) throws DaoException {
        this.delete(new String[]{field}, new Object[]{value});
    }

    @Override
    public void delete(String[] fields, Object[] values) throws DaoException {
        if (ArrayUtils.isEmpty(fields) || ArrayUtils.isEmpty(values) || fields.length != values.length) {
            throw new DaoException("fiels and values is empty or length doesn't match.");
        }

        StringBuffer sb = new StringBuffer(CONST_DELETE);
        sb.append(CONST_FROM).append(this.metadata.getQualifiedTableName()).append(CONST_WHERE).append(CONST_ONE);

        for (int i = 0, len = fields.length; i < len; i++) {
            sb.append(CONST_AND).append(this.metadata.getColumnByField(fields[i]).getNameWithQuote()).append(CONST_EQ)
                    .append("?");
        }

        final String sql = sb.toString();
        try {
            this.jdbcTemplate.update(sql, values);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }

    @Override
    public void delete(T t) throws DaoException {
        try {
            Serializable value = this.getPrimaryValue(t);
            this.delete(value);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }

    @Override
    public void deleteAll() throws DaoException {
        String sb = CONST_DELETE + CONST_FROM +
                this.metadata.getQualifiedTableName();
        this.jdbcTemplate.update(sb);
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
        if (this.metadata == null) {
            return (Class<T>) this.getGenericType(0);
        }
        return this.metadata.getClazz();
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

    public JdbcTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }

    EntityMetadata<T> getMetadata() {
        return this.metadata;
    }

    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return this.namedParameterJdbcTemplate;
    }

    private Serializable getPrimaryValue(T t) throws DaoException {
        final EntityColumnMetadata pkMeta = this.metadata.getPrimaryKey();
        final String pk = StringUtils.capitalize(pkMeta.getField());
        Method md;
        try {
            String methodName = (pkMeta.getFieldType().equals(boolean.class) ? "is" : "get") + pk;
            md = t.getClass().getMethod(methodName);
            Serializable value = (Serializable) md.invoke(t);
            return value;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new DaoException("get primary value for " + t + " failed:", e);
        }

    }

    private Number innerFunctionQuery(String field, String function) throws DaoException {
        return innerFunctionQuery(field, function, null);
    }

    private Number innerFunctionQuery(String field, String function, QueryContext q) throws DaoException {
        StringBuffer sb = new StringBuffer();
        sb.append(CONST_SELECT).append(function).append("(")
                .append(this.metadata.getColumnByField(field).getNameWithQuote()).append(") ").append(CONST_FROM)
                .append(this.metadata.getQualifiedTableName()).append(CONST_WHERE).append(CONST_ONE);
        List<Object> params = new ArrayList<>();
        this.parseQueryContext(sb, q, params);
        final String sql = sb.toString();
        return this.jdbcTemplate.queryForObject(sql, Number.class, params.toArray(new Object[params.size()]));
    }

    private Number innerFunctionQuery(String field, String function, String whereField, Object whereValue)
            throws DaoException {
        if (whereField == null || whereValue == null) {
            throw new IllegalArgumentException("query arg is null.");
        }
        return this.innerFunctionQuery(field, function, new String[]{whereField}, new Object[]{whereValue});
    }

    private Number innerFunctionQuery(String field, String function, String[] whereFields, Object[] whereValues)
            throws DaoException {
        if (whereFields == null || whereValues == null || whereFields.length != whereValues.length) {
            throw new IllegalArgumentException("query array is null or length doesn't match.");
        }
        QueryContext q = new QueryContext();
        for (int i = 0, len = whereFields.length; i < len; i++) {
            q.andEquals(whereFields[i], whereValues[i]);
        }
        return this.innerFunctionQuery(field, function, q);
    }

    private void innerInsert(T t) throws DaoException {
        PreparedStatementCreator psc = new InsertCreator<T>(this.defaultLobHandler, t, this.metadata);
        if (!this.metadata.getPrimaryKey().isAuto()) {
            this.jdbcTemplate.update(psc);
            return;
        }

        KeyHolder generatedKeyHolder = new GeneratedKeyHolder();
        this.jdbcTemplate.update(psc, generatedKeyHolder);
        Class<?> tmpClazz = this.metadata.getPrimaryKey().getFieldType();
        Number genKey = generatedKeyHolder.getKey();
        Method setMd = null;
        try {
            setMd = this.metadata.getClazz()
                    .getMethod("set" + StringUtils.capitalize(this.metadata.getPrimaryKey().getField()), tmpClazz);
            if (tmpClazz.equals(Long.class) || tmpClazz.equals(long.class)) {
                setMd.invoke(t, genKey.longValue());
            } else if (tmpClazz.equals(Integer.class) || tmpClazz.equals(int.class)) {
                setMd.invoke(t, genKey.intValue());
            } else if (tmpClazz.equals(Short.class) || tmpClazz.equals(short.class)) {
                setMd.invoke(t, genKey.shortValue());
            } else if (tmpClazz.equals(Float.class) || tmpClazz.equals(float.class)) {
                setMd.invoke(t, genKey.floatValue());
            } else if (tmpClazz.equals(Double.class) || tmpClazz.equals(double.class)) {
                setMd.invoke(t, genKey.doubleValue());
            } else {
                throw new UnsupportedOperationException("unsupported key holder type[" + tmpClazz + "].");
            }

        } catch (Throwable e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }

    private void innerUpdate(T t) throws DaoException {
        final String sql = "update " + this.metadata.getQualifiedTableName() + " set " + this.updateHeadNoPK +
                CONST_WHERE + this.metadata.getPrimaryKey().getName() + "=:" +
                this.metadata.getPrimaryKey().getField();
        try {
            this.namedParameterJdbcTemplate.update(sql, createSqlParameterSource(t));
        } catch (Throwable e) {
            throw new DaoException(e);
        }
    }

    @Override
    public T load(Serializable primaryfield) throws DaoException {
        final String sql = CONST_SELECT + this.selectHead + CONST_FROM +
                this.metadata.getQualifiedTableName() +
                CONST_WHERE + this.metadata.getPrimaryKey().getName() + CONST_EQ + "? limit 0,1";
        List<T> li = this.jdbcTemplate.query(sql, new Object[]{primaryfield}, createRowMapper());
        if (CollectionUtils.isNotEmpty(li)) {
            return li.get(0);
        }
        return null;
    }

    private void parseOrder(final StringBuffer sb, final Order... order) {
        if (ArrayUtils.isNotEmpty(order)) {
            sb.append(CONST_ORDER_BY);
            for (int i = 0, len = order.length; i < len; i++) {
                sb.append(this.metadata.getColumnByField(order[i].getField()).getNameWithQuote()).append(CONST_SPACE)
                        .append(order[i].isAsc() ? CONST_ORDER_BY_ASC : CONST_ORDER_BY_DESC);
                if (i != len - 1) {
                    sb.append(",");
                }
            }
        }
    }

    void parseQueryContext(final StringBuffer sb, final QueryContext context, final List<Object> params)
            throws DaoException {
        parseQueryContext(sb, context, params, null);
    }

    void parseQueryContext(final StringBuffer sb, final QueryContext context, final List<Object> params,
                           String tableAlias) throws DaoException {
        if (context == null) {
            return;
        }

        boolean isFirst = true;

        for (Iterator<QueryContextPiece> it = context.iterator(); it.hasNext(); ) {
            final QueryContextPiece piece = it.next();

            for (Object tmpV : piece.getValues()) {
                if (tmpV.getClass().isArray()) {
                    throw new DaoException(
                            "illegal args: if you're using int[],long[] and other arrays,replaced them use Integer[],Long[]...");
                } else if (tmpV instanceof Collection) {
                    throw new DaoException("illegal args. Collection<E> cannot be used as query args!");
                }
            }

            boolean isIn = piece.getOperator().equals(IN.singleton) || piece.getOperator().equals(NOT_IN.singleton);
            boolean isBetween = piece.getOperator().equals(BETWEEN.singleton)
                    || piece.getOperator().equals(NOT_BETWEEN.singleton);

            sb.append(CONST_SPACE).append(isFirst ? CONST_AND : piece.getAppendType().name()).append(CONST_SPACE);
            if (isIn && ArrayUtils.isEmpty(piece.getValues())) {
                sb.append(" 1=2 ");
                continue;
            }

            if (StringUtils.isNotBlank(tableAlias)) {
                sb.append(tableAlias).append(".");
            }

            sb.append("`").append(this.metadata.getColumnByField(piece.getField()).getName()).append("` ")
                    .append(piece.getOperator().value()).append(CONST_SPACE);
            isFirst = false;

            if (isIn) {
                sb.append(" (");
            }

            if (!isBetween) {
                // if operator is (NOT_)NULL skip scan params
                if (!piece.getOperator().equals(NOT_NULL.singleton) && !piece.getOperator().equals(NULL.singleton)) {
                    if (piece.getValues().length < 1) {
                        throw new RuntimeException("where case error:'in' list cannot be empty!");
                    }
                    for (int i = 0, len = piece.getValues().length; i < len; i++) {
                        sb.append("?");
                        params.add(piece.getValues()[i]);
                        sb.append((i != len - 1) ? "," : " ");
                    }
                }

            } else if (piece.getValues().length == 2) {
                // process between
                sb.append(" ? and ? ");
                params.add(piece.getValues()[0]);
                params.add(piece.getValues()[1]);
            } else {
                throw new DaoException("operator between parameter size illegal.");
            }

            if (isIn) {
                sb.append(") ");
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> query(Map<String, Object> map, Order... order) throws DaoException {
        StringBuffer sb = new StringBuffer();
        sb.append(CONST_SELECT).append(this.selectHead).append(CONST_FROM).append(this.metadata.getQualifiedTableName())
                .append(CONST_WHERE).append(CONST_ONE);
        if (MapUtils.isNotEmpty(map)) {
            EntityColumnMetadata foo;
            for (Iterator<Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext(); ) {
                Entry<String, Object> entry = it.next();
                foo = this.metadata.getColumnByField(entry.getKey());
                sb.append(CONST_AND).append(foo.getName()).append("=:").append(foo.getField());
            }
        }

        this.parseOrder(sb, order);

        final String sql = sb.toString();
        try {
            List<T> li = this.namedParameterJdbcTemplate.query(sql, MapUtils.isNotEmpty(map) ? map : MapUtils.EMPTY_MAP,
                    createRowMapper());
            return li;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }

    private List<T> query(QueryContext context, int limitStart, int limitSize, Order... order) throws DaoException {
        StringBuffer sb = new StringBuffer();
        sb.append(CONST_SELECT).append(this.selectHead).append(CONST_FROM).append(this.metadata.getQualifiedTableName())
                .append(CONST_WHERE).append(CONST_ONE);

        final List<Object> params = new ArrayList<>();
        this.parseQueryContext(sb, context, params);
        this.parseOrder(sb, order);

        if (limitSize > 0 && limitStart > -1) {
            sb.append(" limit ").append(limitStart).append(",").append(limitSize);
        }

        final String sql = sb.toString();
        try {
            return this.jdbcTemplate.query(sql, params.toArray(new Object[params.size()]), createRowMapper());
        } catch (Throwable e) {
            e.printStackTrace();
            throw new DaoException();
        }
    }

    @Override
    public List<T> query(QueryContext context, Order... order) throws DaoException {
        return this.query(context, -1, -1, order);
    }

    @Override
    public List<T> query(String field, Object value, Order... order) throws DaoException {
        QueryContext ctx = new QueryContext();
        ctx.and(field, Operator.eq, value);
        return this.query(ctx, order);
    }

    @Override
    public List<T> query(String[] fields, Object[] values, Order... order) throws DaoException {
        if (fields == null || values == null) {
            throw new DaoException("query fields and values array cannot be null.");
        }

        if (fields.length != values.length) {
            throw new DaoException("fields.length != values.length");
        }

        QueryContext ctx = new QueryContext();
        for (int i = 0, len = fields.length; i < len; i++) {
            ctx.and(fields[i], Operator.eq, values[i]);
        }
        return this.query(ctx, order);
    }

    @Override
    public List<T> queryAll(Order... order) throws DaoException {
        StringBuffer sb = new StringBuffer();
        sb.append(CONST_SELECT).append(this.selectHead).append(CONST_FROM)
                .append(this.metadata.getQualifiedTableName());
        this.parseOrder(sb, order);
        final String sql = sb.toString();
        try {
            return this.jdbcTemplate.query(sql, createRowMapper());
        } catch (Throwable e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }

    @Override
    public Number queryAvgValue(String field) throws DaoException {
        return this.innerFunctionQuery(field, "avg", null);
    }

    @Override
    public Number queryAvgValue(String field, QueryContext queryContext) throws DaoException {
        return this.innerFunctionQuery(field, "avg", queryContext);
    }

    @Override
    public Number queryAvgValue(String field, String whereField, Object whereValue) throws DaoException {
        return this.innerFunctionQuery(field, "avg", whereField, whereValue);
    }

    @Override
    public Number queryAvgValue(String field, String[] whereFields, Object[] whereValues) throws DaoException {
        return this.innerFunctionQuery(field, "avg", whereFields, whereValues);
    }

    @Override
    public List<T> queryBySQL(String sql, Object... params) throws DaoException {
        try {
            return this.jdbcTemplate.query(sql, params, createRowMapper());
        } catch (Throwable e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }

    @Override
    public T queryBySQLFirst(String sql, Object... params) throws DaoException {
        List<T> li = this.queryBySQL(sql + " limit 0,1", params);
        return CollectionUtils.isNotEmpty(li) ? li.get(0) : null;
    }

    @Override
    public <E> E queryFieldFirst(String field, Class<E> fieldClass, Map<String, Object> map, Order... order)
            throws DaoException {
        QueryContext q = new QueryContext();
        for (Iterator<Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext(); ) {
            Entry<String, Object> entry = it.next();
            q.andEquals(entry.getKey(), entry.getValue());
        }
        return this.queryFieldFirst(field, fieldClass, q, order);
    }

    @Override
    public <E> E queryFieldFirst(String field, Class<E> fieldClass, Order... order) throws DaoException {
        QueryContext q = new QueryContext();
        return this.queryFieldFirst(field, fieldClass, q, order);
    }

    @Override
    public <E> E queryFieldFirst(String field, Class<E> fieldClass, QueryContext queryContext, Order... order)
            throws DaoException {
        List<Object> params = new ArrayList<>();
        StringBuffer sb = new StringBuffer();
        sb.append(CONST_SELECT).append("DISTINCT(").append(this.metadata.getColumnByField(field).getNameWithQuote())
                .append(") as `").append(field).append("` ").append(CONST_FROM)
                .append(this.metadata.getQualifiedTableName()).append(CONST_WHERE).append(CONST_ONE);
        this.parseQueryContext(sb, queryContext, params);
        this.parseOrder(sb, order);
        sb.append(" limit 0,1");

        final String sql = sb.toString();
        final Object[] paramArray = params.toArray(new Object[params.size()]);
        try {
            List<T> li = this.jdbcTemplate.query(sql, paramArray, this.createRowMapper());
            if (CollectionUtils.isEmpty(li)) {
                return null;
            }
            T pojo = li.get(0);
            return ObjectUtils.getValue(pojo, fieldClass, field);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }

    @Override
    public <E> E queryFieldFirst(String field, Class<E> fieldClass, String whereField, Object whereValue,
                                 Order... order) throws DaoException {
        return this.queryFieldFirst(field, fieldClass, new String[]{whereField}, new Object[]{whereValue}, order);
    }

    @Override
    public <E> E queryFieldFirst(String field, Class<E> fieldClass, String[] whereFields, Object[] whereValues,
                                 Order... order) throws DaoException {
        if (whereFields == null || whereValues == null || whereFields.length != whereValues.length) {
            throw new IllegalArgumentException("whereFields and whereValues is illegal.");
        }
        QueryContext q = new QueryContext();
        for (int i = 0, len = whereFields.length; i < len; i++) {
            q.andEquals(whereFields[i], whereValues[i]);
        }
        return this.queryFieldFirst(field, fieldClass, q, order);
    }

    @Override
    public Object queryFieldFirst(String field, Map<String, Object> map, Order... order) throws DaoException {
        return this.queryFieldFirst(field, Object.class, map, order);
    }

    @Override
    public Object queryFieldFirst(String field, Order... order) throws DaoException {
        return this.queryFieldFirst(field, Object.class, order);
    }

    @Override
    public Object queryFieldFirst(String field, QueryContext queryContext, Order... order) throws DaoException {
        return this.queryFieldFirst(field, Object.class, queryContext, order);
    }

    @Override
    public Object queryFieldFirst(String field, String whereField, Object whereValue, Order... order)
            throws DaoException {
        return this.queryFieldFirst(field, Object.class, whereField, whereValue, order);
    }

    @Override
    public Object queryFieldFirst(String field, String[] whereFields, Object[] whereValues, Order... order)
            throws DaoException {
        return this.queryFieldFirst(field, Object.class, whereFields, whereValues, order);
    }

    @Override
    public <E> List<E> queryFieldValues(String field, Class<E> fieldClass, Map<String, Object> map, Order... order)
            throws DaoException {
        QueryContext q = new QueryContext();
        for (Iterator<Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext(); ) {
            Entry<String, Object> entry = it.next();
            q.andEquals(entry.getKey(), entry.getValue());
        }
        return this.queryFieldValues(field, fieldClass, q, order);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> List<E> queryFieldValues(String field, Class<E> fieldClass, QueryContext queryContext, Order... order)
            throws DaoException {
        List<Object> params = new ArrayList<>();
        StringBuffer sb = new StringBuffer();
        sb.append(CONST_SELECT).append("DISTINCT(").append(this.metadata.getColumnByField(field).getNameWithQuote())
                .append(") as `").append(field).append("` ").append(CONST_FROM)
                .append(this.metadata.getQualifiedTableName()).append(CONST_WHERE).append(CONST_ONE);
        this.parseQueryContext(sb, queryContext, params);
        this.parseOrder(sb, order);

        final String sql = sb.toString();
        final Object[] paramArray = params.toArray(new Object[params.size()]);
        try {
            List<T> li = this.jdbcTemplate.query(sql, paramArray, this.createRowMapper());
            if (CollectionUtils.isEmpty(li)) {
                return ListUtils.EMPTY_LIST;
            }
            List<E> result = new ArrayList<>();
            for (T t : li) {
                E foo = ObjectUtils.getValue(t, fieldClass, field);
                result.add(foo);
            }
            return result;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }

    @Override
    public <E> List<E> queryFieldValues(String field, Class<E> fieldClass, String whereField, Object whereValue,
                                        Order... order) throws DaoException {
        return this.queryFieldValues(field, fieldClass, new String[]{whereField}, new Object[]{whereValue},
                order);
    }

    @Override
    public <E> List<E> queryFieldValues(String field, Class<E> fieldClass, String[] whereFields, Object[] whereValues,
                                        Order... order) throws DaoException {
        if (whereFields == null || whereValues == null || whereFields.length != whereValues.length) {
            throw new IllegalArgumentException("whereFields and whereValues illegal.");
        }
        QueryContext q = new QueryContext();
        for (int i = 0, len = whereFields.length; i < len; i++) {
            q.andEquals(whereFields[i], whereValues[i]);
        }
        return this.queryFieldValues(field, fieldClass, q, order);
    }

    @Override
    public List<Object> queryFieldValues(String field, Map<String, Object> map, Order... order) throws DaoException {
        return this.queryFieldValues(field, Object.class, map, order);
    }

    @Override
    public List<Object> queryFieldValues(String field, QueryContext queryContext, Order... order) throws DaoException {
        return this.queryFieldValues(field, Object.class, queryContext, order);
    }

    @Override
    public List<Object> queryFieldValues(String field, String whereField, Object whereValue, Order... order)
            throws DaoException {
        return this.queryFieldValues(field, Object.class, new String[]{whereField}, new Object[]{whereValue},
                order);
    }

    @Override
    public List<Object> queryFieldValues(String field, String[] whereFields, Object[] whereValues, Order... order)
            throws DaoException {
        return this.queryFieldValues(field, Object.class, whereFields, whereValues, order);
    }

    @Override
    public T queryFirst(Map<String, Object> map, Order... order) throws DaoException {
        StringBuffer sb = new StringBuffer();
        sb.append(CONST_SELECT).append(this.selectHead).append(CONST_FROM).append(this.metadata.getQualifiedTableName())
                .append(CONST_WHERE).append(CONST_ONE);
        if (MapUtils.isNotEmpty(map)) {
            for (Iterator<Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext(); ) {
                Entry<String, Object> entry = it.next();
                EntityColumnMetadata col = this.metadata.getColumnByField(entry.getKey());
                sb.append(CONST_AND).append(col.getName()).append(CONST_EQ).append(":").append(col.getField());
            }
        }

        this.parseOrder(sb, order);

        final String sql = sb.toString();
        final List<T> li = this.namedParameterJdbcTemplate.query(sql, map, createRowMapper());
        return CollectionUtils.isNotEmpty(li) ? li.get(0) : null;
    }

    @Override
    public T queryFirst(QueryContext context, Order... order) throws DaoException {
        List<T> li = this.query(context, 0, 1, order);
        return CollectionUtils.isNotEmpty(li) ? li.get(0) : null;
    }

    @Override
    public T queryFirst(String field, Object value, Order... order) throws DaoException {
        final QueryContext ctx = new QueryContext();
        ctx.and(field, Operator.eq, value);
        return this.queryFirst(ctx, order);
    }

    @Override
    public T queryFirst(String[] fields, Object[] values, Order... order) throws DaoException {
        if (fields == null || values == null) {
            throw new DaoException("query fields and values cannot be null.");
        }
        if (fields.length != values.length) {
            throw new DaoException("fields.length doesn't equal with values.length.");
        }
        final QueryContext ctx = new QueryContext();
        for (int i = 0, len = fields.length; i < len; i++) {
            ctx.and(fields[i], Operator.eq, values[i]);
        }
        return this.queryFirst(ctx, order);
    }

    @Override
    public List<T> queryLimit(int limitFrom, int limitSize, Order... order) throws DaoException {
        return queryLimit(null, limitFrom, limitSize, order);
    }

    @Override
    public List<T> queryLimit(QueryContext context, int limitFrom, int limitSize, Order... order) throws DaoException {
        if (limitFrom < 0 || limitSize < 1) {
            throw new IllegalArgumentException("illegal limit params.");
        }
        StringBuffer sb = new StringBuffer();
        sb.append(CONST_SELECT).append(this.selectHead).append(CONST_FROM).append(this.metadata.getQualifiedTableName())
                .append(CONST_WHERE).append(CONST_ONE);
        List<Object> params = new ArrayList<>();
        this.parseQueryContext(sb, context, params);
        parseOrder(sb, order);
        sb.append(" limit ").append(limitFrom).append(',').append(limitSize);
        final String sql = sb.toString();
        return this.jdbcTemplate.query(sql, params.toArray(new Object[params.size()]), createRowMapper());
    }

    @Override
    public List<T> queryLimit(String field, Object value, int limitFrom, int limitSize, Order... order)
            throws DaoException {
        QueryContext context = new QueryContext().andEquals(field, value);
        return this.queryLimit(context, limitFrom, limitSize, order);
    }

    @Override
    public List<T> queryLimit(String[] fields, Object[] values, int limitFrom, int limitSize, Order... order)
            throws DaoException {
        if (fields == null || values == null || fields.length != values.length) {
            throw new IllegalArgumentException("fields.length!=values.length");
        }
        QueryContext context = new QueryContext();
        for (int i = 0, len = fields.length; i < len; i++) {
            context.andEquals(fields[i], values[i]);
        }
        return this.queryLimit(context, limitFrom, limitSize, order);
    }

    @Override
    public Number queryMaxValue(String field) throws DaoException {
        return this.innerFunctionQuery(field, "max");
    }

    @Override
    public Number queryMaxValue(String field, QueryContext queryContext) throws DaoException {
        return this.innerFunctionQuery(field, "max", queryContext);
    }

    @Override
    public Number queryMaxValue(String field, String whereField, Object whereValue) throws DaoException {
        return this.innerFunctionQuery(field, "max", whereField, whereValue);
    }

    @Override
    public Number queryMaxValue(String field, String[] whereFields, Object[] whereValues) throws DaoException {
        return this.innerFunctionQuery(field, "max", whereFields, whereValues);
    }

    @Override
    public Number queryMinValue(String field) throws DaoException {
        return this.innerFunctionQuery(field, "min");
    }

    @Override
    public Number queryMinValue(String field, QueryContext queryContext) throws DaoException {
        return this.innerFunctionQuery(field, "min", queryContext);
    }

    @Override
    public Number queryMinValue(String field, String whereField, Object whereValue) throws DaoException {
        return this.innerFunctionQuery(field, "min", whereField, whereValue);
    }

    @Override
    public Number queryMinValue(String field, String[] whereFields, Object[] whereValues) throws DaoException {
        return this.innerFunctionQuery(field, "min", whereFields, whereValues);
    }

    @Override
    public Number querySumValue(String field) throws DaoException {
        return this.innerFunctionQuery(field, "sum");
    }

    @Override
    public Number querySumValue(String field, String whereField, Object whereValue) throws DaoException {
        return this.innerFunctionQuery(field, "sum", whereField, whereValue);
    }

    @Override
    public Number querySumValue(String field, String[] whereFields, Object[] whereValues) throws DaoException {
        return this.innerFunctionQuery(field, "sum", whereFields, whereValues);
    }

    @Override
    public Page<T> queryWithPage(int pageNo, int pageSize, Order... order) throws DaoException {
        return this.queryWithPage(pageNo, pageSize, null, order);
    }

    @Override
    public Page<T> queryWithPage(int pageNo, int pageSize, QueryContext context, Order... order) throws DaoException {
        if (pageSize < 1) {
            throw new DaoException("page size must be ");
        }

        StringBuffer sb = new StringBuffer();
        sb.append(CONST_SELECT).append(this.selectHead).append(CONST_FROM).append(this.metadata.getQualifiedTableName())
                .append(CONST_WHERE).append(CONST_ONE);
        final List<Object> params = new ArrayList<>();
        this.parseQueryContext(sb, context, params);
        this.parseOrder(sb, order);

        int psize = pageSize;
        int pageIndex = (Math.max(Page.DEFAULT_PAGE_NO, pageNo) - 1) * psize;
        sb.append(" limit ").append(pageIndex).append(",").append(psize);
        final String sql = sb.toString();
        try {
            List<T> li = this.jdbcTemplate.query(sql, params.toArray(new Object[params.size()]), createRowMapper());
            long count = this.count(context);
            Page<T> page = new Page<T>(li, count, pageNo, pageSize);
            return page;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new DaoException(e);
        }
    }

    @Override
    public Page<T> queryWithPage(int pageNo, int pageSize, String field, Object value, Order... order)
            throws DaoException {
        final QueryContext context = new QueryContext();
        context.and(field, Operator.eq, value);
        return this.queryWithPage(pageNo, pageSize, context, order);
    }

    @Override
    public Page<T> queryWithPage(int pageNo, int pageSize, String[] fields, Object[] values, Order... order)
            throws DaoException {
        if (fields == null || values == null) {
            throw new IllegalArgumentException("fields or values is null.");
        }
        if (fields.length != values.length) {
            throw new IllegalArgumentException("length of fields and values doesn't match.");
        }

        final QueryContext context = new QueryContext();
        for (int i = 0, len = fields.length; i < len; i++) {
            context.andEquals(fields[i], values[i]);
        }
        return this.queryWithPage(pageNo, pageSize, context, order);
    }

    @Override
    @Transactional
    public void save(T... t) throws DaoException {
        for (T foo : t) {
            this.innerInsert(foo);
        }
    }

    @Override
    @Transactional
    public void saveOrUpdate(Collection<T> t) throws DaoException {
        this.saveOrUpdate(simple.orm.utils.ArrayUtils.toArray(t, this.metadata.getClazz()));
    }

    @Override
    @Transactional
    public void saveOrUpdate(T... t) throws DaoException {
        for (T foo : t) {
            Serializable pk = this.getPrimaryValue(foo);
            boolean isAuto = this.getMetadata().getPrimaryKey().isAuto();
            if ((isAuto && (pk == null || ObjectUtils.isZero(pk))) || (!isAuto || this.load(pk) == null)) {
                this.innerInsert(foo);
            } else {
                this.innerUpdate(foo);
            }
        }
    }

    @Override
    @Transactional
    public void update(T... t) throws DaoException {
        for (T foo : t) {
            this.innerUpdate(foo);
        }
    }

    @Override
    public void updateField(Serializable pk, String field, Object value) throws DaoException {
        this.updateField(pk, new String[]{field}, new Object[]{value});
    }

    @Override
    public void updateField(Serializable pk, String[] fields, Object[] values) throws DaoException {
        if (pk == null) {
            throw new IllegalArgumentException("primary key cannot be null.");
        }

        if (ArrayUtils.isEmpty(fields) || ArrayUtils.isEmpty(values)) {
            throw new IllegalArgumentException("fields and values cannot be empty.");
        }

        if (fields.length != values.length) {
            throw new IllegalArgumentException("length of fields must be equal with values.");
        }

        StringBuffer sb = new StringBuffer();
        sb.append(CONST_UPDATE).append(this.metadata.getQualifiedTableName()).append(" set ");
        for (int i = 0, len = fields.length; i < len; i++) {
            String field = fields[i];
            if (StringUtils.isBlank(field)) {
                throw new IllegalArgumentException("field name cannot be blank.");
            }
            sb.append(this.metadata.getColumnByField(field).getNameWithQuote()).append("=?");
            if (i != len - 1) {
                sb.append(",");
            }
        }
        sb.append(CONST_WHERE).append(this.metadata.getPrimaryKey().getNameWithQuote()).append("=?");

        final String sql = sb.toString();

        Object[] values2 = new Object[values.length + 1];
        for (int i = 0, len = values.length; i < len; i++) {
            values2[i] = values[i];
        }
        values2[values2.length - 1] = pk;
        try {
            this.jdbcTemplate.update(sql, values2);
        } catch (Throwable e) {
            throw new DaoException(e);
        }
    }

    @Override
    public void delete(Map<String, Object> map) throws DaoException {
        if (MapUtils.isEmpty(map)) {
            if (log.isDebugEnabled()) {
                log.debug("delete by map cancel: map is empty.");
            }
            return;
        }
        int len = map.size();

        String[] fields = new String[len];
        Object[] values = new Object[len];
        int i = 0;
        for (Iterator<Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext(); i++) {
            Entry<String, Object> entry = it.next();
            fields[i] = entry.getKey();
            values[i] = entry.getValue();
        }
        this.delete(fields, values);
    }

    @Override
    public void delete(QueryContext queryContext) throws DaoException {
        StringBuffer sb = new StringBuffer();
        sb.append(CONST_DELETE).append(CONST_FROM).append(this.metadata.getQualifiedTableName()).append(CONST_WHERE)
                .append(CONST_ONE);
        List<Object> params = new ArrayList<>();
        this.parseQueryContext(sb, queryContext, params);

        if (CollectionUtils.isEmpty(params)) {
            if (log.isDebugEnabled()) {
                log.debug("delete by query context cancel: querycontext is empty.");
            }
            return;
        }
        this.jdbcTemplate.update(sb.toString(), params.toArray(new Object[params.size()]));
    }
}