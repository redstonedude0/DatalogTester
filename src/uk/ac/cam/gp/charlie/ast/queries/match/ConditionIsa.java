package uk.ac.cam.gp.charlie.ast.queries.match;

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
 * Represents an {@link MatchCondition} stating that a thing
 * is of a type, has some attributes, and plays in some relations
 */
public class ConditionIsa extends MatchCondition {

  public String type;

  //Map of "has" relations
  public Map<Attribute, AttributeValue> has = new HashMap<>();
  /**
   * List of "relates" relations (may be implicit in '()' syntax), null plays implies unknown relation (will be inferred)
   */
  public List<Entry<Plays,Variable>> relates = new ArrayList<>();

  public ConditionIsa(Variable returnVariable,String type) {
    super(returnVariable);
    this.type = type;
  }
}
