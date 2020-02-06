package uk.ac.cam.gp.charlie.ast.queries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.AttributeValue;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.Variable;

/**
 * Represents abstract definition of a Grakn Concept Type or Grakn Rule. See subtypes for definition
 * of specific concept types.
 *
 * https://dev.grakn.ai/docs/schema/concepts#summary
 */
public class QueryInsert extends Query {

  /**
   * If null then this subtypes the parent type directly (e.g. entity, relation...)
   */
  public String isa = null;
  public Variable returnVariable;
  //List of Things which may play iff this is a relation
  public List<Entry<Plays,Variable>> plays = new ArrayList<>();
  //List of attributes this entity or relation 'has'
  public Map<Attribute, AttributeValue> attributes = new HashMap<>();

  public QueryInsert(Variable returnVariable, String type) {
    this.returnVariable = returnVariable;
    isa = type;
  }
}
