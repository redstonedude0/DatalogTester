package uk.ac.cam.gp.charlie.graql;

import java.util.ArrayList;
import java.util.List;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.Define;
import uk.ac.cam.gp.charlie.ast.DefineEntity;
import uk.ac.cam.gp.charlie.ast.DefineRelation;
import uk.ac.cam.gp.charlie.ast.Plays;

/**
 * Parse Graql into ASTs
 */
public class GraqlParser {

  /**
   * Convert a test environment schema (written in graql) into a list of ASTs
   *
   * @param schema the graql schema to convert
   * @return A list of ASTs representing the schema
   */
  public static List<Define> schemaToAST(String schema) {
    List<Define> toRet = new ArrayList<>();
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

    DefineEntity person = new DefineEntity("person");
    person.attributes.add(nameAttribute);
    person.plays.add(employee);
    toRet.add(person);

    DefineEntity organisation = new DefineEntity("organisation");
    organisation.attributes.add(nameAttribute);
    organisation.plays.add(employer);
    toRet.add(organisation);

    DefineRelation employment = new DefineRelation("employment");
    employment.relates.add(employee);
    employment.relates.add(employer);
    toRet.add(employment);

    DefineRelation coworkers = new DefineRelation("coworkers");
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
