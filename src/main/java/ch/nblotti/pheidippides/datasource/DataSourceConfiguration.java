package ch.nblotti.pheidippides.datasource;

import ch.nblotti.pheidippides.GeneratedExcludeJacocoTestCoverage;
import ch.nblotti.pheidippides.statemachine.EVENTS;
import ch.nblotti.pheidippides.statemachine.STATES;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.statemachine.StateMachine;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@GeneratedExcludeJacocoTestCoverage
@Configuration
public class DataSourceConfiguration {


    @Value("${app.basic.flyway.package}")
    private String basicPackage;

    @Bean
    public DataSource createDefaultSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:db;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("sa");

        return dataSource;
    }

    @Bean
    @Scope("singleton")
    public RoutingDataSource routingDatasource(StateMachine<STATES, EVENTS> stateMachine) {

        return new RoutingDataSource(createDefaultSource(), stateMachine);

    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(RoutingDataSource routingDatasource) {
        LocalContainerEntityManagerFactoryBean em
                = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(routingDatasource);
        em.setPackagesToScan(basicPackage);

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(additionalProperties());

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(RoutingDataSource routingDatasource) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory(routingDatasource).getObject());

        return transactionManager;
    }

    Properties additionalProperties() {
        Properties properties = new Properties();
        properties.setProperty("spring.h2.console.enabled", "false");
        properties.setProperty("spring.jpa.hibernate.ddl-auto", "update");
        properties.setProperty("spring.jpa.database-platform", "org.hibernate.dialect.PostgreSQL9Dialect");
        properties.setProperty("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");


        return properties;
    }
}
