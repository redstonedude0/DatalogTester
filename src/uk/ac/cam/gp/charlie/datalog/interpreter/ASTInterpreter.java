package uk.ac.cam.gp.charlie.datalog.interpreter;

import abcdatalog.ast.Clause;
import abcdatalog.parser.DatalogParseException;
import abcdatalog.parser.DatalogParser;
import abcdatalog.parser.DatalogTokenizer;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.AttributeValue;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.Variable;
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
    /*
    t_ type definition
    a_ attribute definition
    r_ relation definiition (plays)

    e_ thing instance
    const_ constant instance

     */
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
      Integer iNum = c.getInstanceNumber();
      if (insert instanceof QueryInsertEntity) {
        QueryInsertEntity entity = (QueryInsertEntity) insert;
        toRet.append(String.format("instanceof(e_%d,t_%d).\n",iNum,c.getTypeNumber(entity.isa)));
        for (Entry<Attribute, AttributeValue> entry : entity.attributes.entrySet()) {
          toRet.append(String.format("instanceattr(e_%d,a_%d,const_%d).\n",iNum,c.getAttributeNumber(entry.getKey()),c.getConstNumber(entry.getValue())));
        }
      } else if (insert instanceof QueryInsertRelation) {
        QueryInsertRelation relation = (QueryInsertRelation) insert;
        toRet.append(String.format("instanceof(e_%d,t_%d).\n",iNum,c.getTypeNumber(relation.isa)));
        for (Entry<Plays, Variable> entry : relation.plays) {
          toRet.append(String.format("instancerel(e_%d,e_%d,r_%d).\n",iNum,c.resolveScope(entry.getValue()),c.getPlaysNumber(entry.getKey())));
        }
      } else {
        throw new RuntimeException("unknown insert type");
      }
      c.addToScope(insert.returnVariable,iNum);
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
