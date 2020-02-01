package uk.ac.cam.gp.charlie.ast;

/**
 * Represents an attribute an object (entity, relation) 'has'.
 *
 * Note: Later will need to change to DefineAttribute as something can sub attribute.
 */
public class Attribute {

  public final String identifier;

  public Attribute(String identifier) {
    this.identifier = identifier;
  }

}
