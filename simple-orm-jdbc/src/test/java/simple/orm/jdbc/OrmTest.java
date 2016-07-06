package simple.orm.jdbc;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;

import simple.orm.api.query.Order;
import simple.orm.cfg.DataSourceConfiguration;
import simple.orm.exception.DaoException;

@Transactional
@ContextConfiguration(classes = DataSourceConfiguration.class)
public class OrmTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	private UserDao userDao;

	@Test
	public void insert() throws DaoException {
		User user = new User();
		user.setName("aaa");
		user.setAge(11);
		user.setAddress("shanghai");
		user.setCreatedAt(new Date());
		userDao.save(user);
		System.out.println(user.getId());
	}

	@Test
	public void query() throws DaoException {
		User user = userDao.load(2);
		System.out.println(JSON.toJSONString(user));

		List<User> users = userDao.queryAll(Order.desc("id"));
		System.out.println(JSON.toJSONString(users));
	}
}
