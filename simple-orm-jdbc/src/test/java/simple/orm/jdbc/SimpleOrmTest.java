package simple.orm.jdbc;

import com.alibaba.fastjson2.JSON;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import simple.orm.api.Page;
import simple.orm.api.query.Order;
import simple.orm.api.query.QueryContext;
import simple.orm.cfg.DataSourceConfiguration;
import simple.orm.exception.DaoException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Transactional
@ContextConfiguration(classes = DataSourceConfiguration.class)
public class SimpleOrmTest extends AbstractJUnit4SpringContextTests {

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

        List<User> users = new ArrayList<>();
        user = new User();
        user.setName("bbb");
        user.setAge(12);
        user.setAddress("beijing");
        user.setCreatedAt(new Date());
        users.add(user);
        user = new User();
        user.setName("ccc");
        user.setAge(13);
        user.setAddress("nanjing");
        user.setCreatedAt(new Date());
        users.add(user);
        userDao.saveOrUpdate(users);
        for (User u : users) {
            System.out.println(u.getId());
        }
    }

    @Test
    public void query() throws DaoException {
        User user = userDao.load(2);
        System.out.println(JSON.toJSONString(user));

        List<User> users = userDao.queryAll(Order.desc("id"));
        System.out.println(JSON.toJSONString(users));

        QueryContext context = new QueryContext();
        context.andEquals("name", "bbb");
        users = userDao.query(context);
        System.out.println(JSON.toJSONString(users));

        Page<User> page = userDao.queryWithPage(1, 2, Order.desc("id"));
        System.out.println(JSON.toJSONString(page.getResult()));
    }

    @Test
    public void update() throws DaoException {
        User user = userDao.load(2);
        user.setName("hehe");
        userDao.update(user);
    }

    @Test
    public void delete() throws DaoException {
        userDao.delete(1);
    }
}