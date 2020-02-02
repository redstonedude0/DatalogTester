package uk.ac.cam.gp.charlie.ast.queries;

/**
 * Represents abstract definition of a Grakn Concept Type or Grakn Rule. See subtypes for definition
 * of specific concept types.
 *
 * https://dev.grakn.ai/docs/schema/concepts#summary
 */
public abstract class QueryDefine extends Query {

  /**
   * If null then this subtypes the parent type directly (e.g. entity, relation...)
   */
  public QueryDefine subs = null;
  //Unique string used to refer to this object
  public final String identifier;

  protected QueryDefine(String identifier) {
    this.identifier = identifier;
  }
}
