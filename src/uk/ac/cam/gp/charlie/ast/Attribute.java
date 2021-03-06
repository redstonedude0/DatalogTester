package uk.ac.cam.gp.charlie.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an attribute a thing (entity, relation) 'has'.
 *
 * Note: Later will need to change to DefineAttribute as something can sub attribute.
 * @author hrjh2@cam.ac.uk
 */
public class Attribute {

  public final String identifier;
  private static List<Attribute> instances = new ArrayList<>();

  private Attribute(String identifier) {
    this.identifier = identifier;
    instances.add(this);
  }

  public static Attribute fromIdentifier(String identifier) {
    for(Attribute v : instances) {
      if (v.identifier.equals(identifier)) {
        return v;
      }
    }
    return new Attribute(identifier);
  }

}
