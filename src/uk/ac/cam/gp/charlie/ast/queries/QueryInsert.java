package uk.ac.cam.gp.charlie.ast.queries;

import uk.ac.cam.gp.charlie.ast.Variable;

/**
 * Represents abstract definition of a Grakn Concept Type or Grakn Rule. See subtypes for definition
 * of specific concept types.
 *
 * https://dev.grakn.ai/docs/schema/concepts#summary
 */
public abstract class QueryInsert extends Query {

  /**
   * If null then this subtypes the parent type directly (e.g. entity, relation...)
   */
  public String isa = null;
  public Variable returnVariable;

  protected QueryInsert(Variable returnVariable, String type) {
    this.returnVariable = returnVariable;
    isa = type;
  }
}
