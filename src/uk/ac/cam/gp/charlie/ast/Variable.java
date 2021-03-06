package uk.ac.cam.gp.charlie.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a variable
 * @author hrjh2@cam.ac.uk
 */
public class Variable extends AttributeValue{

  private final String identifier;
  private static List<Variable> instances = new ArrayList<>();

  private Variable(String identifier) {
    this.identifier = identifier;
    instances.add(this);
  }

  public static Variable fromIdentifier(String identifier) {
    for(Variable v : instances) {
      if (v.identifier.equals(identifier)) {
        return v;
      }
    }
    return new Variable(identifier);
  }

  public static String getIdentifier(Variable v) {
    if (v == null) {
      return "_";
    }
    return v.identifier;
  }

}
