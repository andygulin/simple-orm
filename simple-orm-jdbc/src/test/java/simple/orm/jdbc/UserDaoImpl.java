package simple.orm.jdbc;

import org.springframework.stereotype.Repository;

import simple.orm.jdbc.impl.AbstractDaoSupportImpl;

@Repository
public class UserDaoImpl extends AbstractDaoSupportImpl<User> implements UserDao {

}