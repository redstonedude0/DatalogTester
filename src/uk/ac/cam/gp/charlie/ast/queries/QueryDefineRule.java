package uk.ac.cam.gp.charlie.ast.queries;

import java.util.ArrayList;
import java.util.List;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.queries.match.MatchCondition;

/**
 * Represents abstract definition of a Grakn Rule.
 * @author hrjh2@cam.ac.uk
 */
public class QueryDefineRule extends Query {

  /**
   * If null then this is the parent type (e.g. 'rule')
   */
  public final QueryDefineRule subs;
  //Unique string used to refer to this rule
  public final String identifier;

  //list of conditions which must be true to trigger the 'then'
  public List<MatchCondition> when = new ArrayList<>();

  //Then - in the form of an insert query (invoked if the rule is broken)
  public QueryInsert then;

  private static List<QueryDefineRule> instances = new ArrayList<>();
  public QueryDefineRule(String identifier, QueryDefineRule subs) {
    this.identifier = identifier;
    this.subs = subs;
    instances.add(this);
  }

  public static QueryDefineRule getFromIdentifier(String identifier) {
    for (QueryDefineRule v : instances) {
      if (v.identifier.equals(identifier)) {
        return v;
      }
    }
    return null;
  }

  public static final QueryDefineRule RULE =  new QueryDefineRule("rule", null);
}
