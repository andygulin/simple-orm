package simple.orm.cfg;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import simple.orm.utils.OrmStartupService;

import javax.sql.DataSource;
import java.util.Iterator;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = "simple.orm.jdbc")
public class DataSourceConfiguration {

    @Bean(name = "dataSource")
    public DataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        org.apache.commons.configuration.Configuration configuration = null;
        try {
            configuration = new PropertiesConfiguration("jdbc.properties");
        } catch (ConfigurationException e1) {
            e1.printStackTrace();
        }
        Iterator<String> keys = configuration.getKeys("jdbc");
        while (keys.hasNext()) {
            String key = keys.next();
            String prop = StringUtils.substringAfter(key, ".");
            try {
                BeanUtils.copyProperty(dataSource, prop, configuration.getProperty(key));
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
        return dataSource;
    }

    @Bean(name = "jdbcTemplate")
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }

    @Bean(name = "namedParameterJdbcTemplate")
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(dataSource());
    }

    @Bean(name = "transactionManager")
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Lazy
    @Bean
    public DefaultLobHandler defaultLobHandler() {
        return new DefaultLobHandler();
    }

    @Bean
    public OrmStartupService ormStartupService() {
        return new OrmStartupService();
    }
}