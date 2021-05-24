package ch.nblotti.Pheidippides;

import ch.nblotti.Pheidippides.database.RoutingDataSource;
import ch.nblotti.Pheidippides.statemachine.EVENTS;
import ch.nblotti.Pheidippides.statemachine.STATES;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@SpringBootApplication(exclude = {
  FlywayAutoConfiguration.class})
@Slf4j
public class PheidippidesApplication {

  public static void main(String[] args) {
    SpringApplication.run(PheidippidesApplication.class, args);

  }




  @Value("${spring.zookeeper.connect-string}")
  private String connectString;

  @Value("${global.full-date-format}")
  public String messageDateFormat;

  @Autowired
  private StateMachineFactory<STATES, EVENTS> stateMachineFactory;


  @Bean
  public DateTimeFormatter formatMessage() {
    return DateTimeFormatter.ofPattern(messageDateFormat);
  }


  @Bean("stateMachine")
  @Scope("singleton")
  public StateMachine<STATES, EVENTS> stateMachine() {

    return  stateMachineFactory.getStateMachine();

  }

  @Bean
  @Scope("singleton")
  ZkClient zkClient() {

    return new ZkClient(connectString, 12000, 3000, zkSerializer());
  }

  public ZkSerializer zkSerializer() {
    return new ZkSerializer() {

      @Override
      public byte[] serialize(Object data) throws ZkMarshallingError {
        try {
          return ((String) data).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public Object deserialize(byte[] bytes) throws ZkMarshallingError {
        if (bytes == null)
          return null;
        try {
          return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  @Bean
  ModelMapper modelMapper() {
    return new ModelMapper();
  }


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
  public RoutingDataSource routingDatasource() {

    return new RoutingDataSource(createDefaultSource());

  }

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(RoutingDataSource routingDatasource) {
    LocalContainerEntityManagerFactoryBean em
      = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(routingDatasource);
    em.setPackagesToScan(new String[]{"ch.nblotti.chronos"});

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
    properties.setProperty("spring.jpa.database-platform", "org.hibernate.dialect.H2Dialect");
    properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");


    return properties;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void doSomethingAfterStartup() {
    stateMachine().sendEvent(EVENTS.EVENT_RECEIVED);
  }

}
