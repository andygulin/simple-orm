# 简单的ORM框架，基于Spring-JDBC实现

### 安装到本地maven仓库
```
cd simple-orm
mvn clean install
```
### 添加到项目pom.xml中
```
<dependency>
	<groupId>simple.orm</groupId>
	<artifactId>simple-orm-jdbc</artifactId>
	<version>0.0.1</version>
</dependency>
```
### 加载ORM框架，XML方式
```
<context:component-scan base-package="simple.orm.cfg" />
```
### 加载ORM框架，注解方式
```
@Import(simple.orm.cfg.DataSourceConfiguration.class)
```
## 开始使用
### 创建数据库
```
CREATE TABLE `user` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `name` varchar(20) DEFAULT NULL,
    `age` int(11) DEFAULT NULL,
    `address` varchar(100) DEFAULT NULL,
    `createdAt` datetime DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```
### 创建Bean
```
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "user")
public class User implements Serializable {

    private static final long serialVersionUID = 4003033001780718635L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private int id;

	@Column(name = "name")
	private String name;

	@Column(name = "age")
	private int age;

	@Column(name = "address")
	private String address;

	@Column(name = "createdAt")
	private Date createdAt;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
}
```

### 创建DAO
```
import simple.orm.api.DaoSupport;

public interface UserDao extends DaoSupport<User> {

}
```

### 创建DAO实现
```
import org.springframework.stereotype.Repository;
import simple.orm.jdbc.impl.AbstractDaoSupportImpl;

@Repository
public class UserDaoImpl extends AbstractDaoSupportImpl<User> implements UserDao {

}
```

### 使用，注入DAO对象即可
```
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import simple.orm.api.Page;
import simple.orm.api.query.Order;
import simple.orm.api.query.QueryContext;
import simple.orm.exception.DaoException;

@Service
public class UserService {

    @Autowired
	private UserDao userDao;

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

	public void update() throws DaoException {
		User user = userDao.load(2);
		user.setName("hehe");
		userDao.update(user);
	}

	public void delete() throws DaoException {
		userDao.delete(1);
	}
}
```

### 更多方法等待你发现......