package uk.ac.cam.gp.charlie.ast.queries;

import java.util.ArrayList;
import java.util.List;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.Plays;

public class QueryDefineEntity extends QueryDefine {

  //List of roles this entity 'plays'
  public List<Plays> plays = new ArrayList<>();
  //List of attributes this entity 'has'
  public List<Attribute> attributes = new ArrayList<>();

  public QueryDefineEntity(String identifier) {
    super(identifier);
  }
}
