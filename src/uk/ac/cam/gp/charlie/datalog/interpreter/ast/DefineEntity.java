package uk.ac.cam.gp.charlie.datalog.interpreter.ast;

import java.util.ArrayList;
import java.util.List;

public class DefineEntity extends Define {

  //List of roles this entity 'plays'
  public List<Plays> plays = new ArrayList<>();
  //List of attributes this entity 'has'
  public List<Attribute> attributes = new ArrayList<>();

  public DefineEntity(String identifier) {
    super(identifier);
  }
}
