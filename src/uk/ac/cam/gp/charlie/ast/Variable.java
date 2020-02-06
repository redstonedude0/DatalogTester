package uk.ac.cam.gp.charlie.ast;

import java.util.ArrayList;
import java.util.List;

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

  public String getIdentifier() {
    return identifier;
  }

}
