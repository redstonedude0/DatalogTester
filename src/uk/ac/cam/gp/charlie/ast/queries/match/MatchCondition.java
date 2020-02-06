package uk.ac.cam.gp.charlie.ast.queries.match;

import uk.ac.cam.gp.charlie.ast.Variable;

/**
 * Represents each condition in the match. Examples:
 *
 * - $x isa person has name "Bob";
 * - $y isa person has name "Alice";
 * - $emp (employer: $x, employee: $y) isa employment;
 * - $emp (employer: $x, employee: $y) isa employment, has reference-id $ref;
 * - $fr ($x, $y) isa friendship;
 */
public abstract class MatchCondition {

  public Variable returnVariable;

  public MatchCondition(Variable returnVariable) {
    this.returnVariable = returnVariable;
  }

}
