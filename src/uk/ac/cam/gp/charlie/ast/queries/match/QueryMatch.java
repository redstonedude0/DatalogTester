package uk.ac.cam.gp.charlie.ast.queries.match;

import java.util.ArrayList;
import java.util.List;
import uk.ac.cam.gp.charlie.ast.Variable;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsert;

/**
 * Represents abstract definition of a Grakn Concept Type or Grakn Rule. See subtypes for definition
 * of specific concept types.
 *
 * https://dev.grakn.ai/docs/schema/concepts#summary
 */
public class QueryMatch extends Query {

  /**
   * If null then this subtypes the parent type directly (e.g. entity, relation...)
   */
  public List<MatchCondition> conditions = new ArrayList<>();
  //TODO store modifiers (e.g. limit, sort)

  public enum Action {
    GET,
    DELETE,
    INSERT
  }
  private Action action = null;
  private List<Variable> DATA_GET;
  private Variable DATA_DELETE;
  private QueryInsert DATA_INSERT;
  public Action getAction() {
    return action;
  }
  public List<Variable> getDATA_GET() {
    return DATA_GET;
  }
  public Variable getDATA_DELETE() {
    return DATA_DELETE;
  }
  public QueryInsert getDATA_INSERT() {
    return DATA_INSERT;
  }

  /**
   * Set this to be a match...get query
   * @param v the variables to get, if null or empty then this returns all variables in match scope.
   */
  public void setActionGet(List<Variable> v) {
    if (action != null) {
      throw new RuntimeException("Match already " + action + " cannot be set to get");
    }
    action = Action.GET;
    DATA_GET = v;
  }

  /**
   * Set this to be a match...delete query
   * @param v the variable to delete, e.g. x in "delete $x"
   */
  public void setActionDelete(Variable v) {
    if (action != null) {
      throw new RuntimeException("Match already " + action + " cannot be set to delete");
    }
    action = Action.DELETE;
    DATA_DELETE = v;
  }

  /**
   * Set this to be a match...insert query
   * @param query the query to insert, you can assume variables defined in the match... part are bound in the insertion
   */
  public void setActionInsert(QueryInsert query) {
    if (action != null) {
      throw new RuntimeException("Match already " + action + " cannot be set to insert");
    }
    action = Action.INSERT;
    DATA_INSERT = query;
  }

}
/*
Currently syntax:
match::=
match
  (<match-condition>;)+
  <match-action>

match-condition::=
  $emp (employer: $org, employee: $p) isa employment;
  $emp (employer: $x, employee: $y) isa employment;
  <rel_ent__isa_condition>

  (employer: $x, employee: $y) isa employment;
  <rel_isa_condition>

  $y isa person, has full-name $y-fn, has phone-number $y-pn;
  $p isa person, has full-name $fn;
  $p isa person, has full-name $n;
  $org isa organisation, has name "Facelook";
  $person isa person, has email "tanya.arnold@gmail.com";
  $org isa organisation, has name "Pharos";
  $p isa person, has email "raphael.santos@gmail.com";
  $p isa person;
  $p isa person, has nickname $nn, has full-name $fn;
  <isa-has-condition>

match-action::=
	get;
	<get-all>

	get $x-fn, $y-fn, $y-pn;
	<get-some>

	get; sort $fn; offset 100; limit 10;
	get; sort $fn asc;
	get; limit 1;
	(limitations, sorting, etc on get currently unimplemented)

	insert $new-employment (employer: $org, employee: $person) isa employment;
  $new-employment has reference-id "WGFTSH";
  	<insert-block>

	delete $p;
	delete $emp;
	delete $r;
	<delete>
##################################

  $emp (employer: $x, employee: $y) isa employment, has reference-id $ref;
  (relations with attributes not currently implemented)

  $fr ($x, $y) isa friendship;
  (label-less relations not currently implemented well)

  $x-fn contains "Miriam";
  $phone-number contains "+44";
  (contains)

  $t isa travel, has start-date 2013-12-22 via $r;
  (via)

  $x "like";
  $n isa nickname; $n "Mitzi";
  (variable-value not currently implemented as a condition)

  $x like "(Miriam Morton|Solomon Tran)";
  (regex like not implemented)

  $s isa school, has ranking < 100;
  $s isa school, has ranking $r; $r < 100;
  (numerical comparison not implemented)

  $p isa person, has full-name $fn; { $fn contains "Miriam"; } or { $fn contains "Solomon"; };
  (block optionals (and negations) not currently implemented)

  $rr isa! romantic-relationship;  (directly isa)
  $x sub! post;
  (direct relations not currently implementeD)

  $x id V41016;
  (ids exposed in datalog not the same as graql)

  $x sub post;
  $x type post;
  (type matching not supported currently)

  employment relates $x;
  location-of-office relates $x as located-subject;
  $x plays employee;
  $x has title;
  (whatever this is is also not supported)

<match-action>:
get; sort $fn; offset 100; limit 10;
get; sort $fn asc;
get $x-fn, $y-fn, $y-pn;
get; limit 1;
insert $new-employment (employer: $org, employee: $person) isa employment;
  $new-employment has reference-id "WGFTSH";
delete $p;
delete $emp;
delete $r;









 */