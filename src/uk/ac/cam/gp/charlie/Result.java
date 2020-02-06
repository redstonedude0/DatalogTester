package uk.ac.cam.gp.charlie;

//import grakn.client.answer.ConceptMap;

import java.util.List;
import java.util.Map;

public class Result {

  /**
   * NOTE: Removed as this wasn't compiling for me, also this should be an
   * implementation-independent class IMO?
   */

//    public Result(List<List<ConceptMap>> graqlResult) {
//        // TODO: transform in a common format used to then compare graql and datalog results
//    }

  /**
   * List of possible variable mappings, only returns variables which do not refer to things,
   * for example the query match
   *  $x isa person, has name $n1;
   *  $y isa person, has name $n2;
   *  $r ($x,$y) isa friendship; get;
   * might return
   * [
   *  {
   *    "n1"=>"Bob",
   *    "n2"=>"Alice"
   *  },
   *  {
   *    "n1"=>"Alice",
   *    "n2"=>"Bob"
   *  }
   * ]
   *
   * @param results a
   */
  public Result(List<Map<String, String>> results) {
    this.results = results;
  }

  public final List<Map<String, String>> results;
}
