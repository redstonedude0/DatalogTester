package uk.ac.cam.gp.charlie.ast.queries;

import java.nio.channels.AlreadyBoundException;
import java.util.ArrayList;
import java.util.List;
import uk.ac.cam.gp.charlie.ast.Variable;

/**
 * Represents abstract definition of a Grakn Concept Type or Grakn Rule. See subtypes for definition
 * of specific concept types.
 *
 * https://dev.grakn.ai/docs/schema/concepts#summary
 */
public abstract class QueryMatch extends Query {

  /**
   * If null then this subtypes the parent type directly (e.g. entity, relation...)
   */
  public List<MatchCondition> conditions = new ArrayList<>();
  //TODO store modifiers (e.g. limit, sort)

  public enum Action {
    GET,
    DELETE,
    INSERT
  }
  public Action action = null;

  private List<Variable> DATA_GET;
  private Variable DATA_DELETE;
  private QueryInsert DATA_INSERT;

  protected QueryMatch() {}


  /**
   * Set this to be a match...get query
   * @param v the variables to get, if null or empty then this returns all variables in match scope.
   */
  public void setActionGet(List<Variable> v) {
    if (action != null) {
      throw new RuntimeException("Match already " + action + " cannot be set to get");
    }
    DATA_GET = v;
  }

  /**
   * Set this to be a match...delete query
   * @param v the variable to delete, e.g. x in "delete $x"
   */
  public void setActionDelete(Variable v) {
    if (action != null) {
      throw new RuntimeException("Match already " + action + " cannot be set to delete");
    }
    DATA_DELETE = v;
  }

  /**
   * Set this to be a match...insert query
   * @param query the query to insert, you can assume variables defined in the match... part are bound in the insertion
   */
  public void setActionInsert(QueryInsert query) {
    if (action != null) {
      throw new RuntimeException("Match already " + action + " cannot be set to insert");
    }
    DATA_INSERT = query;
  }

  public class MatchCondition {

  }


}
