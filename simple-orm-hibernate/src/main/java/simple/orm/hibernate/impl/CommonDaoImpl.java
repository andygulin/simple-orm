package simple.orm.hibernate.impl;

import org.springframework.stereotype.Repository;
import simple.orm.api.CommonDao;
import simple.orm.api.join.JoinResult;
import simple.orm.api.query.Order;
import simple.orm.api.query.QueryContext;
import simple.orm.exception.DaoException;
import simple.orm.nsql.cmd.SQLCommand;
import simple.orm.nsql.cmd.SQLResult;

import java.util.List;

@Repository
public class CommonDaoImpl implements CommonDao {

    @Override
    public <A, B> List<JoinResult> leftJoin(Class<A> clazz1, Class<B> clazz2, String onField1, String onField2,
                                            QueryContext context1, QueryContext context2, Order[] order1, Order[] order2, int limitIndex, int limitSize)
            throws DaoException {
        return null;
    }

    @Override
    public <A, B> List<JoinResult> leftJoin(Class<A> clazz1, Class<B> clazz2, String onField1, String onField2,
                                            QueryContext context1, QueryContext context2, Order[] order1, Order[] order2) throws DaoException {
        return null;
    }

    @Override
    public <A, B> List<JoinResult> leftJoin(Class<A> clazz1, Class<B> clazz2, String onField1, String onField2,
                                            QueryContext context1, QueryContext context2) throws DaoException {
        return null;
    }

    @Override
    public <A, B> List<JoinResult> leftJoin(Class<A> clazz1, Class<B> clazz2, String onField1, String onField2)
            throws DaoException {
        return null;
    }

    @Override
    public SQLResult execute(SQLCommand command) throws DaoException {
        return null;
    }

}
