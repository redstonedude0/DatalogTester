package uk.ac.cam.gp.charlie.graql;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Attr;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.ConstantValue;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.Variable;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefine;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsert;
import uk.ac.cam.gp.charlie.ast.queries.match.ConditionIsa;
import uk.ac.cam.gp.charlie.ast.queries.match.QueryMatch;

/**
 * Parse Graql into ASTs
 */
public class GraqlParser {

  public static List<Query> graqlToAST(String input) {
    //TODO for now leave this in so it's easier to test AST->datalog directly
    switch (input) {
      case "test_schema":
        return test_schema();
      case "test_data":
        return test_data();
    }
    //TODO remove above once we've finished AST stuff

    //parse input into 0 or more graql statements, and return then
    List<Query> queryList = new ArrayList<>();
    String[] typeQuery = input.split("\n", 2);
    String[] queryBlocks = typeQuery[1].split(";");
    for (String q : queryBlocks) {
      String[] lines = q.split("\\s*,\\s*");
      String[][] words = new String[lines.length][];
      for (int i = 0; i < lines.length; i++) {
        words[i] = lines[i].split(" ");
      }
      if (typeQuery[0].equals("define")) {
        //TODO I changed how this works so that subbing works properly and we don't need to know if it's an entity or a relationship, please check this
        QueryDefine define = new QueryDefine(words[0][0],QueryDefine.getFromIdentifier(words[0][2]));
        for (int i = 1; i < words.length; i++) {
          if (words[i][0].equals("has")) {
            Attribute attribute = Attribute.fromIdentifier(words[i][1]);
            define.attributes.add(attribute);
          }
          if (words[i][0].equals("plays")) {
            Plays role = Plays.fromIdentifier(words[i][1]);
            define.plays.add(role);
          }
        }
        for (int i = 1; i < words.length; i++) {
          if (words[i][0].equals("relates")) {
            Plays role = Plays.fromIdentifier(words[i][1]);
            define.relates.add(role);
          }
        }
        queryList.add(define);
      }
      if (typeQuery[0].equals("insert")) {
        if (words[0][1].equals("isa")) {
          //At this point we assume this to be an entity TODO:fix
          //NOTE: We now just have QueryInsert.
          String name = words[0][0];
          String type = words[0][2];
          Variable var = Variable.fromIdentifier(name);
          QueryInsert entity = new QueryInsert(var, type);
          for (int i = 1; i < words.length; i++) {
            if (words[i][0].equals("has")) {
              entity.attributes
                  .put(Attribute.fromIdentifier(words[i][1]), ConstantValue.fromValue(words[i][2]));
            }
          }
        }
        //TODO: make this less hideous and work for more than 1 case
        for (int i = 0; i < words[0].length; i++) {
          if (words[0][i].startsWith("(")) {
            String name = (i == 0) ? "" : words[0][i - 1].substring(1, words[0][i - 1].length());
            List<Map.Entry<Plays, Variable>> relationList = new ArrayList<>();
            Variable var = Variable
                .fromIdentifier(words[0][i + 1].substring(1, words[0][i + 1].length()));
            String role = words[0][i].substring(1, words[0][i].length() - 1);
            relationList.add((new SimpleEntry<>(Plays.fromIdentifier(role), var)));
            boolean reachedIsa = false;
            for (int j = 1; j < words.length; j++) {
              Variable var2 = Variable
                  .fromIdentifier(words[j][0].substring(0, words[j][0].length()));
              String role2 = "";
              if (words[j][1].endsWith(")")) {
                role2 = words[j][1].substring(1, words[j][1].length() - 1);
                reachedIsa = true;
              } else {
                role2 = words[j][1].substring(1, words[j][1].length());
              }
              relationList.add((new SimpleEntry<>(Plays.fromIdentifier(role2), var2)));
              if (reachedIsa) {
                QueryInsert relation = new QueryInsert(
                    Variable.fromIdentifier(name), words[j][3]);
                relation.plays.addAll(relationList);
                break;
                //we need to do more stuff here
              }
            }
            break;
          }
        }
      }
//      System.out.println("breakpoint");
    }
    return queryList;
  }

  public static void main(String[] args) {
    String test = "insert\n$z (employee: $x, employee: $y) isa coworkers;";
    GraqlParser.graqlToAST(test);
  }

  /**
   * ############################################################# I suggest we remove all code
   * below, in favour of just using the code above.
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
     * match
     *  $x isa person has name "Bob";
     *  $y isa person has name "Alice";
     * insert
     *  $z (employee: $x, employee: $y) isa coworkers;
     *
     * //TODO
     * match
     *  $x isa person has name $n;
     * get $n;
     *
     */

    Variable var_y = Variable.fromIdentifier("y");
    QueryInsert uni = new QueryInsert(var_y, "organisation");
    uni.attributes.put(Attribute.fromIdentifier("name"), ConstantValue.fromValue("Uni"));
    toRet.add(uni);

    Variable var_x = Variable.fromIdentifier("x");
    QueryInsert bob = new QueryInsert(var_x, "person");
    bob.attributes.put(Attribute.fromIdentifier("name"), ConstantValue.fromValue("Bob"));
    toRet.add(bob);

    Variable var_z = Variable.fromIdentifier("z");
    QueryInsert z = new QueryInsert(var_z, "employment");
    z.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employer"), var_y));
    z.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employee"), var_x));
    toRet.add(z);

    //Redefinition of var_x isn't necessary - fromIdentifier will return the same object,
    //I've just included it here for consistency, it's up to you whether you do this or not
    var_x = Variable.fromIdentifier("x");
    QueryInsert alice = new QueryInsert(var_x, "person");
    alice.attributes.put(Attribute.fromIdentifier("name"), ConstantValue.fromValue("Alice"));
    toRet.add(alice);

    //Example without redefinition of var and new query obj.
    z = new QueryInsert(var_z, "employment");
    z.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employer"), var_y));
    z.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employee"), var_x));
    toRet.add(z);

    QueryInsert person = new QueryInsert(var_x, "person");
    person.attributes.put(Attribute.fromIdentifier("name"), ConstantValue.fromValue("Charlie"));
    toRet.add(person);
    person = new QueryInsert(var_x, "person");
    person.attributes.put(Attribute.fromIdentifier("name"), ConstantValue.fromValue("Dhruv"));
    toRet.add(person);
    person = new QueryInsert(var_x, "person");
    person.attributes.put(Attribute.fromIdentifier("name"), ConstantValue.fromValue("Ellie"));
    toRet.add(person);


    //Examples of match
    QueryInsert insertQuery = new QueryInsert(var_z, "friendship");
    insertQuery.plays.add(new SimpleEntry<>(Plays.fromIdentifier("friend"), var_x));
    insertQuery.plays.add(new SimpleEntry<>(Plays.fromIdentifier("friend"), var_y));
    QueryMatch match = new QueryMatch();
    match.setActionInsert(insertQuery);
    ConditionIsa cond_1 = new ConditionIsa(var_x, "person");
    cond_1.has.put(Attribute.fromIdentifier("name"), ConstantValue.fromValue("Bob"));
    ConditionIsa cond_2 = new ConditionIsa(var_y, "person");
    cond_2.has.put(Attribute.fromIdentifier("name"), ConstantValue.fromValue("Alice"));
    match.conditions.add(cond_1);
    match.conditions.add(cond_2);
    toRet.add(match);

    //Examples of match
    insertQuery = new QueryInsert(var_z, "coworkers");
    insertQuery.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employee"), var_x));
    insertQuery.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employee"), var_y));
    match = new QueryMatch();
    match.setActionInsert(insertQuery);
    cond_1 = new ConditionIsa(var_x, "person");
    cond_2 = new ConditionIsa(var_y, "person");
    match.conditions.add(cond_1);
    match.conditions.add(cond_2);
    toRet.add(match);

    //match get
    Variable var_n = Variable.fromIdentifier("n");
    QueryMatch matchget = new QueryMatch();
    matchget.setActionGet(List.of(var_n));
    cond_1 = new ConditionIsa(var_x,"person");
    cond_1.has.put(Attribute.fromIdentifier("name"),var_n);
    matchget.conditions.add(cond_1);
    toRet.add(matchget);

    //match delete  match $x isa person, has name "Alice"; $y (employee:$x) isa employment; delete $y;
    QueryMatch matchdel = new QueryMatch();
    matchdel.setActionDelete(var_y);
    cond_1 = new ConditionIsa(var_x, "person");
    cond_1.has.put(Attribute.fromIdentifier("name"),ConstantValue.fromValue("Alice"));
    cond_2 = new ConditionIsa(var_y,"employment");
    cond_2.relates.add(new SimpleEntry<>(Plays.fromIdentifier("employee"),var_x));
    matchdel.conditions.add(cond_1);
    matchdel.conditions.add(cond_2);
    //toRet.add(matchdel);


    //match delete  match $x isa person, has name "Alice"; delete $x;
    matchdel = new QueryMatch();
    matchdel.setActionDelete(var_x);
    cond_1 = new ConditionIsa(var_x, "person");
    cond_1.has.put(Attribute.fromIdentifier("name"),ConstantValue.fromValue("Alice"));
    matchdel.conditions.add(cond_1);
    toRet.add(matchdel);


    //match get again
    toRet.add(matchget);

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
    Plays friend = Plays.fromIdentifier("friend");

    QueryDefine person = new QueryDefine("person",QueryDefine.getFromIdentifier("entity"));
    person.attributes.add(nameAttribute);
    person.plays.add(employee);
    person.plays.add(friend);
    toRet.add(person);

    QueryDefine organisation = new QueryDefine("organisation",QueryDefine.getFromIdentifier("entity"));
    organisation.attributes.add(nameAttribute);
    organisation.plays.add(employer);
    toRet.add(organisation);

    QueryDefine employment = new QueryDefine("employment",QueryDefine.getFromIdentifier("relation"));
    employment.relates.add(employee);
    employment.relates.add(employer);
    toRet.add(employment);

    QueryDefine coworkers = new QueryDefine("coworkers",QueryDefine.getFromIdentifier("relation"));
    coworkers.relates.add(employee);
    toRet.add(coworkers);

    QueryDefine friendship = new QueryDefine("friendship",QueryDefine.getFromIdentifier("relation"));
    friendship.relates.add(friend);
    toRet.add(friendship);
    return toRet;
  }

}
