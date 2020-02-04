package uk.ac.cam.gp.charlie.ast.queries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.AttributeValue;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.Variable;

public class QueryInsertEntity extends QueryInsert {

  //List of attributes this entity 'has'
  public Map<Attribute, AttributeValue> attributes = new HashMap<>();

  public QueryInsertEntity(Variable returnVariable, String type) {
    super(returnVariable,type);
  }
}
