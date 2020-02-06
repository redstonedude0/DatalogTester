package uk.ac.cam.gp.charlie.graql;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.AttributeValue;
import uk.ac.cam.gp.charlie.ast.ConstantValue;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.Variable;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefineEntity;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefineRelation;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsert;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsertEntity;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsertRelation;
import uk.ac.cam.gp.charlie.ast.queries.match.ConditionIsa;
import uk.ac.cam.gp.charlie.ast.queries.match.QueryMatch;

/**
 * Parse Graql into ASTs
 */
public class GraqlParser {

  public static List<Query> graqlToAST(String input) {
    //TODO for testing only, remove
    switch (input) {
      case "test_schema":
        return test_schema();
      case "test_data":
        return test_data();
    }
    //TODO********************* remove above
    //parse input into 0 or more graql statements, and return then
    List<Query> queryList = new ArrayList<>();
    String[] typeQuery = input.split("\n", 2);
    String[] queryBlocks = typeQuery[1].split(";");
    for(String q : queryBlocks) {
      String[] lines = q.split("\\s*,\\s*");
      String[][] words =  new String[lines.length][];
      for(int i = 0; i < lines.length; i++){
        words[i] = lines[i].split(" ");
      }
      if (typeQuery[0].equals("define")){
        if(words[0][2].equals("entity")){
          QueryDefineEntity entity = new QueryDefineEntity(words[0][0]);
          for(int i = 1; i < words.length; i++){
            if(words[i][0].equals("has")){
              Attribute attribute = Attribute.fromIdentifier(words[i][1]);
              entity.attributes.add(attribute);
            }
            if(words[i][0].equals("plays")){
              Plays role = Plays.fromIdentifier(words[i][1]);
              entity.plays.add(role);
            }
          }
          queryList.add(entity);
        }
        if(words[0][2].equals("relation")){
          QueryDefineRelation relation = new QueryDefineRelation(words[0][0]);
          for(int i = 1; i < words.length; i++){
            if(words[i][0].equals("relates")){
              Plays role = Plays.fromIdentifier(words[i][1]);
              relation.relates.add(role);
            }
          }
          queryList.add(relation);
        }
      }
      System.out.println("breakpoint");

    }
    return queryList;
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

  private static List<Query> test_data() {
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
     *
     *
     */

    Variable var_x = Variable.fromIdentifier("x");
    QueryInsertEntity bob = new QueryInsertEntity(var_x,"person");
    bob.attributes.put(Attribute.fromIdentifier("name"),ConstantValue.fromValue("Bob"));
    toRet.add(bob);

    Variable var_y = Variable.fromIdentifier("y");
    QueryInsertEntity uni = new QueryInsertEntity(var_y,"organisation");
    uni.attributes.put(Attribute.fromIdentifier("name"),ConstantValue.fromValue("Uni"));
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
    alice.attributes.put(Attribute.fromIdentifier("name"),ConstantValue.fromValue("Alice"));
    toRet.add(alice);

    //Example without redefinition of var and new query obj.
    z = new QueryInsertRelation(var_z,"employment");
    z.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employer"), var_y));
    z.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employee"), var_x));
    toRet.add(z);

    //Example of match
    QueryInsertRelation insertQuery = new QueryInsertRelation(var_z,"coworkers");
    insertQuery.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employee"),var_x));
    insertQuery.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employee"),var_y));
    QueryMatch match = new QueryMatch();
    match.setActionInsert(insertQuery);
    ConditionIsa cond_1 = new ConditionIsa(var_x,"person");
    cond_1.has.put(Attribute.fromIdentifier("name"),ConstantValue.fromValue("Bob"));
    ConditionIsa cond_2 = new ConditionIsa(var_y,"person");
    cond_2.has.put(Attribute.fromIdentifier("name"),ConstantValue.fromValue("Alice"));
    match.conditions.add(cond_1);
    match.conditions.add(cond_2);
    toRet.add(match);

    return toRet;
  }

  private static List<Query> test_schema() {
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
