package simple.orm.jdbc.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import simple.orm.api.CommonDao;
import simple.orm.api.join.JoinResult;
import simple.orm.api.query.Order;
import simple.orm.api.query.QueryContext;
import simple.orm.exception.DaoException;
import simple.orm.meta.EntityColumnMetadata;
import simple.orm.nsql.cmd.SQLCommand;
import simple.orm.nsql.cmd.SQLResult;
import simple.orm.utils.AopTargetUtils;
import simple.orm.utils.DaoUtils;
import simple.orm.utils.ObjectUtils;

import java.util.*;
import java.util.Map.Entry;

@Repository
public class CommonDaoImpl implements CommonDao {

	private static transient final Log log = LogFactory.getLog(CommonDaoImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@SuppressWarnings("unchecked")
	@Override
	public <A, B> List<JoinResult> leftJoin(Class<A> clazz1, Class<B> clazz2, String onField1, String onField2,
			QueryContext context1, QueryContext context2, Order[] order1, Order[] order2, int limitIndex, int limitSize)
			throws DaoException {
		AbstractDaoSupportImpl<A> dao1 = this.getDaoSupport(clazz1);
		AbstractDaoSupportImpl<B> dao2 = this.getDaoSupport(clazz2);
		StringBuffer sb = new StringBuffer();
		sb.append(AbstractDaoSupportImpl.CONST_SELECT);

		String alias1 = "foo";
		String alias2 = "bar";

		List<String> tmpArr = new ArrayList<>();

		StringBuffer ssb;
		for (EntityColumnMetadata metadata : dao1.getMetadata().getAllColumnMetadatas()) {
			ssb = new StringBuffer();
			ssb.append(alias1).append(".").append("`").append(metadata.getName()).append("`").append(" as ")
					.append(alias1).append("___").append(metadata.getField());
			tmpArr.add(ssb.toString());
		}

		for (EntityColumnMetadata metadata : dao2.getMetadata().getAllColumnMetadatas()) {
			ssb = new StringBuffer();
			ssb.append(alias2).append(".").append("`").append(metadata.getName()).append("`").append(" as ")
					.append(alias2).append("___").append(metadata.getField());
			tmpArr.add(ssb.toString());
		}
		sb.append(StringUtils.join(tmpArr, ","));

		sb.append(AbstractDaoSupportImpl.CONST_FROM).append(dao1.getMetadata().getQualifiedTableName()).append(" as ")
				.append(alias1).append(" left join ").append(dao2.getMetadata().getQualifiedTableName()).append(" as ")
				.append(alias2).append(" on ").append(alias1).append(".")
				.append(dao1.getMetadata().getColumnByField(onField1).getName()).append(AbstractDaoSupportImpl.CONST_EQ)
				.append(alias2).append(".").append(dao2.getMetadata().getColumnByField(onField2).getName())
				.append(AbstractDaoSupportImpl.CONST_WHERE).append(" 1=1 ");

		List<Object> params = new ArrayList<>();
		dao1.parseQueryContext(sb, context1, params, alias1);
		dao2.parseQueryContext(sb, context2, params, alias2);

		if (ArrayUtils.isNotEmpty(order1) || ArrayUtils.isNotEmpty(order2)) {
			sb.append(" order by ");
		}
		List<String> orderStrArr = new ArrayList<>();

		if (ArrayUtils.isNotEmpty(order1)) {
			for (Order foo : order1) {
				orderStrArr.add(alias1 + ".`" + dao1.getMetadata().getColumnByField(foo.getField()).getName() + "` "
						+ (foo.isAsc() ? "asc" : "desc"));
			}
		}
		if (ArrayUtils.isNotEmpty(order2)) {
			for (Order foo : order2) {
				orderStrArr.add(alias2 + ".`" + dao2.getMetadata().getColumnByField(foo.getField()).getName() + "` "
						+ (foo.isAsc() ? "asc" : "desc"));
			}
		}

		sb.append(StringUtils.join(orderStrArr, ","));
		if (limitIndex > -1 && limitSize > 0) {
			sb.append(" limit ").append(limitIndex).append(",").append(limitSize);
		}

		final String sql = sb.toString();

		if (log.isDebugEnabled()) {
			log.debug("left join: " + sql);
		}

		List<Map<String, Object>> li = this.jdbcTemplate.queryForList(sql, params.toArray(new Object[params.size()]));

		if (CollectionUtils.isEmpty(li)) {
			return ListUtils.EMPTY_LIST;
		}
		List<JoinResult> result = new ArrayList<>(li.size());
		Map<String, Object> map1;
		Map<String, Object> map2;
		JoinResult jr;
		for (Map<String, Object> foo : li) {
			jr = new JoinResult();
			map1 = new HashMap<>();
			map2 = new HashMap<>();
			for (Iterator<Entry<String, Object>> it = foo.entrySet().iterator(); it.hasNext();) {
				Entry<String, Object> entry = it.next();
				if (entry.getKey().startsWith(alias1 + "___")) {
					map1.put(entry.getKey().substring(alias1.length() + 3), entry.getValue());
				} else if (entry.getKey().startsWith(alias2 + "___")) {
					map2.put(entry.getKey().substring(alias2.length() + 3), entry.getValue());
				}
			}
			jr.add(ObjectUtils.fromMap(map1, clazz1));
			jr.add(ObjectUtils.fromMap(map2, clazz2));
			result.add(jr);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private <T> AbstractDaoSupportImpl<T> getDaoSupport(Class<T> clazz) {
		try {
			return (AbstractDaoSupportImpl<T>) AopTargetUtils.getTarget(DaoUtils.getDaoSupport(clazz));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public <A, B> List<JoinResult> leftJoin(Class<A> clazz1, Class<B> clazz2, String onField1, String onField2,
			QueryContext context1, QueryContext context2, Order[] order1, Order[] order2) throws DaoException {
		return this.leftJoin(clazz1, clazz2, onField1, onField2, context1, context2, order1, order2, -1, -1);
	}

	@Override
	public <A, B> List<JoinResult> leftJoin(Class<A> clazz1, Class<B> clazz2, String onField1, String onField2,
			QueryContext context1, QueryContext context2) throws DaoException {
		return this.leftJoin(clazz1, clazz2, onField1, onField2, context1, context2, null, null);
	}

	@Override
	public <A, B> List<JoinResult> leftJoin(Class<A> clazz1, Class<B> clazz2, String onField1, String onField2)
			throws DaoException {
		return this.leftJoin(clazz1, clazz2, onField1, onField2, null, null);
	}

	@Override
	public SQLResult execute(SQLCommand command) throws DaoException {
		return null;
	}
}