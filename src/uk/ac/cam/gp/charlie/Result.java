package uk.ac.cam.gp.charlie;

//import grakn.client.answer.ConceptMap;

import grakn.client.answer.ConceptMap;
import grakn.client.concept.Concept;
import graql.lang.statement.Variable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Result {

  /**
   * List of possible variable mappings, only returns variables which do not refer to things, for
   * example the query match $x isa person, has name $n1; $y isa person, has name $n2; $r ($x,$y)
   * isa friendship; get; might return [ { "n1"=>"Bob", "n2"=>"Alice" }, { "n1"=>"Alice",
   * "n2"=>"Bob" },... ]
   *
   * @param results a
   */
  public Result(List<Map<String, String>> results) {
    this.results = results;
  }

  public static Result fromGrakn(List<List<ConceptMap>> graknResults) {
    List<Map<String, String>> results = new ArrayList<>();
    for (List<ConceptMap> mapList : graknResults) {
      for (ConceptMap map : mapList) {
        Map<String, String> result = new HashMap<>();
        for (Entry<Variable, Concept> entry : map.map().entrySet()) {
          result.put(entry.getKey().name(), entry.getValue().asAttribute().value().toString());
          System.out
              .println(entry.getKey().name() + " : " + entry.getValue().asAttribute().value());
        }
        results.add(result);
      }
    }
    return new Result(results);
  }

  public void print() {
    System.out.println("\u001b[35;1mRESULTS:\u001b[0m");
    if (results.size() == 0) {
      System.out.println("\u001b[30;1m  [NONE]\u001b[0m");
    } else {
      for (Map<String, String> result : results) {
        System.out.println("\u001b[35;1m  RESULT:\u001b[0m");
        for (Entry<String, String> entry : result.entrySet()) {
          System.out.println(
              "\u001b[35;1m    $" + entry.getKey() + " -> " + entry.getValue()
                  + "\u001b[0m");

        }
      }
    }
  }

  public final List<Map<String, String>> results;
}
