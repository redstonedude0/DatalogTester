package uk.ac.cam.gp.charlie.ast.queries.match;

import java.util.ArrayList;
import java.util.List;
import uk.ac.cam.gp.charlie.ast.Variable;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsert;

/**
 * Represents abstract definition of a Grakn Concept Type or Grakn Rule. See subtypes for definition
 * of specific concept types.
 *
 * https://dev.grakn.ai/docs/schema/concepts#summary
 */
public class QueryMatch extends Query {

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
  private Action action = null;
  private List<Variable> DATA_GET;
  private Variable DATA_DELETE;
  private QueryInsert DATA_INSERT;
  public Action getAction() {
    return action;
  }
  public List<Variable> getDATA_GET() {
    return DATA_GET;
  }
  public Variable getDATA_DELETE() {
    return DATA_DELETE;
  }
  public QueryInsert getDATA_INSERT() {
    return DATA_INSERT;
  }

  /**
   * Set this to be a match...get query
   * @param v the variables to get, if null or empty then this returns all variables in match scope.
   */
  public void setActionGet(List<Variable> v) {
    if (action != null) {
      throw new RuntimeException("Match already " + action + " cannot be set to get");
    }
    action = Action.GET;
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
    action = Action.DELETE;
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
    action = Action.INSERT;
    DATA_INSERT = query;
  }

}
