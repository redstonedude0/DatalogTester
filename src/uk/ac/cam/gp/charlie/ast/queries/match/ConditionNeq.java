package uk.ac.cam.gp.charlie.ast.queries.match;

import uk.ac.cam.gp.charlie.ast.AttributeValue;

public class ConditionNeq extends MatchCondition {

  public AttributeValue lhs;
  public AttributeValue rhs;

  public ConditionNeq(AttributeValue lhs,AttributeValue rhs) {
    super(null);
    this.lhs = lhs;
    this.rhs = rhs;
  }
}
