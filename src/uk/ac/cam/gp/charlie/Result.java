package uk.ac.cam.gp.charlie;

//import grakn.client.answer.ConceptMap;

import grakn.client.answer.ConceptMap;
import grakn.client.concept.Concept;
import grakn.client.concept.Role;
import grakn.client.concept.Thing;
import graql.lang.statement.Variable;
import uk.ac.cam.gp.charlie.Result.ResultValue.Type;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author hrjh2@cam.ac.uk
 */
public class Result {

  public static class ResultValue {
    public enum Type {
      ATTRIBUTE, RELATION, ENTITY, RULE, ROLE, TYPE;
    }
    public Type type;
    public String value; //ATTRIBUTE, ENTITY, RULE, ROLE, TYPE, RELATION
    public Map<String,Set<String>> values;//RELATION

    public ResultValue(Type t) {
      this.type = t;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof ResultValue)) return false;
      ResultValue r2 = (ResultValue) o;
      if (type != r2.type) {
        //Different types, normally this would return false,
        //however graql uses the RELATION type whilst datalog does not, so
        if (type == Type.RELATION || r2.type == Type.RELATION) {
          if (type == Type.ENTITY || r2.type == Type.ENTITY) {
            //REL=ENTITY for now
            return true;
          }
        }
        return false;
      }
      //2 have the same time, change based on the type here
      if (type == Type.RULE) return true;//any rule is equal
      if (type == Type.ROLE) return true;//any role is equal
      if (type == Type.TYPE) return true;//any type is equal
      if (type == Type.ENTITY) return true;//any entity is equal
      if (type == Type.ATTRIBUTE) return value.equals(r2.value); //attributes equal if value equal
      if (type == Type.RELATION) return values.size() == r2.values.size(); //relations equal if relate the same nnumber of things
      return false;//unreachable
    }

    @Override
    public String toString() {
      switch (type) {
        case ATTRIBUTE:
          return "\""+value+"\"";
        case RELATION:
          return "REL "+values.size()+"";
        case ENTITY:
          return "ENT "+value+"";
        case RULE:
          return "RULE "+value+"";
        case ROLE:
          return "ROLE "+value+"";
        case TYPE:
          return "TYPE "+value+"";
      }
      return "ERR";
    }
  }

  /**
   * List of possible variable mappings, only returns variables which do not refer to things, for
   * example the query match $x isa person, has name $n1; $y isa person, has name $n2; $r ($x,$y)
   * isa friendship; get; might return [ { "n1"=>"Bob", "n2"=>"Alice" }, { "n1"=>"Alice",
   * "n2"=>"Bob" },... ]
   *
   * @param results a
   */
  public Result(List<Map<String, ResultValue>> results) {
    this(results,true);
  }

  public Result(List<Map<String, ResultValue>> results, boolean doSetify) {
    this.results = results;
    if (doSetify) {//Datalog will pre-setify results, graql won't incase it returns a non-set intentionally
      setify();//ensure set-like (should be called iff datalog result)
    }
  }

  public static Result fromGrakn(List<List<ConceptMap>> graknResults) {
    List<Map<String, ResultValue>> results = new ArrayList<>();
    for (List<ConceptMap> mapList : graknResults) {
      for (ConceptMap map : mapList) {
        Map<String, ResultValue> result = new HashMap<>();
        for (Entry<Variable, Concept> entry : map.map().entrySet()) {
          Concept concept = entry.getValue();
          ResultValue rv;
          if (concept.isAttribute()) {
            rv = new ResultValue(Type.ATTRIBUTE);
            rv.value = concept.asAttribute().value().toString();
          } else if (concept.isEntity()) {
            rv = new ResultValue(Type.ENTITY);
            rv.value = concept.asEntity().id().toString();
          } else if (concept.isRelation()) {
            rv = new ResultValue(Type.RELATION);
            Map<Role, Set<Thing>> rolePlayers = concept.asRelation().rolePlayersMap();
            Map<String, Set<String>> rolePlayers_s = new HashMap<>();
            for (Entry<Role, Set<Thing>> rolePlayer : rolePlayers.entrySet()) {
              Set<Thing> players = rolePlayer.getValue();
              Set<String> players_s = new HashSet<>();
              for (Thing player : players) {
                players_s.add(player.id().toString());
              }
              rolePlayers_s.put(rolePlayer.getKey().toString(), players_s);
            }
            rv.values = rolePlayers_s;
            rv.value = concept.asRelation().id().toString();
          } else if (concept.isRule()) {
            rv = new ResultValue(Type.RULE);
            rv.value = concept.asRule().id().toString();
          } else if (concept.isRole()) {
            rv = new ResultValue(Type.ROLE);
            rv.value = concept.asRole().id().toString();
          } else if (concept.isType()) {
            rv = new ResultValue(Type.TYPE);
            rv.value = concept.asType().id().toString();
          } else {
            System.out.println("Unknown concept " + concept.id().getValue());
            System.out.println("isThing " + concept.isThing());
            throw new RuntimeException("Unexpected concept");
          }
          result.put(entry.getKey().name(), rv);
        }
        results.add(result);
      }
    }
    return new Result(results,false);
  }

  public void print() {
    System.out.println("\u001b[35;1mRESULT:\u001b[0m");
    if (results.size() == 0) {
      System.out.println("\u001b[30;1m  [NONE]\u001b[0m");
    } else {
      for (Map<String, ResultValue> result : results) {
        System.out.println("\u001b[35;1m  RESULT:\u001b[0m");
        for (Entry<String, ResultValue> entry : result.entrySet()) {
          System.out.println(
              "\u001b[35;1m    $" + entry.getKey() + " -> " + entry.getValue()
                  + "\u001b[0m");

        }
      }
    }
  }

  public List<Map<String, ResultValue>> results;

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Result)) return false;
    Result r2 = (Result) other;
    if (results.size() != r2.results.size()) return false;

    ConcurrentLinkedQueue<Map<String, ResultValue>> res1s = new ConcurrentLinkedQueue<>(results);
    ConcurrentLinkedQueue<Map<String, ResultValue>> res2s = new ConcurrentLinkedQueue<>(r2.results);
    loop: for (Map<String,ResultValue> res2: res2s) {
      for (Map<String,ResultValue> res1: res1s) {
        //if map equal
        boolean mapEqual = resultMapsEqual(res2, res1,false);
        if (mapEqual) {
          res1s.remove(res1);
          res2s.remove(res2);
          continue loop;
        }
      }
    }
    if (res1s.size() != 0 || res2s.size() != 0) {
      return false;
    }
    return true;
  }

  private static boolean resultMapsEqual(Map<String, ResultValue> result1,
      Map<String, ResultValue> result2, boolean strong) {
    //USE strong comparison if from the same engine do want to test values
    boolean mapEqual = false;
    if (result2.size() == result1.size()) {
      boolean allEntriesEqual = true;
      for (Entry<String,ResultValue> res1Entry : result2.entrySet()) {
        String key = res1Entry.getKey();
        ResultValue value = res1Entry.getValue();
        ResultValue value2 = result1.get(key);
        if (value == null && value2 == null) {
          continue;//Equal entry
        }
        if (value != null && value.equals(value2)) {
          if (strong) {
            if (!Objects.equals(value.value,value2.value)) {
              allEntriesEqual = false;
              break;
            }
            if (!Objects.equals(value.values,value2.values)) {
              allEntriesEqual = false;
              break;
            }
          }
          continue;//Equal entry
        }
        //Not equal, out
        allEntriesEqual = false;
        break;
      }
      if (allEntriesEqual) {
        mapEqual = true;
      }
    }
    return mapEqual;
  }

  /**
   * Turn into a set-like structure (so there's no duplicate results)
   */
  public void setify() {
    List<Map<String, ResultValue>> newResults = new ArrayList<>();
    for (Map<String,ResultValue> result: results) {
      //check if map in newResults
      boolean alreadyInNewResults = false;
      for (Map<String,ResultValue> newResult: newResults) {
        //if map equal
        boolean mapEqual = resultMapsEqual(newResult, result,true);//Need strong matching here
        if (mapEqual) {
          alreadyInNewResults = true;
          break;//no point checking rest of map
        }
      }
      //if not in, add
      if (!alreadyInNewResults) {
        newResults.add(result);
      }
    }
    this.results = newResults;
  }
}
