package uk.ac.cam.gp.charlie;

//import grakn.client.answer.ConceptMap;

import com.google.common.collect.HashMultiset;
import grakn.client.answer.ConceptMap;
import grakn.client.concept.Concept;
import grakn.client.concept.Role;
import grakn.client.concept.Thing;
import graql.lang.statement.Variable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import uk.ac.cam.gp.charlie.Result.ResultValue.Type;

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
      if (type != r2.type) return false;
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
    this.results = results;
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
          } else if (concept.isEntity()) {
            rv = new ResultValue(Type.ENTITY);
            rv.value = concept.asEntity().id().toString();
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
    return new Result(results);
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

  public final List<Map<String, ResultValue>> results;

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Result)) return false;
    Result r2 = (Result) other;

    HashMultiset<Map<String,ResultValue>> s1 = HashMultiset.create();
    HashMultiset<Map<String,ResultValue>> s2 = HashMultiset.create();

    s1.addAll(results);
    s2.addAll(r2.results);
    return s1.equals(s2);

  }
}
