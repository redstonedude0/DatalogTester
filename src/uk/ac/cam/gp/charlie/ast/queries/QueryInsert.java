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
 * Represents an insert statement
 *
 */
public class QueryInsert extends Query {

  //type of thing being inserted
  public String isa;
  //Variable to bind this result to (or null)
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
