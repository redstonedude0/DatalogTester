package uk.ac.cam.gp.charlie.datalog.interpreter;

import abcdatalog.ast.Clause;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.AttributeValue;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.Variable;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefine;

/**
 * Represents a schema and data (test environment) in AST form
 */
public class Context {

  public Context() {
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

  private int constNumber = 0;
  private Map<Integer, AttributeValue> constDefinitions = new HashMap<>();
  int getConstNumber(AttributeValue constant) {
    for (Entry<Integer,AttributeValue> entry : constDefinitions.entrySet()) {
      if (entry.getValue().value.equals(constant.value)) {
        return entry.getKey();
      }
    }
    constDefinitions.put(constNumber,constant);
    return constNumber++;
  }

  //TODO only maps to things for now, need to map to constants, types, etc
  private Map<Variable,Integer> scope = new HashMap<>();
  public Integer resolveScope(Variable v) {
    return scope.get(v);
  }
  public void addToScope(Variable v, Integer i) {
    scope.put(v,i);
  }

  public void prettifyDatalog(String datalog) {

  }

}
