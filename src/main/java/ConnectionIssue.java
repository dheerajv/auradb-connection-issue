import org.neo4j.driver.AccessMode;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Logging;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

class ConnectionIssue {

  private static final String HOST = "a1ec17aa.databases.neo4j.io";
  private static final long PORT = 7687;
  private static final String USER = "neo4j";
  private static final String PASSWORD = "DRsfGQBDnZhJ1Af4xGeZLZBDV2KS-Pou_Ublb0AbNeA";
  private static final String DATABASE = "neo4j";

  static final int DEFAULT_FETCH_SIZE = 1000;
  static final int DEFAULT_CONNECTION_LIFETIME = 50;
  static final int DEFAULT_CONNECTION_POOL_SIZE = 50;
  static final int DEFAULT_CONNECTION_ACQUISITION_TIMEOUT = 2;
  static final String DRIVER_ROUTING_PREFIX_PLUS_S = "neo4j+s://";

  void reproduceIssue() {
    try (Driver driver = connect()) {
      executeQuery(driver);
    }
  }

  private void executeQuery(Driver instance) {

    try (Session session = createNewSession(instance, DATABASE, AccessMode.READ)) {
      for (int i = 0; i < 10; i++) {

        try {
          String openCypher = "MATCH (n) RETURN id(n) LIMIT 1";
          Result res = session.run(openCypher);
          res.forEachRemaining(
              row -> System.out.println(row.get(0).asLong())
          );

          //Mock being idle for 10 minutes after the first query
          if (i == 0) {
            int sleepTime = 10;
            System.out.println(MessageFormat.format("Going to sleep for {0} minutes..", sleepTime));
            for (int j = 0; j < sleepTime; j++) {
              System.out.println(MessageFormat.format("Remaining {0} minutes of sleep time..", sleepTime - j));
              Thread.sleep(60 * 1000);
            }

            System.out.println("Awake again..");
          }

        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  private Driver connect() {
    String uri = DRIVER_ROUTING_PREFIX_PLUS_S + HOST + ':' + PORT;

    Config config = Config.builder()
        .withMaxConnectionLifetime(DEFAULT_CONNECTION_LIFETIME, TimeUnit.MINUTES)
        .withMaxConnectionPoolSize(DEFAULT_CONNECTION_POOL_SIZE)
        .withConnectionAcquisitionTimeout(DEFAULT_CONNECTION_ACQUISITION_TIMEOUT, TimeUnit.MINUTES)
        .withRoutingTablePurgeDelay(1, TimeUnit.SECONDS)
        .withLogging(Logging.console(Level.INFO))
        .withDriverMetrics()
        .build();

    return GraphDatabase.driver(uri, AuthTokens.basic(USER, PASSWORD), config);
  }

  private Session createNewSession(Driver driver,
                                   String database,
                                   AccessMode mode) {
    SessionConfig.Builder sessionBuilder = SessionConfig.builder()
        .withFetchSize(DEFAULT_FETCH_SIZE)
        .withDatabase(database)
        .withDefaultAccessMode(mode);

    SessionConfig sessionConfig = sessionBuilder.build();
    return driver.session(sessionConfig);
  }
}
