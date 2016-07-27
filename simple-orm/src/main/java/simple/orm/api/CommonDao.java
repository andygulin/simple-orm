package simple.orm.api;

import java.util.List;

import simple.orm.api.join.JoinResult;
import simple.orm.api.query.Order;
import simple.orm.api.query.QueryContext;
import simple.orm.exception.DaoException;
import simple.orm.nsql.cmd.SQLCommand;
import simple.orm.nsql.cmd.SQLResult;

public interface CommonDao {

	SQLResult execute(SQLCommand command) throws DaoException;

	@Deprecated
	<A, B> List<JoinResult> leftJoin(Class<A> clazz1, Class<B> clazz2, String onField1, String onField2,
			QueryContext context1, QueryContext context2, Order[] order1, Order[] order2, int limitIndex, int limitSize)
			throws DaoException;

	<A, B> List<JoinResult> leftJoin(Class<A> clazz1, Class<B> clazz2, String onField1, String onField2,
			QueryContext context1, QueryContext context2, Order[] order1, Order[] order2) throws DaoException;

	<A, B> List<JoinResult> leftJoin(Class<A> clazz1, Class<B> clazz2, String onField1, String onField2,
			QueryContext context1, QueryContext context2) throws DaoException;

	<A, B> List<JoinResult> leftJoin(Class<A> clazz1, Class<B> clazz2, String onField1, String onField2)
			throws DaoException;

}