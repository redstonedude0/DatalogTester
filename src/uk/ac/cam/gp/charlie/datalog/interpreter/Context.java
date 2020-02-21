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
    //Prepopulate with some datalog
    String precode = "instanceof(Concept,Supertype) :- instanceof(Concept,Subtype), t_subs(Subtype,Supertype)."; //subtyping is transitive
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
   *
   * Note that this contains a lot of private state, with public getters and setters so looks rather messy, intellij
   * editor-fold comments have been used to make this easier to navigate, if you're not using intellij you may want to
   * replace these with collapsible code blocks in your editor
   *********************************************************/

  //<editor-fold desc="Typing information">
  //each type has a unique number
  private int typeNumber = 0;
  //a map from type num->name is kept
  private Map<Integer, String> typeDefinitions = new HashMap<>();
  int getTypeNumber(String identifier) {
    for (Entry<Integer,String> entry : typeDefinitions.entrySet()) {
      if (entry.getValue().equals(identifier)) {
        return entry.getKey();
      }
    }
    typeDefinitions.put(typeNumber,identifier);
    return typeNumber++; //post-increment after the return
  }
  //</editor-fold>

  //<editor-fold desc="Attribute information">
  //Each attribute (e.g. 'name' in 'has name') has a unique id,
  //if 2 entity types both have the same attribute string, they will have the same attribute number
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
  //</editor-fold>

  //<editor-fold desc="Plays role information">
  //Each role that can be played has a unique number
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
  //</editor-fold>

  //<editor-fold desc="Instance information">
  //Each instance of a thing (entity or relation) has a unique number)
  private int instanceNumber = 0;
  int getInstanceNumber() {
    return instanceNumber++;
  }
  /**
   * Returns the last instance number assigned in the last insert query
   */
  int getMaxInstanceNumber() { return instanceNumber-1;}
  //Maps which instances are responsible for which clauses so they can be deleted properly
  private Map<Integer,Set<Clause>> instanceClauseBindings = new HashMap<>();
  //Maps which instances relate to which other instances so deletion is transitive if necessary
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
  private Set<Integer> getInstanceBoundInstances(Integer i) {
    if (instanceInstanceBindings.keySet().contains(i)) {
      return instanceInstanceBindings.get(i);
    } else {
      return new HashSet<>();
    }
  }
  /**
   * Unsafe if cyclical, gets all the clauses which need to be deleted if instance i is deleted
   * Should never be cyclical in normal use
   * @param i instance i
   * @return
   */
  public Set<Clause> getInstanceBoundClauses_TRANSITIVE(Integer i) {
    Set<Clause> toRet = new HashSet<>(instanceClauseBindings.get(i));
    for (Integer j : getInstanceBoundInstances(i)) {
      toRet.addAll(getInstanceBoundClauses_TRANSITIVE(j));
    }
    return toRet;
  }
  //</editor-fold>

  //<editor-fold desc="Constant mapping information">
  //To prevent possible parsing errors all constants are abstracted away from datalog for now
  //e.g. "$x has name 'Bob'" becomes "instanceattr($x,a_0,const_0)", if numerical comparisons
  //are added then they may bypass this in the future but for now all attributes are strings and
  //are converted to constants
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
  //</editor-fold>

  //<editor-fold desc="Unbound variable information (volatile)">
  //NOTE: Reset after each engine execution (should be)
  private int variableNumber = 0;
  //Each variable number (e.g. '0' or '1' in 'Var0, Var1,...') is mapped to a variable it binds inside that match/rule clause
  //Map goes from 0=>Var(x) for example - the variable inside has the internal identifier
  private Map<Integer,Variable> variableDefinitions = new HashMap<>();
  int getVariableNumber(Variable v) {
    if (v == null) {
      //@bug this functionality was added after initial design, without checking how this may affect the rest of the code
      //it may well present a problem in specific places
      return variableNumber++;//each null variable is assigned a unique unbound variable
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
  /**
   * Returns the last variable number assigned (1 less than the total number of free variable at this moment)
   * TODO: Replace all of this logic with a set of free variables, rather than a number
   * @return
   */
  public int getMaxVariableNumber() {return variableNumber-1;}
  //</editor-fold>

  //<editor-fold desc="Scope information (bound variables)">
  //TODO only maps to things for now, need to map to types, etc
  //Maps all bound variables (bound by previously inserted in an insert block, or matched in a match/rule)
  //@bug not entirely sure how reliable this maps to constants, relations, etc (check in ASTInterpreter for all instances of e_)
  private Map<Variable,Integer> scope = new HashMap<>();
  public Integer resolveScope(Variable v) {
    return scope.get(v);
  }
  public void addToScope(Variable v, Integer i) {
    scope.put(v,i);
  }
  public void removeFromScope(Variable v) {scope.remove(v);}
  //</editor-fold>

  //<editor-fold desc="Rules (invariants)">
  //Keeps track of rules
  //Maps a rule number, to a map of "Var0"->Variable(x) to bind into the scope on a match
  //TODO make consistent with variableDefinitions (Int->Var map for a match)
  public Map<Integer,Map<String,Variable>> invariantVariableMappings = new HashMap<>();
  //Maps a rule number to the query to run to fix its invariant
  public Map<Integer, QueryInsert> invariantInsertQueries = new HashMap<>();
  //Map the name of the rule, to its identifier
  private Map<String,Integer> invariantIdentifiers = new HashMap<>();
  //Map a rule number, to the positive atom to check which values break the invariant
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
  //The next highest rule number
  private int invariantNumber = 0;
  int getInvariantNumber(String identifier) {
    if (invariantIdentifiers.containsKey(identifier)) {
      return invariantIdentifiers.get(identifier);
    }
    invariantIdentifiers.put(identifier,invariantNumber);
    return invariantNumber++;//post increment after return
  }

  /**
   * Get the number of the last bound rule
   * @return
   */
  public int getMaxInvariantNumber() {return invariantNumber-1;}
  //</editor-fold>

  /**
   * Prettify datalog using ANSI color codes and filling in data to be more human-readable
   * @param datalog
   * @return
   */
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
    //ANSI color codes key:
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
