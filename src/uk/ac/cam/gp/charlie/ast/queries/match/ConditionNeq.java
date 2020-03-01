package uk.ac.cam.gp.charlie.ast.queries.match;

import uk.ac.cam.gp.charlie.ast.AttributeValue;

/**
 * Represents an {@link MatchCondition} stating that two things are not equal.
 * LHS and RHS represent variables or constants
 * @author hrjh2@cam.ac.uk
 */
public class ConditionNeq extends MatchCondition {

  public AttributeValue lhs;
  public AttributeValue rhs;

  public ConditionNeq(AttributeValue lhs,AttributeValue rhs) {
    super(null);
    this.lhs = lhs;
    this.rhs = rhs;
  }
}
