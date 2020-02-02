package uk.ac.cam.gp.charlie.ast.queries;

import java.util.ArrayList;
import java.util.List;
import uk.ac.cam.gp.charlie.ast.Plays;

public class QueryDefineRelation extends QueryDefine {

  //List of play relations this relationship 'relates'
  public List<Plays> relates = new ArrayList<>();

  public QueryDefineRelation(String identifier) {
    super(identifier);
  }
}
