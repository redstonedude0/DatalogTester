package uk.ac.cam.gp.charlie.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a 'plays' relationship connection.
 * @author hrjh2@cam.ac.uk
 */
public class Plays {

  public final String identifier;
  private static List<Plays> instances = new ArrayList<>();

  private Plays(String identifier) {
    this.identifier = identifier;
    instances.add(this);
  }

  public static Plays fromIdentifier(String identifier) {
    for(Plays v : instances) {
      if (v.identifier.equals(identifier)) {
        return v;
      }
    }
    return new Plays(identifier);
  }

}
