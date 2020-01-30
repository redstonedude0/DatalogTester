package uk.ac.cam.gp.charlie.datalog.interpreter;

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

  //INTO:
  //List<Definition> Schema
  //  [Definition->DefineEntity
  //    List<String> attributes
  //      ["name"]
  //    List<String> roles
  //      ["employee"]
  //    String name "person"
  //    String subs "entity"
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
