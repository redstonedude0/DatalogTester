package uk.ac.cam.gp.charlie;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author hrjh2@cam.ac.uk
 */
public abstract class Executor implements AutoCloseable{

  /**
   * Execute a query in this execution environment
   * @param query The query to execute, in graql
   * @return the Result of the transaction, or null if an error occured
   */
  protected abstract Result execute(String query);

  /**
   * Execute a batch of queries on an execution environment
   * see Executor.execute
   * @param tests A set of tests to run
   * @return A map from test strings to results, results are either Result or null
   */
  public final Map<String,Result> executeBatch(Set<String> tests) {
    Map<String,Result> toReturn = new HashMap<>();
    for (String t: tests) {
      toReturn.put(t,execute(t));
    }
    return toReturn;
  }

  @Override
  public void close() {
  }

}
