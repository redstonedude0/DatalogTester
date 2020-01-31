package uk.ac.cam.gp.charlie.datalog.interpreter.ast;

public abstract class Define {

  public Define subs = null;
  public final String identifier;

  protected Define(String identifier) {
    this.identifier = identifier;
  }
}
