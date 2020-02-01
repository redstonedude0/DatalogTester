package uk.ac.cam.gp.charlie.datalog.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.Define;
import uk.ac.cam.gp.charlie.ast.DefineEntity;
import uk.ac.cam.gp.charlie.ast.DefineRelation;
import uk.ac.cam.gp.charlie.ast.Plays;

/**
 * Represents a schema and data (test environment) in AST form
 */
public class Context {

  public Context(List<Define> schema, List<String> data) {
    this.schema = schema;
    this.data = data;
  }

  /**
   * The Schema this Context represents
   */
  public List<Define> schema;
  /**
   * The Data this context represents TODO: Define an AST for data (e.g. need 'Insert' AST node?,
   * then change the below definition accordingly (don't use String)
   */
  public List<String> data;
  //TODO: Remove, this is just for testing so I can pass datalog into the interpreter directly
  public String TEST_REMOVE = "";

  /**
   * TODO:Remove, or make into a unit test
   *
   * This method should generate an example Context. This is to allow us to test Context->Datalog
   * conversion without having to finish coding Graql->Context conversion.
   * @return the example context
   */
  public static Context generateExample() {
    List<Define> schema = new ArrayList<>();

    Attribute nameAttribute = new Attribute("name");

    Plays employee = new Plays("employee");
    Plays employer = new Plays("employer");

    DefineEntity person = new DefineEntity("person");
    person.attributes.add(nameAttribute);
    person.plays.add(employee);
    schema.add(person);

    DefineEntity organisation = new DefineEntity("organisation");
    organisation.attributes.add(nameAttribute);
    organisation.plays.add(employer);
    schema.add(organisation);

    DefineRelation employment = new DefineRelation("employment");
    employment.relates.add(employee);
    employment.relates.add(employer);
    schema.add(employment);

    DefineRelation coworkers = new DefineRelation("coworkers");
    coworkers.relates.add(employee);
    schema.add(coworkers);

    Context toRet = new Context(schema, new ArrayList<>());

    return toRet;
  }

  /*********************************************************
   * Below is private data used by the datalog engine when converting the context.
   * This maps datalog -> ast syntax
   *********************************************************/

  Map<Integer,Define> typeDefinitions = new HashMap<>();
  int typeNumber = 0;

  private int attributeNumber = 0;
  private Map<Integer,Attribute> attributeDefinitions = new HashMap<>();
  int getAttributeNumber(Attribute attribute) {
    for (Entry<Integer,Attribute> entry : attributeDefinitions.entrySet()) {
      if (entry.getValue().identifier.equals(attribute.identifier)) {
        return entry.getKey();
      }
    }
    attributeDefinitions.put(attributeNumber,attribute);
    return attributeNumber++;
  }

  private int playsNumber = 0;
  private Map<Integer,Plays> playsDefinitions = new HashMap<>();
  int getPlaysNumber(Plays plays) {
    for (Entry<Integer,Plays> entry : playsDefinitions.entrySet()) {
      if (entry.getValue().identifier.equals(plays.identifier)) {
        return entry.getKey();
      }
    }
    playsDefinitions.put(playsNumber,plays);
    return playsNumber++;
  }


}
