package uk.ac.cam.gp.charlie.graql;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.AttributeValue;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.Variable;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefineEntity;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefineRelation;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsertEntity;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsertRelation;

/**
 * Parse Graql into ASTs
 */
public class GraqlParser {

  public static List<Query> graqlToAST(String input) {
    //parse input into 0 or more graql statements, and return then
    //TODO return parsed graql, for now just return test trees
    switch (input) {
      case "test_schema":
        return test_schema();
      case "test_data":
        return test_data();
      case "test_test":
        break;
    }
    return new ArrayList<>();
  }

  /**
   *
   * #############################################################
   * I suggest we remove all code below, in favour of just
   * using the code above.
   *
   *
   *
   *
   *
   */

  public static List<Query> test_data() {
    List<Query> toRet = new ArrayList<>();
    //this returns an example list of inserts
    /**
     * The below example is equivalent to the graql:
     *
     * insert $x isa person has name "Bob";
     * insert $y isa organisation has name "Uni"";
     * insert $z (employer: $y, employee: $x) isa employment;
     * insert $x isa person has name "Alice";
     * insert $z (employer: $y, employee: $x) isa employment;
     *
     * //TODO
     * match
     *  $x isa person has name "Bob";
     *  $y isa person has name "Alice";
     * insert
     *  $z (employee: $x, employee: $y) isa coworkers;
     *
     */

    Variable var_x = Variable.fromIdentifier("x");
    QueryInsertEntity bob = new QueryInsertEntity(var_x,"person");
    bob.attributes.put(Attribute.fromIdentifier("name"),new AttributeValue("Bob"));
    toRet.add(bob);

    Variable var_y = Variable.fromIdentifier("y");
    QueryInsertEntity uni = new QueryInsertEntity(var_y,"organisation");
    uni.attributes.put(Attribute.fromIdentifier("name"),new AttributeValue("Uni"));
    toRet.add(uni);

    Variable var_z = Variable.fromIdentifier("z");
    QueryInsertRelation z = new QueryInsertRelation(var_z,"employment");
    z.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employer"), var_y));
    z.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employee"), var_x));
    toRet.add(z);

    //Redefinition of var_x isn't necessary - fromIdentifier will return the same object,
    //I've just included it here for consistency, it's up to you whether you do this or not
    var_x = Variable.fromIdentifier("x");
    QueryInsertEntity alice = new QueryInsertEntity(var_x,"person");
    alice.attributes.put(Attribute.fromIdentifier("name"),new AttributeValue("Alice"));
    toRet.add(alice);

    //Example without redefinition of var and new query obj.
    z = new QueryInsertRelation(var_z,"employment");
    z.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employer"), var_y));
    z.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employee"), var_x));
    toRet.add(z);

    return toRet;
  }

  public static List<Query> test_schema() {
    List<Query> toRet = new ArrayList<>();
    //this returns an example list of defines
    /**
     * The below example is equivalent to the graql:
     *
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
     *   relates employee;
     *
     */

    Attribute nameAttribute = Attribute.fromIdentifier("name");

    Plays employee = Plays.fromIdentifier("employee");
    Plays employer = Plays.fromIdentifier("employer");

    QueryDefineEntity person = new QueryDefineEntity("person");
    person.attributes.add(nameAttribute);
    person.plays.add(employee);
    toRet.add(person);

    QueryDefineEntity organisation = new QueryDefineEntity("organisation");
    organisation.attributes.add(nameAttribute);
    organisation.plays.add(employer);
    toRet.add(organisation);

    QueryDefineRelation employment = new QueryDefineRelation("employment");
    employment.relates.add(employee);
    employment.relates.add(employer);
    toRet.add(employment);

    QueryDefineRelation coworkers = new QueryDefineRelation("coworkers");
    coworkers.relates.add(employee);
    toRet.add(coworkers);
    return toRet;
  }

}
