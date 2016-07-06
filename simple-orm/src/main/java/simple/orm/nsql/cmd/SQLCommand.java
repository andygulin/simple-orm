package simple.orm.nsql.cmd;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import simple.orm.exception.DaoException;
import simple.orm.utils.OrmApplicationContextUtils;

public class SQLCommand {

	private String sql;
	private List<Object> params;
	private Map<String, Class<?>> classMap;

	public SQLCommand(String sql, List<Object> params, Map<String, Class<?>> classMap) {
		this.sql = sql;
		this.params = params;
		this.classMap = classMap;
	}

	public String getSql() {
		return sql;
	}

	public List<Object> getParams() {
		return params;
	}

	public SQLResult execute() throws DaoException {
		Class<NamedParameterJdbcTemplate> namedTemplate = null;
		Class<JdbcTemplate> template = null;
		try {
			namedTemplate = (Class<NamedParameterJdbcTemplate>) Class
					.forName("org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate");
			template = (Class<JdbcTemplate>) Class.forName("org.springframework.jdbc.core.JdbcTemplate");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new DaoException(e);
		}
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = OrmApplicationContextUtils.getApplicationContext()
				.getBean(namedTemplate);
		JdbcTemplate jdbcTemplate = OrmApplicationContextUtils.getApplicationContext().getBean(template);

		if (StringUtils.strip(this.sql.toLowerCase()).startsWith("select")) {
			final List<Map<String, Object>> li = new ArrayList<>();
			RowCallbackHandler rch = new RowCallbackHandler() {
				@Override
				public void processRow(ResultSet rs) throws SQLException {
					ResultSetMetaData meta = rs.getMetaData();
					Map<String, Object> map;
					while (rs.next()) {
						map = new LinkedHashMap<>();
						for (int i = 1, len = meta.getColumnCount(); i <= len; i++) {
							String columnLabel = meta.getColumnLabel(i);
							map.put(columnLabel, rs.getObject(columnLabel));
						}
						li.add(map);
					}

				}
			};

			if (CollectionUtils.isEmpty(this.params)) {
				jdbcTemplate.query(this.sql, rch);
			} else {
				jdbcTemplate.query(this.sql, this.params.toArray(new Object[this.params.size()]), rch);
			}
			return new MappedSQLResult(li, this.classMap);
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return this.sql + ";" + (this.params == null ? "" : (" [" + StringUtils.join(this.params, ",") + "]"));
	}
}
