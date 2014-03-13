/*
 * The MIT License
 *
 * Copyright 2014 J. David Mendoza.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.davidmendoza.hyperion.config;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.orm.hibernate4.HibernateExceptionTranslator;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author J. David Mendoza <jdmendozar@gmail.com>
 */
@Configuration
@EnableTransactionManagement
@Import(PropertyPlaceholderConfig.class)
public class DataConfig {

    private static final Logger log = LoggerFactory.getLogger(DataConfig.class);
    @Value("${hibernate.dialect}")
    protected String hibernateDialect;
    @Value("${hibernate.show_sql}")
    protected String hibernateShowSql;
    @Value("${hibernate.hbm2ddl.auto}")
    protected String hibernateHbm2DDL;
    @Value("${hibernate.cache.use_second_level_cache}")
    protected String hibernateSecondLevelCache;
    @Value("${hibernate.cache.provider_class}")
    protected String hibernateCacheClass;
    @Value("${hibernate.cache.region.factory_class}")
    protected String hibernateCacheRegionFactoryClass;
    @Value("${hibernate.default_schema}")
    protected String hibernateSchema;
    @Value("${jdbc.driverClassName}")
    protected String jdbcDriver;
    @Value("${jdbc.username}")
    protected String jdbcUsername;
    @Value("${jdbc.password}")
    protected String jdbcPassword;
    @Value("${jdbc.url}")
    protected String jdbcUrl;

    @Bean
    public SessionFactory sessionFactory() {

        LocalSessionFactoryBean factoryBean;
        try {
            factoryBean = new LocalSessionFactoryBean();
            Properties pp = new Properties();
            pp.setProperty("hibernate.dialect", hibernateDialect);
            pp.setProperty("hibernate.show_sql", hibernateShowSql);
            pp.setProperty("hibernate.hbm2ddl.auto", hibernateHbm2DDL);
            pp.setProperty("hibernate.cache.use_second_level_cache", hibernateSecondLevelCache);
            pp.setProperty("hibernate.cache.provider_class", hibernateCacheClass);
            pp.setProperty("hibernate.cache.region.factory_class", hibernateCacheRegionFactoryClass);
            pp.setProperty("hibernate.default_schema", hibernateSchema);

            factoryBean.setDataSource(dataSource());
            factoryBean.setPackagesToScan("org.davidmendoza.hyperion.model");
            factoryBean.setHibernateProperties(pp);
            factoryBean.afterPropertiesSet();
            return factoryBean.getObject();
        } catch (IOException e) {
            log.error("Couldn't configure the sessionFactory bean", e);
        }
        throw new RuntimeException("Couldn't configure the sessionFactory bean");
    }

    @Bean
    public DataSource dataSource() {
        LazyConnectionDataSourceProxy ds = new LazyConnectionDataSourceProxy(mainDataSource());
        return ds;
    }

    @Bean
    public DataSource mainDataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(jdbcDriver);
        ds.setUsername(jdbcUsername);
        ds.setPassword(jdbcPassword);
        ds.setUrl(jdbcUrl);
        return ds;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new HibernateTransactionManager(sessionFactory());
    }

    @Bean
    public HibernateExceptionTranslator hibernateExceptionTranslator() {
        return new HibernateExceptionTranslator();
    }

}
