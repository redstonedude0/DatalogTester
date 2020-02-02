package uk.ac.cam.gp.charlie.graql;

import java.util.ArrayList;
import java.util.List;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefineEntity;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefineRelation;

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
        break;
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

  public static List<Query> test_schema() {
    List<Query> toRet = new ArrayList<>();
    //this should parse the schema into a list of defines, for now it just returns an example schema
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

    Plays employee = new Plays("employee");
    Plays employer = new Plays("employer");

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
