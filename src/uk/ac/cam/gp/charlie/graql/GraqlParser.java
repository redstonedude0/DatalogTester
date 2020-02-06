package uk.ac.cam.gp.charlie.graql;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    List<Query> queryList = new ArrayList<>();
    String[] typeQuery = input.split("\n", 2);
    String[] queryBlocks = typeQuery[1].split(";");
    for(String q : queryBlocks) {
      String[] lines = q.split("\\s*,\\s*");
      String[][] words =  new String[lines.length][];
      for(int i = 0; i < lines.length; i++){
        words[i] = lines[i].split(" ");
      }
      if(typeQuery[0].equals("define")){
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
      if(typeQuery[0].equals("insert")) {
        if (words[0][1].equals("isa")) {
          //At this point we assume this to be an entity TODO:fix
          String name = words[0][0];
          String type = words[0][2];
          Variable var = Variable.fromIdentifier(name);
          QueryInsertEntity entity = new QueryInsertEntity(var, type);
          for (int i = 1; i < words.length; i++) {
            if (words[i][0].equals("has")) {
              entity.attributes.put(Attribute.fromIdentifier(words[i][1]), new AttributeValue(words[i][2]));
            }
          }
        }
        //TODO: make this less hideous and work for more than 1 case
        for(int i = 0; i < words[0].length; i++){
          if(words[0][i].startsWith("(")){
            String name = (i == 0) ? "" : words[0][i - 1].substring(1, words[0][i - 1].length());
            List<Map.Entry<Plays,Variable>> relationList = new ArrayList<>();
            Variable var = Variable.fromIdentifier(words[0][i+1].substring(1, words[0][i+1].length()));
            String role = words[0][i].substring(1, words[0][i].length() - 1);
            relationList.add((new SimpleEntry<>(Plays.fromIdentifier(role), var)));
            boolean reachedIsa = false;
            for (int j = 1; j < words.length; j++){
              Variable var2 = Variable.fromIdentifier(words[j][0].substring(0, words[j][0].length()));
              String role2 = "";
              if(words[j][1].endsWith(")")){
                role2 = words[j][1].substring(1, words[j][1].length() - 1);
                reachedIsa = true;
              }
              else {
                role2 = words[j][1].substring(1, words[j][1].length());
              }
              relationList.add((new SimpleEntry<>(Plays.fromIdentifier(role2), var2)));
              if(reachedIsa){
                QueryInsertRelation relation = new QueryInsertRelation(Variable.fromIdentifier(name),words[j][3]);
                relation.plays.addAll(relationList);
                break;
                //we need to do more stuff here
              }
            }
            break;
          }
        }
      }
      System.out.println("breakpoint");
    }
    return queryList;
  }

  public static void main(String[] args) {
    String test = "insert\n$z (employee: $x, employee: $y) isa coworkers;";
    GraqlParser.graqlToAST(test);
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
