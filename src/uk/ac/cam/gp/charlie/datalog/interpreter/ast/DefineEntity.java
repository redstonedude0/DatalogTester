package uk.ac.cam.gp.charlie.datalog.interpreter.ast;

import java.util.ArrayList;
import java.util.List;

public class DefineEntity extends Define {

  public List<Plays> plays = new ArrayList<>();
  public List<Attribute> attributes = new ArrayList<>();

}
