# auradb-connection-issue
This code is designed to replicate a Neo4j AuraDB connection issue.

## How to use
Make following updates to ConnectionIssue.java
- Update HOST, PORT, USER, PASSWORD and DATABASE values per your neo4j aura instance

  ```java
  private static final String HOST = "abcd123efg.databases.neo4j.io";
  private static final long PORT = 7687;
  private static final String USER = "neo4j";
  private static final String PASSWORD = "DRsfDheerajf4xGeZLZBDV2KS-Pou_Ublb0AbNeA";
  private static final String DATABASE = "neo4j";
  ```

  - To reproduce the issue comment ln# 71, i.e. `.withConnectionLivenessCheckTimeout(50, TimeUnit.SECONDS)`
 
  - To increase or decrease the sleep time, update the value on ln# 48, i.e. `int sleepTime = 10;`. The value represent sleep time in minutes.
