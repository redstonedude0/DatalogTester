package uk.ac.cam.gp.charlie.datalog.interpreter.ast;

import java.util.ArrayList;
import java.util.List;

public class DefineRelation extends Define {

  //List of play relations this relationship 'relates'
  public List<Plays> relates = new ArrayList<>();

  public DefineRelation(String identifier) {
    super(identifier);
  }
}
