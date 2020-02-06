package uk.ac.cam.gp.charlie.datalog.interpreter;

import abcdatalog.ast.Clause;
import abcdatalog.ast.PositiveAtom;
import abcdatalog.parser.DatalogParseException;
import abcdatalog.parser.DatalogParser;
import abcdatalog.parser.DatalogTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.AttributeValue;
import uk.ac.cam.gp.charlie.ast.ConstantValue;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.Variable;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefine;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefineEntity;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefineRelation;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsert;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsertEntity;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsertRelation;
import uk.ac.cam.gp.charlie.ast.queries.match.ConditionIsa;
import uk.ac.cam.gp.charlie.ast.queries.match.MatchCondition;
import uk.ac.cam.gp.charlie.ast.queries.match.QueryMatch;

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
        toRet.append(String.format("instanceof(e_%d,t_%d).\n", iNum, c.getTypeNumber(entity.isa)));
        for (Entry<Attribute, AttributeValue> entry : entity.attributes.entrySet()) {
          AttributeValue attribute = entry.getValue();
          if (attribute instanceof ConstantValue) {
            toRet.append(String.format("instanceattr(e_%d,a_%d,const_%d).\n", iNum,
                c.getAttributeNumber(entry.getKey()), c.getConstNumber((ConstantValue) attribute)));
          } else {
            throw new RuntimeException(
                "Variables not currently implemented as values. Probably just need const_{scopeResolve attribute}");
          }
        }
      } else if (insert instanceof QueryInsertRelation) {
        QueryInsertRelation relation = (QueryInsertRelation) insert;
        toRet
            .append(String.format("instanceof(e_%d,t_%d).\n", iNum, c.getTypeNumber(relation.isa)));
        for (Entry<Plays, Variable> entry : relation.plays) {
          toRet.append(String
              .format("instancerel(e_%d,e_%d,r_%d).\n", iNum, c.resolveScope(entry.getValue()),
                  c.getPlaysNumber(entry.getKey())));
        }
      } else {
        throw new RuntimeException("unknown insert type");
      }
      c.addToScope(insert.returnVariable, iNum);
      toRet.append("\n");
    } else if (q instanceof QueryMatch) {
      QueryMatch match = (QueryMatch) q;
      c.resetVariableNumber();
      Set<String> vars = new HashSet<>();
      List<String> conditions = new ArrayList<>();
      //query(X,Y) :- instanceof(X,person), instanceattr(X,name,str_Bob), instanceof(Y,person), instanceattr(Y,name,str_Alice).
      for (MatchCondition mc : ((QueryMatch) q).conditions) {
        if (mc instanceof ConditionIsa) {
          ConditionIsa cond = (ConditionIsa) mc;
          int varNum = c.getVariableNumber(cond.returnVariable);
          String varString = "Var"+varNum;
          vars.add(varString);
          conditions.add(String.format("instanceof(%s,t_%d)",varString,c.getTypeNumber(cond.type)));
          for (Entry<Attribute,AttributeValue> entry : cond.has.entrySet()) {
            String attrval_s;
            AttributeValue attrval = entry.getValue();
            if (attrval instanceof ConstantValue) {
              attrval_s = "const_"+c.getConstNumber((ConstantValue) attrval);
            } else if (attrval instanceof Variable) {
              Integer i = c.resolveScope((Variable) attrval);
              if (i == null) {
                attrval_s = "Var"+c.getVariableNumber((Variable) attrval);
              } else {
                attrval_s = "e_"+i;
              }
            } else {
              throw new RuntimeException("Unknown attribute value");
            }
            conditions.add(String.format("instanceattr(%s,a_%d,%s)",varString,c.getAttributeNumber(entry.getKey()),attrval_s));
          }
          for (Entry<Plays,Variable> entry : cond.relates) {
            throw new RuntimeException("unimplemented");
          }
        } else {
          throw new RuntimeException("Unknown match condition during datalog translation:" + mc.getClass().getSimpleName());
        }
      }
      toRet.append("query(");
      toRet.append(String.join(",",vars));
      toRet.append(") :- ");
      toRet.append(String.join(", ",conditions));
      toRet.append(".\n");
    } else {
      throw new RuntimeException(
          "Unsupported query type during datalog query execution: " + q.getClass());
    }
    System.out.print(c.prettifyDatalog(toRet.toString()));
    DatalogTokenizer tokenizer = new DatalogTokenizer(new StringReader(toRet.toString()));
    try {
      return DatalogParser.parseProgram(tokenizer);
    } catch (DatalogParseException e) {
      e.printStackTrace();
      return new HashSet<>();
    }
  }

  /**
   * Used for converting match queries into runnable code in the engine
   * @param q
   * @param c
   * @return
   */
  public static PositiveAtom toExecutableDatalog(Query q, Context c) {
    StringBuilder toRet = new StringBuilder();
    if (q instanceof QueryMatch) {
      QueryMatch match = (QueryMatch) q;
      Set<String> vars = new HashSet<>();
      for (int i = 0; i <= c.getMaxVariableNumber(); i++) {
        vars.add("Var"+i);
      }
      toRet.append("query(");
      toRet.append(String.join(",",vars));
      toRet.append(").\n");
    } else {
      //todo test for non-executing query
      throw new RuntimeException(
          "Unsupported query type during datalog query execution: " + q.getClass());
    }
    System.out.print(c.prettifyDatalog(toRet.toString()));
    DatalogTokenizer tokenizer = new DatalogTokenizer(new StringReader(toRet.toString()));
    try {
      return DatalogParser.parseClauseAsPositiveAtom(tokenizer);
    } catch (DatalogParseException e) {
      e.printStackTrace();
      return null;
    }
  }



}
