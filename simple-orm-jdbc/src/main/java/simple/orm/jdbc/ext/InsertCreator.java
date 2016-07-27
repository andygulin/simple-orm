package simple.orm.jdbc.ext;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SerializationUtils;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;

import com.alibaba.fastjson.JSON;

import simple.orm.meta.EntityColumnMetadata;
import simple.orm.meta.EntityMetadata;
import simple.orm.utils.ObjectUtils;

public class InsertCreator<T> implements PreparedStatementCreator {

	private LobHandler lobHandler;
	private T t;
	private EntityColumnMetadata[] metadatas;
	private String[] columnNames;
	private String sql;

	public InsertCreator(LobHandler lobHandler, T t, EntityMetadata<T> entityMetadata) {
		this.lobHandler = lobHandler;
		this.t = t;
		Set<EntityColumnMetadata> set = entityMetadata.getPrimaryKey().isAuto() ? entityMetadata.getAllColumnMetadatas()
				: entityMetadata.getAllColumnMetadatasNoPK();
		this.metadatas = set.toArray(new EntityColumnMetadata[set.size()]);
		this.columnNames = new String[metadatas.length];
		StringBuffer sb = new StringBuffer();
		sb.append("insert into ").append(entityMetadata.getQualifiedTableName()).append("(");
		StringBuffer sb2 = new StringBuffer();
		for (int i = 0, len = this.metadatas.length; i < len; i++) {
			EntityColumnMetadata bar = this.metadatas[i];
			sb.append(bar.getNameWithQuote()).append((i == len - 1) ? ") values(" : ",");
			sb2.append("?").append((i == len - 1) ? ")" : ",");
			this.columnNames[i] = bar.getName();
		}
		sb.append(sb2);
		this.sql = sb.toString();
	}

	@Override
	public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
		final PreparedStatement ps = con.prepareStatement(this.sql, this.columnNames);
		for (int i = 0, len = this.metadatas.length; i < len; i++) {
			EntityColumnMetadata foo = this.metadatas[i];
			if (!foo.isLob()) {
				ps.setObject(i + 1, ObjectUtils.getValue(this.t, foo.getField()));
			} else {
				try {
					this.fillLob(ps, i + 1, ObjectUtils.getValue(this.t, foo.getField()));
				} catch (IOException e) {
					e.printStackTrace();
					throw new SQLException(e);
				}
			}
		}
		return ps;
	}

	private void fillLob(PreparedStatement ps, int i, Object value) throws SQLException, IOException {
		LobCreator lobCreator = this.lobHandler.getLobCreator();
		if (value instanceof InputStream) {
			lobCreator.setBlobAsBinaryStream(ps, i, (InputStream) value, ((InputStream) value).available());
		} else if (value instanceof Reader) {
			lobCreator.setClobAsString(ps, i, IOUtils.toString((Reader) value));
		} else if (value instanceof String) {
			lobCreator.setClobAsString(ps, i, (String) value);
		} else if (value instanceof byte[] || value instanceof Byte[]) {
			lobCreator.setBlobAsBytes(ps, i, (byte[]) value);
		} else if (value instanceof Serializable) {
			lobCreator.setBlobAsBytes(ps, i, SerializationUtils.serialize((Serializable) value));
		} else {
			lobCreator.setClobAsString(ps, i, JSON.toJSONString(value));
		}
	}
}