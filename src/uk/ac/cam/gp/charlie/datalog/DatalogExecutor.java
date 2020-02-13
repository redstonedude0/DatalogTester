package uk.ac.cam.gp.charlie.datalog;

import abcdatalog.ast.Clause;
import abcdatalog.ast.PositiveAtom;
import abcdatalog.ast.validation.DatalogValidationException;
import abcdatalog.engine.DatalogEngine;
import abcdatalog.engine.bottomup.sequential.SemiNaiveEngine;
import abcdatalog.util.substitution.Substitution;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import uk.ac.cam.gp.charlie.DebugHelper;
import uk.ac.cam.gp.charlie.Executor;
import uk.ac.cam.gp.charlie.Result;
import uk.ac.cam.gp.charlie.TestEnvironment;
import uk.ac.cam.gp.charlie.Workbench;
import uk.ac.cam.gp.charlie.ast.Variable;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefine;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsert;
import uk.ac.cam.gp.charlie.ast.queries.match.QueryMatch;
import uk.ac.cam.gp.charlie.datalog.interpreter.ASTInterpreter;
import uk.ac.cam.gp.charlie.datalog.interpreter.Context;
import uk.ac.cam.gp.charlie.graql.parsing.GraqlParser;

public class DatalogExecutor extends Executor {

  private DatalogEngine engine; //The engine to use for datalog interpretation
  private Context c = new Context(); //The context of the test environment, contains the ASTs representing the environment
  public static GraqlParser parser = new GraqlParser();

  /**
   * Execute a query using the context and engine.
   *
   * @param q the query to execute
   * @return a map of variableName->e_XXX identifier, if this is a get query.
   */
  private List<Map<Variable, String>> executeQuery(Query q) {
    try {
      /*
        This function will take the query, convert it to datalog and put in in to the context datalog clause set.
        It will use and modify variables in the context to properly store the current thing ids, etc. It will
        create relevant pointers into the clause set to allow deletion to work properly.
      */
      if (q instanceof QueryDefine) {//defining
        c.datalog.addAll(ASTInterpreter.toDatalog(q, c));
      } else if (q instanceof QueryInsert) {//inserting
        c.datalog.addAll(ASTInterpreter.toDatalog(q, c));
      } else if (q instanceof QueryMatch) {
        //Need to match into scope(s), then perform insertions
        //engine = new RecursiveQsqEngine(); //doesn't allow disunification
        engine = new SemiNaiveEngine(); //allows disunification
        if (DebugHelper.VERBOSE_DATALOG) {
          System.out.println("\u001b[33;1m<EXECUTING>\u001b[0m");
        }
        Set<Clause> clauses = new HashSet<>(c.datalog);
        clauses.addAll(ASTInterpreter.toDatalog(q, c));
        engine.init(clauses);

        //Tokenize, parse, and query(execute)
        if (DebugHelper.VERBOSE_DATALOG) {
          System.out.print("\u001b[33;1mResults of \u001b[0m");
        }
        PositiveAtom query = ASTInterpreter.toExecutableDatalog(q, c);
        Set<PositiveAtom> results = engine.query(query);
        System.out.println(results);
        if (DebugHelper.VERBOSE_DATALOG) {
          System.out.println("\u001b[33;1m</EXECUTING>\u001b[0m\n");
        }

        switch (((QueryMatch) q).getAction()) {
          case GET:
            if (DebugHelper.VERBOSE_DATALOG) {
              System.out.println("\u001b[33;1mResults of query:\u001b[0m");
            }
            List<Map<Variable, String>> toRet = new ArrayList<>();
            for (PositiveAtom result : results) {
              if (DebugHelper.VERBOSE_DATALOG) {
                System.out.println("Match:");
              }
              Map<Variable, String> resultMap = new HashMap<>();
              //Get variables
              Substitution s = result.unify(query);
              for (int i = 0; i <= c.getMaxVariableNumber(); i++) {
                Variable v = c.getVariableByNumber(i);
                String boundValue = s.get(abcdatalog.ast.Variable.create("Var" + i)).toString();
                //add to result map
                Integer boundInt = Integer.parseInt(boundValue.split("_")[1]);
                if (boundValue.startsWith("e_")) {
                  //'Thing' -> not stored in resultMap
                  if (DebugHelper.VERBOSE_DATALOG) {
                    System.out.println(
                        " _$" + v.getIdentifier() + " => \u001b[35m{" + boundInt + "}\u001b[0m");
                  }
                } else if (boundValue.startsWith("const_")) {
                  resultMap.put(v, c.getConstantFromID(boundInt).value + "");
                  if (DebugHelper.VERBOSE_DATALOG) {
                    System.out.println("  $" + v.getIdentifier() + " => \u001b[33m" + c
                        .getConstantFromID(boundInt).value + "\u001b[0m");
                  }
                } else {
                  throw new RuntimeException(
                      "Invalid returned object (unimplemented): " + boundValue);
                }
              }
              toRet.add(resultMap);
            }
            return toRet;
          case DELETE:
            if (DebugHelper.VERBOSE_DATALOG) {
              System.out.println("\u001b[33;1mDeleting:\u001b[0m");
            }
            Variable variableToDelete = ((QueryMatch) q).getDATA_DELETE();
            for (PositiveAtom result : results) {
              if (DebugHelper.VERBOSE_DATALOG) {
                System.out.println("Match:");
              }
              //Get variable
              Substitution s = result.unify(query);
              for (int i = 0; i <= c.getMaxVariableNumber(); i++) {
                Variable v = c.getVariableByNumber(i);
                if (v.equals(variableToDelete)) {
                  String boundValue = s.get(abcdatalog.ast.Variable.create("Var" + i)).toString();
                  Integer boundInt = Integer.parseInt(boundValue.split("_")[1]);
                  if (boundValue.startsWith("e_")) {
                    //'Thing' -> can be deleted
                    if (DebugHelper.VERBOSE_DATALOG) {
                      System.out.println(
                          "  $" + v.getIdentifier() + " => \u001b[35m{" + boundInt + "}\u001b[0m");
                    }
                  } else {
                    throw new RuntimeException("Attemped to delete non-thing object " + boundValue);
                  }
                  Set<Clause> clausesToDelete = c.getInstanceBoundClauses_TRANSITIVE(boundInt);
                  if (DebugHelper.VERBOSE_DATALOG) {
                    System.out.println("    Clauses:");
                  }
                  for (Clause clause : clausesToDelete) {
                    if (DebugHelper.VERBOSE_DATALOG) {
                      System.out.println("      " + c.prettifyDatalog(clause.toString()));
                    }
                  }
                  c.datalog.removeAll(clausesToDelete);
                }
              }
            }
            if (DebugHelper.VERBOSE_DATALOG) {
              System.out.println("\u001b[33;1m</RESULTS>\u001b[0m\n");
            }
            break;
          case INSERT:
            //Insert as new inserts
            if (DebugHelper.VERBOSE_DATALOG) {
              System.out.println("\u001b[33;1m<RESULTS>\u001b[0m");
            }
            for (PositiveAtom result : results) {
              //Get variables
              Substitution s = result.unify(query);
              Set<Variable> variables = new HashSet<>();
              for (int i = 0; i <= c.getMaxVariableNumber(); i++) {
                Variable v = c.getVariableByNumber(i);
                String boundValue = s.get(abcdatalog.ast.Variable.create("Var" + i)).toString();
                Integer boundInt = Integer.parseInt(boundValue.substring(2));
                if (!boundValue.equals("e_" + boundInt)) {
                  throw new RuntimeException(
                      "Invalid returned object (unimplemented): " + boundValue);
                }
                //bind scope
                c.addToScope(v, boundInt);
                variables.add(v);
              }
              //insert
              executeQuery(((QueryMatch) q).getDATA_INSERT());
              //unbind scope
              for (Variable v : variables) {
                c.removeFromScope(v);
              }
            }
            if (DebugHelper.VERBOSE_DATALOG) {
              System.out.println("\u001b[33;1m</RESULTS>\u001b[0m\n");
            }
        }
        c.resetVariableNumber();
      } else {
        throw new RuntimeException("Unsupported query type during datalog query execution");
      }
    } catch (DatalogValidationException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Create a DatalogExecutor from a TestEnvironment. Initialises the contexstatementt and
   * environment
   *
   * @param environment the environment the tests will be run in (schema+data)
   */
  public DatalogExecutor(TestEnvironment environment) {
    //Convert the Graql environment to a Context
    List<Query> schema = parser.graqlToAST(environment.schema);
    List<Query> data = parser.graqlToAST(environment.data);

    for (Query q : schema) {
      executeQuery(q);
    }
    for (Query q : data) {
      executeQuery(q);
    }
  }

  @Override
  public Result execute(String query) {
    //Parse to AST
    List<Query> tests = parser.graqlToAST(query);
    //ASSERT length(tests) == 1;
    if (tests.size() == 0) {
      System.out.println("No query found (parsing error?)");
    }
    Result r = new Result(new ArrayList<>());
    for (Query test : tests) {
      if (DebugHelper.VERBOSE_AST) {
        System.out.println("\u001b[35;1mAST:\u001b[0m");
        DebugHelper.printObjectTree(test);
      }
      if (DebugHelper.VERBOSE_AST) {
        System.out.println("\u001b[35;1mDATALOG:\u001b[0m");
      }
      List<Map<Variable, String>> resultMaps = executeQuery(test);
      List<Map<String, String>> toRet = new ArrayList<>();
      if (resultMaps != null) {
        for (Map<Variable, String> resultMap : resultMaps) {
          Map<String, String> newMap = new HashMap<>();
          for (Entry<Variable, String> result : resultMap.entrySet()) {
            newMap.put(result.getKey().getIdentifier(), result.getValue());
          }
          toRet.add(newMap);
        }
      }
      r = new Result(toRet);
      if (DebugHelper.VERBOSE_RESULTS) {
        r.print();
      }

    }
    return r;
  }

  public static void main(String[] args) throws Exception {
    Workbench.interactive_datalog();
  }

}
