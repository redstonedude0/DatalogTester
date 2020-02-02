package uk.ac.cam.gp.charlie.ast.queries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.Variable;

public class QueryInsertRelation extends QueryInsert {

  //List of Things which may play in this relation
  public List<Entry<Plays,Variable>> plays = new ArrayList<>();

  public QueryInsertRelation(String identifier) {
    super(identifier);
  }
}
