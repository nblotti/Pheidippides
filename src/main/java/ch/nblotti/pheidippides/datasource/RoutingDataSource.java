package ch.nblotti.pheidippides.datasource;

import org.flywaydb.core.Flyway;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class RoutingDataSource extends AbstractRoutingDataSource {


  public static final String DB_MIGRATION_LOCATION = "db/migration";

  private final Map<DataSourceEnum, DataSource> dataSources = new HashMap<>();

  public RoutingDataSource(DataSource dataSource) {
    dataSources.put(DataSourceEnum.HSQL, dataSource);
  }


  @Override
  protected Object determineCurrentLookupKey() {


    if (dataSources.containsKey(DataSourceEnum.CUSTOM))
      return DataSourceEnum.CUSTOM;
    else if (dataSources.containsKey(DataSourceEnum.HSQL))
      return DataSourceEnum.HSQL;

    throw new IllegalStateException("No dataSource defined");
  }

  @Override
  protected DataSource determineTargetDataSource() {
    DataSourceEnum lookupKey = (DataSourceEnum) determineCurrentLookupKey();

    return (DataSource) dataSources.get(lookupKey);
  }

  public void createDataSource(String url, String user, String password) throws Exception {

    if (dataSources.containsKey(DataSourceEnum.CUSTOM)) {
      closeConnection(dataSources.get(DataSourceEnum.CUSTOM));
      dataSources.remove(DataSourceEnum.CUSTOM);
    }
    if (dataSources.containsKey(DataSourceEnum.HSQL)) {
      closeConnection(dataSources.get(DataSourceEnum.HSQL));
      dataSources.remove(DataSourceEnum.HSQL);
    }
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setUrl(url);
    dataSource.setUsername(user);
    dataSource.setPassword(password);

    Flyway flyway = Flyway.configure()
      .dataSource(url, user, password)
      .locations(DB_MIGRATION_LOCATION)
      .load();
    flyway.migrate();
    dataSources.put(DataSourceEnum.CUSTOM, dataSource);


  }

  private void closeConnection(DataSource ds) {

    try {
      ds.getConnection().close();
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }

  }

  @Override
  public void afterPropertiesSet() {

  }
}


