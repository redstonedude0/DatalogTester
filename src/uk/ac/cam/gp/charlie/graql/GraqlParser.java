package uk.ac.cam.gp.charlie.graql;

import java.util.ArrayList;
import java.util.List;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefine;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefineEntity;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefineRelation;

/**
 * Parse Graql into ASTs
 */
public class GraqlParser {

  public static List<Query> graqlToAST(String input) {
    //parse input into 0 or more graql statements, and return then
    //TODO
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


  /**
   * Convert a test environment schema (written in graql) into a list of ASTs
   *
   * @param schema the graql schema to convert
   * @return A list of ASTs representing the schema
   */
  public static List<QueryDefine> schemaToAST(String schema) {
    List<QueryDefine> toRet = new ArrayList<>();
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

    Attribute nameAttribute = new Attribute("name");

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

  /**
   * Convert a test environment data (written in graql) into a list of ASTs
   *
   * @param data the graql data to convert
   * @return A list of asts representing the schema
   *
   * TODO: note - should not return List<String>, that is a placeholder. Please return a list of
   * ASTs
   */
  public static List<String> dataToAST(String data) {
    List<String> toRet = new ArrayList<>();
    //Should convert the data to an AST, for now it just returns an example set of data

    /**
     * The following is an example of the graql:
     *
     * insert $x isa person has name "Bob";
     * insert $y isa organisation has name "Uni"";
     * insert $z (employer: $y, employee: $x) isa employment;
     */

    /**
     * TODO - not sure how best to implement that yet. Perhaps;
     * List<Insertable>[
     *  Insert implements Insertable:
     *    ...../TODO
     *
     *
     * ]
     *
     */

    return toRet;
  }

}
