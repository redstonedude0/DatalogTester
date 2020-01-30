package uk.ac.cam.gp.charlie.datalog.interpreter;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Attr;
import uk.ac.cam.gp.charlie.datalog.interpreter.ast.Attribute;
import uk.ac.cam.gp.charlie.datalog.interpreter.ast.Define;
import uk.ac.cam.gp.charlie.datalog.interpreter.ast.DefineEntity;
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

    DefineEntity person = new DefineEntity();
    person.attributes.add(new Attribute("name"));
    Plays employee = new Plays("employee");
    person.plays.add(employee);
    schema.add(person);

    DefineEntity organisation = new DefineEntity();
    organisation.attributes.add(new Attribute("name"));
    Plays employer = new Plays("employer");
    organisation.plays.add(employer);
    schema.add(organisation);
  }

  //INTO:
  //List<Definition> Schema
  //  [Definition->DefineEntity
  //    List<String> attributes
  //      ["name"]
  //    List<String> roles
  //      ["employee"]
  //    String name "person"
  //    Define subs null <other define if didn't sub entity>
  //  [Definition->DefineEntity
  //    List<String> attributes
  //      ["name"]
  //    List<String> roles
  //      ["employer"]
  //    String name "organisation"
  //    String subs "entity"
  //   [Definition->DefineRelation
  //    List<String> relates [employee, employer]
  //    String name "employment"
  //    String subs "relation"
  //  [Definition->DefineRule
  //    String name "same-employment-are-coworkers"
  //    String subs "rule"
  //    List<Clause>
  //      [Clause->Isa
  //        String type "employment";
  //        Map<String,List<String>>
  //          employee->["x"]
  //          employer->["z"]
  //      [Clause->Isa
  //        String type "employment";
  //        Map<String,List<String>>
  //          employee->["y"]
  //          employer->["z"]
  //      [Clause->NotEqual
  //        LHS: Clause->Variable "x"
  //        RHS: Clause->Variable "y"
  //List<Insertion> Data


}
