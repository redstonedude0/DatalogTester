package uk.ac.cam.gp.charlie.datalog.interpreter;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Attr;
import uk.ac.cam.gp.charlie.datalog.interpreter.ast.Attribute;
import uk.ac.cam.gp.charlie.datalog.interpreter.ast.Define;
import uk.ac.cam.gp.charlie.datalog.interpreter.ast.DefineEntity;
import uk.ac.cam.gp.charlie.datalog.interpreter.ast.DefineRelation;
import uk.ac.cam.gp.charlie.datalog.interpreter.ast.Plays;

public class Context {

  //Stores a schema context in AST form
  //TODO needs to transform
  /**
   * define
   *
   * person sub entity,
   *   has name,
   *   plays employee;
   *
   * organisation sub entity,
   *   has name,
   *   plays employer;
   *
   * employment sub relation,
   *   relates employee,
   *   relates employer;
   *
   * coworkers sub relation,
   *    relates employee;
   *
   * same-employment-are-coworkers sub rule,
   *   when {
   *     (employee: $x, $z) isa employment;
   *     (employee: $y, $z) isa employment;
   *     $x != $y;
   *   }, then {
   *     ($x,$y) isa coworkers;
   *   };
   * ###Data
   * insert $x isa person has name "Bob";
   * insert $y isa organisation has name "Uni"";
   * insert $z (employer: $y, employee: $x) isa employment;
   */

  List<Define> schema;

  public static void generateExample() {
    List<Define> schema = new ArrayList<>();

    DefineEntity person = new DefineEntity("person");
    person.attributes.add(new Attribute("name"));
    Plays employee = new Plays("employee");
    person.plays.add(employee);
    schema.add(person);

    DefineEntity organisation = new DefineEntity("organisation");
    organisation.attributes.add(new Attribute("name"));
    Plays employer = new Plays("employer");
    organisation.plays.add(employer);
    schema.add(organisation);

    DefineRelation employment = new DefineRelation("employment");
    employment.relates.add(employee);
    employment.relates.add(employer);
    schema.add(employment);

    DefineRelation coworkers = new DefineRelation("coworkers");
    coworkers.relates.add(employee);
    schema.add(coworkers);

  }

}
