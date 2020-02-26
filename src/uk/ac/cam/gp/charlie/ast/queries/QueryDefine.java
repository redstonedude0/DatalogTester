package uk.ac.cam.gp.charlie.ast.queries;

import java.util.ArrayList;
import java.util.List;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.Plays;

/**
 * Represents a query to define the schema representing an entity or relation
 */
public class QueryDefine extends Query {

  /**
   * If null then this is the parent type itself (e.g. entity, relation...)
   */
  public final QueryDefine subs;
  //Unique string used to refer to this type
  public final String identifier;

  //List of roles this entity 'plays' iff entity
  public List<Plays> plays = new ArrayList<>();
  //List of attributes this entity or relation 'has'
  public List<Attribute> attributes = new ArrayList<>();
  //List of play relations this relationship 'relates' iff relationship
  public List<Plays> relates = new ArrayList<>();

  //List of instances (we keep this so that any 2 queries representing the same identifier are equal)
  private static List<QueryDefine> instances = new ArrayList<>();
  public QueryDefine(String identifier, QueryDefine subs) {
    this.identifier = identifier;
    this.subs = subs;
    instances.add(this);
  }

  public static QueryDefine getFromIdentifier(String identifier) {
    for (QueryDefine v : instances) {
      if (v.identifier.equals(identifier)) {
        return v;
      }
    }
    return null;
  }

  public static final QueryDefine THING = new QueryDefine("thing", null);
  public static final QueryDefine ENTITY = new QueryDefine("entity", null);
  public static final QueryDefine RELATION = new QueryDefine("relation", null);
}
