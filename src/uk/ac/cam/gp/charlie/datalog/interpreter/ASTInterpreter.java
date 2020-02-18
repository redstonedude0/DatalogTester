package uk.ac.cam.gp.charlie.datalog.interpreter;

import abcdatalog.ast.Clause;
import abcdatalog.ast.PositiveAtom;
import abcdatalog.parser.DatalogParseException;
import abcdatalog.parser.DatalogParser;
import abcdatalog.parser.DatalogTokenizer;
import java.io.StringReader;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import uk.ac.cam.gp.charlie.DebugHelper;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.AttributeValue;
import uk.ac.cam.gp.charlie.ast.ConstantValue;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.Variable;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefine;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefineRule;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsert;
import uk.ac.cam.gp.charlie.ast.queries.match.ConditionIsa;
import uk.ac.cam.gp.charlie.ast.queries.match.ConditionNeq;
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
      Integer typeNum = c.getTypeNumber(define.identifier);
      toRet.append(String.format("t_subs(t_%d,%s).\n", typeNum, define.subs.identifier));
      for (Attribute attribute : define.attributes) {
        toRet.append(String
            .format("t_hasattr(t_%d,a_%d).\n", typeNum, c.getAttributeNumber(attribute)));
      }
      for (Plays play : define.plays) {
        toRet.append(
            String.format("t_playsrole(t_%d,r_%d).\n", typeNum, c.getPlaysNumber(play)));
      }
      for (Plays play : define.relates) {
        toRet.append(String
            .format("t_relates(t_%d,r_%d).\n", typeNum, c.getPlaysNumber(play)));
      }
      toRet.append("\n");
    } else if (q instanceof QueryInsert) {
      QueryInsert insert = (QueryInsert) q;
      Integer iNum = c.getInstanceNumber();
      toRet.append(String.format("instanceof(e_%d,t_%d).\n", iNum, c.getTypeNumber(insert.isa)));
      for (Entry<Attribute, AttributeValue> entry : insert.attributes.entrySet()) {
        AttributeValue attribute = entry.getValue();
        if (attribute instanceof ConstantValue) {
          toRet.append(String.format("instanceattr(e_%d,a_%d,const_%d).\n", iNum,
              c.getAttributeNumber(entry.getKey()), c.getConstNumber((ConstantValue) attribute)));
        } else {
          throw new RuntimeException(
              "Variables not currently implemented as values. Probably just need const_{scopeResolve attribute} to implement this though");
        }
      }
      for (Entry<Plays, Variable> entry : insert.plays) {
        toRet.append(String
            .format("instancerel(e_%d,e_%d,r_%d).\n", iNum, c.resolveScope(entry.getValue()),
                c.getPlaysNumber(entry.getKey())));
        c.bindInstanceToInstance(c.resolveScope(entry.getValue()),iNum);
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
          String varString = "Var" + varNum;
          vars.add(varString);
          conditions
              .add(String.format("instanceof(%s,t_%d)", varString, c.getTypeNumber(cond.type)));
          for (Entry<Attribute, AttributeValue> entry : cond.has.entrySet()) {
            String attrval_s;
            AttributeValue attrval = entry.getValue();
            if (attrval instanceof ConstantValue) {
              attrval_s = "const_" + c.getConstNumber((ConstantValue) attrval);
            } else if (attrval instanceof Variable) {
              Integer i = c.resolveScope((Variable) attrval);
              if (i == null) {
                attrval_s = "Var" + c.getVariableNumber((Variable) attrval);
                vars.add(attrval_s);
              } else {
                attrval_s = "e_" + i;
              }
            } else {
              throw new RuntimeException("Unknown attribute value");
            }
            conditions.add(String
                .format("instanceattr(%s,a_%d,%s)", varString, c.getAttributeNumber(entry.getKey()),
                    attrval_s));
          }
          for (Entry<Plays, Variable> entry : cond.relates) {
            Integer i = c.resolveScope(entry.getValue());
            String relates_s;
            if (i == null) {
              relates_s = "Var" + c.getVariableNumber(entry.getValue());
              vars.add(relates_s);
            } else {
              relates_s = "e_" + i;
            }
            conditions.add(String
                .format("instancerel(%s,%s,r_%d)", varString,relates_s,c.getPlaysNumber(entry.getKey())));
          }
        } else {
          throw new RuntimeException(
              "Unknown match condition during datalog translation:" + mc.getClass()
                  .getSimpleName());
        }
      }
      toRet.append("query(");
      toRet.append(String.join(",", vars));
      toRet.append(") :- ");
      toRet.append(String.join(", ", conditions));
      toRet.append(".\n");
    } else if (q instanceof QueryDefineRule) {
      QueryDefineRule rule = (QueryDefineRule) q;
      c.resetVariableNumber();
      int invariantNum = c.getInvariantNumber(rule.identifier);
      Set<String> vars = new HashSet<>();
      List<String> conditions = new ArrayList<>();
      for (MatchCondition mc : rule.when) {
        if (mc instanceof ConditionIsa) {
          ConditionIsa cond = (ConditionIsa) mc;
          int varNum = c.getVariableNumber(cond.returnVariable);
          String varString = "Var" + varNum;
          vars.add(varString);
          conditions
              .add(String.format("instanceof(%s,t_%d)", varString, c.getTypeNumber(cond.type)));
          for (Entry<Attribute, AttributeValue> entry : cond.has.entrySet()) {
            String attrval_s;
            AttributeValue attrval = entry.getValue();
            if (attrval instanceof ConstantValue) {
              attrval_s = "const_" + c.getConstNumber((ConstantValue) attrval);
            } else if (attrval instanceof Variable) {
              Integer i = c.resolveScope((Variable) attrval);
              if (i == null) {
                attrval_s = "Var" + c.getVariableNumber((Variable) attrval);
                vars.add(attrval_s);
              } else {
                attrval_s = "e_" + i;
              }
            } else {
              throw new RuntimeException("Unknown attribute value");
            }
            conditions.add(String
                .format("instanceattr(%s,a_%d,%s)", varString, c.getAttributeNumber(entry.getKey()),
                    attrval_s));
          }
          for (Entry<Plays, Variable> entry : cond.relates) {
            Integer i = c.resolveScope(entry.getValue());
            String relates_s;
            if (i == null) {
              relates_s = "Var" + c.getVariableNumber(entry.getValue());
              vars.add(relates_s);
            } else {
              relates_s = "e_" + i;
            }
            conditions.add(String
                .format("instancerel(%s,%s,r_%d)", varString, relates_s,
                    c.getPlaysNumber(entry.getKey())));
          }
        } else if (mc instanceof ConditionNeq) {
          ConditionNeq cond = (ConditionNeq) mc;
          String[] sides = new String[2];
          for (int side = 0; side < sides.length;side++) {
            AttributeValue attr = cond.lhs;
            if (side == 1) {
              attr = cond.rhs;
            }
            String output;
            if (attr instanceof ConstantValue) {
              output = "const_" + c.getConstNumber((ConstantValue) attr);
            } else if (attr instanceof Variable) {
              Integer i = c.resolveScope((Variable) attr);
              if (i == null) {
                output = "Var" + c.getVariableNumber((Variable) attr);
                vars.add(output);
              } else {
                output = "e_" + i;
              }
            } else {
              throw new RuntimeException("Unknown attribute value (lhs)");
            }
            sides[side] = output;
          }
          conditions.add(String.format("%s != %s",sides[0],sides[1]));
        } else {
          throw new RuntimeException(
              "Unknown match condition during datalog translation:" + mc.getClass()
                  .getSimpleName());
        }
      }
      //invariant_1(P1,P2) :- instanceof (X,employment),
      //                 instancerel(X,P1,employee),
      //                 instancerel(X,Y ,employer),
      //                 instanceof (Z,employment),
      //                 instancerel(Z,P2,employee),
      //                 instancerel(Z,Y ,employer),
      //                 not invariant_1_inv(W,P1,P2).
      //invariant_1_inv(P1,P2) :- instanceof(W,coworkers),
      //                    instancerel(W,P1,employee),
      //                    instancerel(W,P2,employee).
      QueryInsert then = rule.then;
      QueryInsert insert;
      Map<String,Variable> variableMap = new HashMap<>();
      {
        List<String> invConditions = new ArrayList<>();
        List<String> invVars = new ArrayList<>();
        invConditions.add(String.format("instanceof(X,t_%s)",c.getTypeNumber(then.isa)));
        insert = new QueryInsert(null,then.isa);
        for (Entry<Plays,Variable> entry : then.plays) {
          String relates_s = "Var" + c.getVariableNumber(entry.getValue());
          vars.add(relates_s);
          invVars.add(relates_s);
          invConditions.add(String
              .format("instancerel(X,%s,r_%d)",relates_s,c.getPlaysNumber(entry.getKey())));
          insert.plays.add(new SimpleEntry<>(entry.getKey(),entry.getValue()));
          variableMap.put(relates_s,entry.getValue());
        }
        toRet.append("invariant__"+invariantNum+"_inv(");
        toRet.append(String.join(",", invVars));
        toRet.append(") :- ");
        toRet.append(String.join(", ", invConditions));
        toRet.append(".\n");
        conditions.add("not invariant__"+invariantNum+"_inv("+String.join(",", invVars)+")");
      }
      toRet.append("invariant__"+invariantNum+"(");
      toRet.append(String.join(",", vars));
      toRet.append(") :- ");
      toRet.append(String.join(", ", conditions));
      toRet.append(".\n");
      c.invariantVariableMappings.put(invariantNum,variableMap);
      c.invariantInsertQueries.put(invariantNum,insert);
      //
    } else {
      throw new RuntimeException(
          "Unsupported query type during datalog query execution: " + q.getClass());
    }
    if (DebugHelper.VERBOSE_DATALOG) {
      System.out.print(c.prettifyDatalog(toRet.toString()));
    }
    DatalogTokenizer tokenizer = new DatalogTokenizer(new StringReader(toRet.toString()));
    try {
      Set<Clause> clauses = DatalogParser.parseProgram(tokenizer);
      if (q instanceof QueryInsert) {
        c.bindInstanceClauses(c.getMaxInstanceNumber(),clauses);
      }
      return clauses;
    } catch (DatalogParseException e) {
      e.printStackTrace();
      return new HashSet<>();
    }
  }

  /**
   * Used for converting match queries into runnable code in the engine
   */
  public static PositiveAtom toExecutableDatalog(Query q, Context c) {
    StringBuilder toRet = new StringBuilder();
    if (q instanceof QueryMatch) {
      Set<String> vars = new HashSet<>();
      for (int i = 0; i <= c.getMaxVariableNumber(); i++) {
        vars.add("Var" + i);
      }
      toRet.append("query(");
      toRet.append(String.join(",", vars));
      toRet.append(").\n");
    } else if (q instanceof QueryDefineRule) {
      Set<String> vars = new HashSet<>();
      for (int i = 0; i <= c.getMaxVariableNumber(); i++) {
        vars.add("Var" + i);
      }
      toRet.append("invariant__"+c.getMaxInvariantNumber()+"(");
      toRet.append(String.join(",", vars));
      toRet.append(").\n");
    } else {
      throw new RuntimeException(
          "Unsupported query type during datalog query execution: " + q.getClass());
    }
    if (DebugHelper.VERBOSE_DATALOG) {
      System.out.print(c.prettifyDatalog(toRet.toString()));
    }
    DatalogTokenizer tokenizer = new DatalogTokenizer(new StringReader(toRet.toString()));
    try {
      return DatalogParser.parseClauseAsPositiveAtom(tokenizer);
    } catch (DatalogParseException e) {
      e.printStackTrace();
      return null;
    }
  }


}
