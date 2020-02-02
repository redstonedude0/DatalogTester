package uk.ac.cam.gp.charlie.ast.queries;

import java.util.HashMap;
import java.util.Map;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.AttributeValue;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.things.Thing;

public class QueryInsertRelation extends QueryInsert {

  //List of Things which play in this relation
  public Map<Plays, Thing> plays = new HashMap<>();

  public QueryInsertRelation(String identifier) {
    super(identifier);
  }
}
