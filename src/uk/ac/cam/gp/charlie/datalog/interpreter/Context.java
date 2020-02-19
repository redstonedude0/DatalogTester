package uk.ac.cam.gp.charlie.datalog.interpreter;

import abcdatalog.ast.Clause;
import abcdatalog.ast.PositiveAtom;
import abcdatalog.parser.DatalogParseException;
import abcdatalog.parser.DatalogParser;
import abcdatalog.parser.DatalogTokenizer;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.ConstantValue;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.Variable;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsert;

/**
 * Represents a schema and data (test environment) in AST form
 */
public class Context {

  public Context() {
    //Prepop datalog
    String precode = "instanceof(Concept,Supertype) :- instanceof(Concept,Subtype), t_subs(Subtype,Supertype)."; //subtyping relation
    precode += "t_subs(entity,concept).";
    precode += "t_subs(relation,concept).";
    precode += "t_subs(rule,concept)."; //???
    try {
      datalog.addAll(DatalogParser.parseProgram(new DatalogTokenizer(new StringReader(precode))));
    } catch (DatalogParseException e) {
      e.printStackTrace();
      throw new RuntimeException("Error initialising basic datalog");
    }
  }

  /**
   * Current state of the datalog, in clause set form
   */
  public Set<Clause> datalog = new HashSet<>(); //current datalog clause set.

  /*********************************************************
   * Below is private data used by the datalog engine when converting ast->datalog
   * this tracks various IDs, etc
   *********************************************************/

//  int typeNumber = 0;
  private int typeNumber = 0;
  private Map<Integer, String> typeDefinitions = new HashMap<>();
  int getTypeNumber(String identifier) {
    for (Entry<Integer,String> entry : typeDefinitions.entrySet()) {
      if (entry.getValue().equals(identifier)) {
        return entry.getKey();
      }
    }
    typeDefinitions.put(typeNumber,identifier);
    return typeNumber++;
  }

  private int attributeNumber = 0;
  private Map<Integer,Attribute> attributeDefinitions = new HashMap<>();
  int getAttributeNumber(Attribute attribute) {
    for (Entry<Integer,Attribute> entry : attributeDefinitions.entrySet()) {
      if (entry.getValue().identifier.equals(attribute.identifier)) {
        return entry.getKey();
      }
    }
    attributeDefinitions.put(attributeNumber,attribute);
    return attributeNumber++;
  }

  private int playsNumber = 0;
  private Map<Integer,Plays> playsDefinitions = new HashMap<>();
  int getPlaysNumber(Plays plays) {
    for (Entry<Integer,Plays> entry : playsDefinitions.entrySet()) {
      if (entry.getValue().identifier.equals(plays.identifier)) {
        return entry.getKey();
      }
    }
    playsDefinitions.put(playsNumber,plays);
    return playsNumber++;
  }

  private int instanceNumber = 0;
  int getInstanceNumber() {
    return instanceNumber++;
  }
  int getMaxInstanceNumber() { return instanceNumber-1;}
  private Map<Integer,Set<Clause>> instanceClauseBindings = new HashMap<>();
  private Map<Integer,Set<Integer>> instanceInstanceBindings = new HashMap<>();
  public void bindInstanceClauses(Integer iNum, Set<Clause> clauses) {
    instanceClauseBindings.put(iNum,clauses);
  }
  public void bindInstanceToInstance(Integer ifDeleted, Integer thenDelete) {
    if (!instanceInstanceBindings.keySet().contains(ifDeleted)) {
      instanceInstanceBindings.put(ifDeleted,new HashSet<>());
    }
    instanceInstanceBindings.get(ifDeleted).add(thenDelete);
  }
  private Set<Clause> getInstanceBoundClauses(Integer i) {
    return instanceClauseBindings.get(i);
  }
  private Set<Integer> getInstanceBoundInstances(Integer i) {
    if (instanceInstanceBindings.keySet().contains(i)) {
      return instanceInstanceBindings.get(i);
    } else {
      return new HashSet<>();
    }
  }

  /**
   * Unsafe if cyclical
   * @param i
   * @return
   */
  public Set<Clause> getInstanceBoundClauses_TRANSITIVE(Integer i) {
    Set<Clause> toRet = new HashSet<>(instanceClauseBindings.get(i));
    for (Integer j : getInstanceBoundInstances(i)) {
      toRet.addAll(getInstanceBoundClauses_TRANSITIVE(j));
    }
    return toRet;
  }

  private int constNumber = 0;
  private Map<Integer, ConstantValue> constDefinitions = new HashMap<>();
  int getConstNumber(ConstantValue constant) {
    for (Entry<Integer,ConstantValue> entry : constDefinitions.entrySet()) {
      if (entry.getValue().value.equals(constant.value)) {
        return entry.getKey();
      }
    }
    constDefinitions.put(constNumber,constant);
    return constNumber++;
  }
  public ConstantValue getConstantFromID(Integer i) {
    return constDefinitions.get(i);
  }

  private int variableNumber = 0;//NOTE: Reset after each engine execution (should be)
  private Map<Integer,Variable> variableDefinitions = new HashMap<>();
  int getVariableNumber(Variable v) {
    if (v == null) {
      return variableNumber++;
    }
    for (Entry<Integer,Variable> entry : variableDefinitions.entrySet()) {
      if (Objects.equals(entry.getValue(),v)) {
        return entry.getKey();
      }
    }
    variableDefinitions.put(variableNumber,v);
    return variableNumber++;
  }
  public Variable getVariableByNumber(Integer i) {
    return variableDefinitions.get(i);
  }
  public void resetVariableNumber() {variableNumber = 0;variableDefinitions.clear();}
  public int getMaxVariableNumber() {return variableNumber-1;}

  //TODO only maps to things for now, need to map to constants, types, etc
  private Map<Variable,Integer> scope = new HashMap<>();
  public Integer resolveScope(Variable v) {
    return scope.get(v);
  }
  public void addToScope(Variable v, Integer i) {
    scope.put(v,i);
  }
  public void removeFromScope(Variable v) {scope.remove(v);}

  //<editor-fold desc="Rules (invariants)">
  //Keeps track of rules, and what they've done
  public Map<Integer,Map<String,Variable>> invariantVariableMappings = new HashMap<>();
  public Map<Integer, QueryInsert> invariantInsertQueries = new HashMap<>();
  private Map<String,Integer> invariantIdentifiers = new HashMap<>();
  private Map<Integer, PositiveAtom> invariantCheckers = new HashMap<>();
  public PositiveAtom getInvariantChecker(Integer i) {
    return invariantCheckers.get(i);
  }
  public void setInvariantChecker(Integer i, PositiveAtom checker) {
    invariantCheckers.put(i,checker);
  }
  public String getInvariantName(Integer i) {
    for (Entry<String,Integer> entry : invariantIdentifiers.entrySet()) {
      if (entry.getValue() == i) {
        return entry.getKey();
      }
    }
    throw new RuntimeException("No Invariant Name Found");
  }
  private int invariantNumber = 0;//NOTE: Reset after each engine execution (should be)
  int getInvariantNumber(String identifier) {
    if (invariantIdentifiers.containsKey(identifier)) {
      return invariantIdentifiers.get(identifier);
    }
    invariantIdentifiers.put(identifier,invariantNumber);
    return invariantNumber++;
  }
  public int getMaxInvariantNumber() {return invariantNumber-1;}
  //</editor-fold>

  public String prettifyDatalog(String datalog) {
    for (Integer i : constDefinitions.keySet()) {
      datalog = datalog.replaceAll("const_"+i,"\u001b[33m"+constDefinitions.get(i).value+"\u001b[0m");
    }
    for (int i = instanceNumber-1; i >= 0; i--) {
      datalog = datalog.replaceAll("e_"+i,"\u001b[35m{"+i+"}\u001b[0m");
    }
    for (Integer i : playsDefinitions.keySet()) {
      datalog = datalog.replaceAll("r_"+i,"\u001b[32m"+playsDefinitions.get(i).identifier+"\u001b[0m");
    }
    for (Integer i : attributeDefinitions.keySet()) {
      datalog = datalog.replaceAll("a_"+i,"\u001b[34m"+attributeDefinitions.get(i).identifier+"\u001b[0m");
    }
    for (Integer i : typeDefinitions.keySet()) {
      datalog = datalog.replaceAll("t_"+i,"\u001b[31m"+typeDefinitions.get(i)+"\u001b[0m");
    }
    //Black: \u001b[30m
    //Red: \u001b[31m (type)
    //Green: \u001b[32m (plays)
    //Yellow: \u001b[33m (constants)
    //Blue: \u001b[34m (attribute)
    //Magenta: \u001b[35m (things (instances))
    //Cyan: \u001b[36m
    //White: \u001b[37m
    //Reset: \u001b[0m

    return datalog;
  }

}
