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
 * Interprets an AST (AST->Datalog transformation)
 */
public class ASTInterpreter {

  /**
   * Convert a Query with a Context to datalog
   */
  public static Set<Clause> toDatalog(Query q, Context c) {
    /*
    Key for datalog atoms:
    t_<n> type definition
    a_<n> attribute definition
    r_<n> relation definiition (plays)
    e_<n> thing instance
    const_<n> constant instance
    invariant__<n> rule (note the double '_' to prevent pattern matching with t_ during prettyprint)
     */
    StringBuilder toRet = new StringBuilder();
    //Parse the query based upon its type
    if (q instanceof QueryDefine) {
      //Defining an entity or relation
      QueryDefine define = (QueryDefine) q;
      //Get a unique type number to represent this new type
      Integer typeNum = c.getTypeNumber(define.identifier);
      //Define the type subs fact
      toRet.append(String.format("t_subs(t_%d,%s).\n", typeNum, define.subs.identifier));
      //Define that this type has each attribute
      for (Attribute attribute : define.attributes) {
        toRet.append(String
            .format("t_hasattr(t_%d,a_%d).\n", typeNum, c.getAttributeNumber(attribute)));
      }
      //Define that this type plays each role
      for (Plays play : define.plays) {
        toRet.append(
            String.format("t_playsrole(t_%d,r_%d).\n", typeNum, c.getPlaysNumber(play)));
      }
      //Define that this type relates each role-player
      for (Plays play : define.relates) {
        toRet.append(String
            .format("t_relates(t_%d,r_%d).\n", typeNum, c.getPlaysNumber(play)));
      }
      toRet.append("\n");
    } else if (q instanceof QueryInsert) {
      //Inserting data
      QueryInsert insert = (QueryInsert) q;
      //Get a unique number to represent this 'thing'
      Integer iNum = c.getInstanceNumber();
      //Add the fact that this instance is of a type
      toRet.append(String.format("instanceof(e_%d,t_%d).\n", iNum, c.getTypeNumber(insert.isa)));
      //Define this instance has each attribute
      for (Entry<Attribute, AttributeValue> entry : insert.attributes.entrySet()) {
        AttributeValue attribute = entry.getValue();
        if (attribute instanceof ConstantValue) {
          toRet.append(String.format("instanceattr(e_%d,a_%d,const_%d).\n", iNum,
              c.getAttributeNumber(entry.getKey()), c.getConstNumber((ConstantValue) attribute)));
        } else {
          //For now only constant values are defined (can't set an attribute to a variable value)
          throw new RuntimeException(
              "Variables not currently implemented as values. Probably just need const_{scopeResolve attribute} to implement this though");
        }
      }
      //Define instance relations (e.g. insert ($x,$y) isa friends, inserts a friends with <friend,$x> as a plays entry
      for (Entry<Plays, Variable> entry : insert.plays) {
        toRet.append(String
            .format("instancerel(e_%d,e_%d,r_%d).\n", iNum, c.resolveScope(entry.getValue()),
                c.getPlaysNumber(entry.getKey())));
        //entry.getValue() is the variable which plays in this relation, here we're binding the instances
        //so that if x is deleted, then '(x,y)isa friends' is deleted.
        c.bindInstanceToInstance(c.resolveScope(entry.getValue()),iNum);
      }
      //Add the return variable to scope, pointing at this instance
      c.addToScope(insert.returnVariable, iNum);
      toRet.append("\n");
    } else if (q instanceof QueryMatch) {
      //Define the query for a match query (to be executed using toExecutableDatalog)
      QueryMatch match = (QueryMatch) q;
      //Reset the variable number (used to track the number of free variables)
      c.resetVariableNumber();
      //Create a list of vars and conditions, this block will produce
      //"query(Var0,Var1,...,VarN) :- Cond0,Cond1,...,ConM
      //e.g: query(Var0,Var1) :- instanceof(Var0,person), instanceattr(Var0,a_0,str_Bob), instanceof(Var1,person), instanceattr(Var1,a_0,str_Alice).
      Set<String> vars = new HashSet<>();
      List<String> conditions = new ArrayList<>();
      //Add each condition
      parseConditions(((QueryMatch) q).conditions,c,vars,conditions);
      toRet.append("query(");
      toRet.append(String.join(",", vars));
      toRet.append(") :- ");
      toRet.append(String.join(", ", conditions));
      toRet.append(".\n");
    } else if (q instanceof QueryDefineRule) {
      //Defining a rule, very similar to QueryMatch above
      QueryDefineRule rule = (QueryDefineRule) q;
      c.resetVariableNumber();
      int invariantNum = c.getInvariantNumber(rule.identifier);
      Set<String> vars = new HashSet<>();
      List<String> conditions = new ArrayList<>();
      //Add each condition
      parseConditions(rule.when,c,vars,conditions);
      QueryInsert then = rule.then;
      //the 'then' query is discarded, the 'insert' query is sued to ensure returnVariable is null so scope is unaffected
      QueryInsert insert;
      //How to map the variables from the match, to the insert
      Map<String,Variable> variableMap = new HashMap<>();
      //Code block to generate invariant__N_inv, the inverted clause is used because you cannot negate a group of clauses in datalog
      {
        //The inverted rule has conditions and variables too
        List<String> invConditions = new ArrayList<>();
        List<String> invVars = new ArrayList<>();
        //The inverted rule is true iff the 'then' clause holds for the variables passed into the rule from then 'when' clause
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
        //add the inverted rule as a negated condition
        conditions.add("not invariant__"+invariantNum+"_inv("+String.join(",", invVars)+")");
      }
      toRet.append("invariant__"+invariantNum+"(");
      toRet.append(String.join(",", vars));
      toRet.append(") :- ");
      toRet.append(String.join(", ", conditions));
      toRet.append(".\n");
      //create the mappings so the rule checker can assert the invariant
      c.invariantVariableMappings.put(invariantNum,variableMap);
      c.invariantInsertQueries.put(invariantNum,insert);
    } else {
      throw new RuntimeException(
          "Unsupported query type during datalog query execution: " + q.getClass());
    }
    //if debugging then dump the datalog generated
    if (DebugHelper.VERBOSE_DATALOG) {
      System.out.print(c.prettifyDatalog(toRet.toString()));
    }

    //Tokenise and parse
    DatalogTokenizer tokenizer = new DatalogTokenizer(new StringReader(toRet.toString()));
    try {
      Set<Clause> clauses = DatalogParser.parseProgram(tokenizer);
      if (q instanceof QueryInsert) {
        //if we're inserting then add the binding for the clauses so we can remove them if we do a match..delete
        c.bindInstanceClauses(c.getMaxInstanceNumber(),clauses);
      }
      return clauses;
    } catch (DatalogParseException e) {
      e.printStackTrace();
      return new HashSet<>();
    }
  }

  private static void parseConditions(List<MatchCondition> astConditions, Context c, Set<String> vars, List<String> datalogConditions) {
    //Parse each AST condition into a datalog condition
    for (MatchCondition mc : astConditions) {
      //Switch based on condition type
      if (mc instanceof ConditionIsa) {
        //Simple "$x isa type, has attr $val,..." in which has cond.has may not be empty
        //Or "$x (lab1:$v1) isa rel" in which case cond.relates may not be empty
        //In the current implementation cond.has and cond.relates should never both have contents
        //Since relations don't currently have attributes in this implementation
        ConditionIsa cond = (ConditionIsa) mc;
        //get a unique variable number to represent the return variable (e.g. $x)
        int varNum = c.getVariableNumber(cond.returnVariable);
        String varString = "Var" + varNum;
        vars.add(varString);
        //Assert that $x must be of the required type
        datalogConditions
            .add(String.format("instanceof(%s,t_%d)", varString, c.getTypeNumber(cond.type)));
        //For every attribute it "has"
        for (Entry<Attribute, AttributeValue> entry : cond.has.entrySet()) {
          String attrval_s;
          AttributeValue attrval = entry.getValue();
          //Get the value (e.g. Variable(n) in "has name $n" or ConstantValue(Bob) in "has name 'Bob'"
          if (attrval instanceof ConstantValue) {
            //If it's a constant, reference the constant atom (effectively an attribute entity)
            attrval_s = "const_" + c.getConstNumber((ConstantValue) attrval);
          } else if (attrval instanceof Variable) {
            //If it's a variable, try and resolve the scope
            Integer i = c.resolveScope((Variable) attrval);
            if (i == null) {
              //Not in the scope/dataset - it's a bound variable in this match statement, add it to the var list
              attrval_s = "Var" + c.getVariableNumber((Variable) attrval);
              vars.add(attrval_s);
            } else {
              //Scope resolved, it's in the dataset - set it to that instance
              attrval_s = "e_" + i;
            }
          } else {
            throw new RuntimeException("Unknown attribute value");
          }
          //Add the string
          datalogConditions.add(String
              .format("instanceattr(%s,a_%d,%s)", varString, c.getAttributeNumber(entry.getKey()),
                  attrval_s));
        }
        for (Entry<Plays, Variable> entry : cond.relates) {
          Integer i = c.resolveScope(entry.getValue());
          String relates_s;
          //Similar scope resolution as above to resolve the variable
          if (i == null) {
            relates_s = "Var" + c.getVariableNumber(entry.getValue());
            vars.add(relates_s);
          } else {
            relates_s = "e_" + i;
          }
          datalogConditions.add(String
              .format("instancerel(%s,%s,r_%d)", varString,relates_s,c.getPlaysNumber(entry.getKey())));
        }
      } else if (mc instanceof ConditionNeq) {
        //NotEQual condition
        //e.g. $x != $y
        //e.g. $n != "Bob"
        ConditionNeq cond = (ConditionNeq) mc;
        //LHS and RHS need to same parsing, so use an array to store them
        String[] sides = new String[2];
        for (int side = 0; side < sides.length;side++) {
          //Either parse the lhs or rhs
          AttributeValue attr = cond.lhs;
          if (side == 1) {
            attr = cond.rhs;
          }
          //Store the parsed AttributeValue as a String
          String output;
          //Same constant&scope resolving as above in ConditionIsa
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
            throw new RuntimeException("Unknown attribute value");
          }
          //Set the lhs or rhs as appropriate
          sides[side] = output;
        }
        //Add the condition
        datalogConditions.add(String.format("%s != %s",sides[0],sides[1]));
      } else {
        throw new RuntimeException(
            "Unknown match condition during datalog translation:" + mc.getClass()
                .getSimpleName());
      }
    }
  }

  /**
   * Used for converting match queries into runnable code in the engine,
   * this is used for rules and matches (which actually instantiate the engine)
   */
  public static PositiveAtom toExecutableDatalog(Query q, Context c) {
    StringBuilder toRet = new StringBuilder();
    //Switch based on type
    if (q instanceof QueryMatch) {
      //Collect strings representing all free vars in this query
      //ASSERT: This is called directly after toDatalog for the same match query, in order to ensure
      //c.getMaxVariableNumber() returns the correct number of free vars
      Set<String> vars = new HashSet<>();
      for (int i = 0; i <= c.getMaxVariableNumber(); i++) {
        vars.add("Var" + i);
      }
      //Append "query(Var0,Var1,...,VarN)."
      toRet.append("query(");
      toRet.append(String.join(",", vars));
      toRet.append(").\n");
    } else if (q instanceof QueryDefineRule) {
      //Collect strings as vars, as above
      Set<String> vars = new HashSet<>();
      for (int i = 0; i <= c.getMaxVariableNumber(); i++) {
        vars.add("Var" + i);
      }
      //Append "invariant__N(Var0,Var1,...,VarM)."
      //NOTE: 2 '_'s to prevent pattern matching with t_ in pretty print
      toRet.append("invariant__"+c.getMaxInvariantNumber()+"(");
      toRet.append(String.join(",", vars));
      toRet.append(").\n");
    } else {
      throw new RuntimeException(
          "Unsupported query type during datalog query execution: " + q.getClass());
    }
    if (DebugHelper.VERBOSE_DATALOG) {//Debug if debug wanted
      System.out.print(c.prettifyDatalog(toRet.toString()));
    }
    //Tokenise and parse, return as positive atom.
    DatalogTokenizer tokenizer = new DatalogTokenizer(new StringReader(toRet.toString()));
    try {
      return DatalogParser.parseClauseAsPositiveAtom(tokenizer);
    } catch (DatalogParseException e) {
      e.printStackTrace();
      return null;
    }
  }


}
