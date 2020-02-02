package uk.ac.cam.gp.charlie.ast.queries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.AttributeValue;
import uk.ac.cam.gp.charlie.ast.Plays;

public class QueryInsertEntity extends QueryInsert {

  //List of attributes this entity 'has'
  public Map<Attribute, AttributeValue> attributes = new HashMap<>();

  public QueryInsertEntity(String identifier) {
    super(identifier);
  }
}
