package uk.ac.cam.gp.charlie.ast.queries;

import java.util.ArrayList;
import java.util.List;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.queries.match.MatchCondition;

/**
 * Represents abstract definition of a Grakn Rule. See subtypes for definition
 * of specific concept types.
 *
 * https://dev.grakn.ai/docs/schema/concepts#summary
 */
public class QueryDefineRule extends Query {

  /**
   * If null then this is the parent type (e.g. 'rule')
   */
  public final QueryDefineRule subs;
  //Unique string used to refer to this object
  public final String identifier;

  //when
  public List<MatchCondition> when = new ArrayList<>();

  //Then (always of the form <anonymous something (relations) > isa explicit_type;
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
