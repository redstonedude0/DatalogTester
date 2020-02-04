package uk.ac.cam.gp.charlie.datalog.interpreter;

import abcdatalog.ast.Clause;
import abcdatalog.parser.DatalogParseException;
import abcdatalog.parser.DatalogParser;
import abcdatalog.parser.DatalogTokenizer;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefine;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefineEntity;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefineRelation;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsert;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsertEntity;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsertRelation;

/**
 * Interprets to and from Context (An AST representation)
 */
public class ASTInterpreter {

  /**
   * Convert a Context to datalog (to be used for initialising the environment)
   *
   * @param c The Context to convert
   * @return A string to pass into engine.init
   */
  public static Set<Clause> toDatalog(Query q, Context c) {
    StringBuilder toRet = new StringBuilder();
    if (q instanceof QueryDefine) {
      QueryDefine define = (QueryDefine) q;
      if (define instanceof QueryDefineEntity) {
        QueryDefineEntity entity = (QueryDefineEntity) define;
        Integer typeNum = c.getTypeNumber(entity.identifier);
        toRet.append(String.format("t_subs(t_%d,t_entity).\n", typeNum));
        for (Attribute attribute : entity.attributes) {
          toRet.append(String
              .format("t_hasattr(t_%d,a_%d).\n", typeNum, c.getAttributeNumber(attribute)));
        }
        for (Plays play : entity.plays) {
          toRet.append(
              String.format("t_playsrole(t_%d,r_%d).\n", typeNum, c.getPlaysNumber(play)));
        }
      } else if (define instanceof QueryDefineRelation) {
        QueryDefineRelation entity = (QueryDefineRelation) define;
        Integer typeNum = c.getTypeNumber(entity.identifier);
        toRet.append(String.format("t_subs(t_%d,t_relation).\n", typeNum));
        for (Plays play : entity.relates) {
          toRet.append(String
              .format("t_relates(t_%d,r_%d).\n", typeNum, c.getPlaysNumber(play)));
        }
      } else {
        throw new RuntimeException("unknown define type");
      }
      toRet.append("\n");
    } else if (q instanceof QueryInsert) {
      QueryInsert insert = (QueryInsert) q;
      if (insert instanceof QueryInsertEntity) {
        QueryInsertEntity entity = (QueryInsertEntity) insert;
        Integer iNum = c.getInstanceNumber();
        toRet.append(String.format("instanceof(e_%d,t_%s).\n",iNum,c.getTypeNumber(entity.isa)));
      } else if (insert instanceof QueryInsertRelation) {

      } else {
        throw new RuntimeException("unknown insert type");
      }
    } else {
      //todo test for match query
      //not a define
      throw new RuntimeException(
          "Unsupported query type during datalog query execution: " + q.getClass());
    }
    System.out.println(toRet.toString());
    DatalogTokenizer tokenizer = new DatalogTokenizer(new StringReader(toRet.toString()));
    try {
      Set<Clause> datalog_clauses = DatalogParser.parseProgram(tokenizer);
      return datalog_clauses;
    } catch (DatalogParseException e) {
      e.printStackTrace();
      return new HashSet<>();
    }
  }

}
