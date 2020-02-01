package uk.ac.cam.gp.charlie.ast;

/**
 * Represents abstract definition of a Grakn Concept Type or Grakn Rule. See subtypes for definition
 * of specific concept types.
 *
 * https://dev.grakn.ai/docs/schema/concepts#summary
 */
public abstract class Define {

  /**
   * If null then this subtypes the parent type directly (e.g. entity, relation...)
   */
  public Define subs = null;
  //Unique string used to refer to this object
  public final String identifier;

  protected Define(String identifier) {
    this.identifier = identifier;
  }
}
