package uk.ac.cam.gp.charlie.graql.parsing;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.ConstantValue;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.Variable;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefine;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsert;

/**
 * Parse Graql into ASTs
 */
public class GraqlParser {

  public List<Query> graqlToAST(String input) {
    //parse input into 0 or more graql statements, and return then
    List<Query> queryList = new ArrayList<>();
    if (input.equals("")) {
      return queryList;
    }
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

}
