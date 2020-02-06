package uk.ac.cam.gp.charlie.datalog;

import abcdatalog.ast.Clause;
import abcdatalog.ast.PositiveAtom;
import abcdatalog.ast.Term;
import abcdatalog.ast.validation.DatalogValidationException;
import abcdatalog.engine.DatalogEngine;
import abcdatalog.engine.bottomup.sequential.SemiNaiveEngine;
import abcdatalog.parser.DatalogParseException;
import abcdatalog.parser.DatalogParser;
import abcdatalog.parser.DatalogTokenizer;
import abcdatalog.util.substitution.Substitution;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
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
import uk.ac.cam.gp.charlie.TestLoader;
import uk.ac.cam.gp.charlie.ast.Variable;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefine;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsert;
import uk.ac.cam.gp.charlie.ast.queries.match.QueryMatch;
import uk.ac.cam.gp.charlie.datalog.interpreter.ASTInterpreter;
import uk.ac.cam.gp.charlie.datalog.interpreter.Context;
import uk.ac.cam.gp.charlie.graql.GraqlParser;

public class DatalogExecutor extends Executor {

  private DatalogEngine engine; //The engine to use for datalog interpretation
  private Context c = new Context(); //The context of the test environment, contains the ASTs representing the environment

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
        System.out.println("\u001b[33;1m<EXECUTING>\u001b[0m");
        Set<Clause> clauses = new HashSet<>(c.datalog);
        clauses.addAll(ASTInterpreter.toDatalog(q,c));
        engine.init(clauses);

        //Tokenize, parse, and query(execute)
        System.out.print("\u001b[33;1mResults of \u001b[0m");
        PositiveAtom query = ASTInterpreter.toExecutableDatalog(q, c);
        Set<PositiveAtom> results = engine.query(query);
        System.out.println(results);
        System.out.println("\u001b[33;1m</EXECUTING>\u001b[0m\n");

        switch (((QueryMatch) q).getAction()) {
          case GET:
            System.out.println("\u001b[33;1mResults of query:\u001b[0m");
            List<Map<Variable,String>> toRet = new ArrayList<>();
            for (PositiveAtom result : results) {
              System.out.println("Match:");
              Map<Variable,String> resultMap = new HashMap<>();
              //Get variables
              Substitution s = result.unify(query);
              for (int i = 0; i <= c.getMaxVariableNumber(); i++) {
                Variable v = c.getVariableByNumber(i);
                String boundValue = s.get(abcdatalog.ast.Variable.create("Var"+i)).toString();
                //add to result map
                Integer boundInt = Integer.parseInt(boundValue.split("_")[1]);
                if (boundValue.startsWith("e_")) {
                  //'Thing' -> not stored in resultMap
                  System.out.println(" _$" + v.getIdentifier()+ " => \u001b[35m{"+boundInt+"}\u001b[0m");
                } else if (boundValue.startsWith("const_")) {
                  resultMap.put(v,c.getConstantFromID(boundInt).value+"");
                  System.out.println("  $" + v.getIdentifier()+ " => \u001b[33m"+c.getConstantFromID(boundInt).value+"\u001b[0m");
                } else {
                  throw new RuntimeException("Invalid returned object (unimplemented): " + boundValue);
                }
              }
              toRet.add(resultMap);
            }
            return toRet;
          case DELETE:
            throw new RuntimeException("unimplemented for now");
//            break;
          case INSERT:
            //Insert as new inserts
            System.out.println("\u001b[33;1m<RESULTS>\u001b[0m");
            for (PositiveAtom result : results) {
              //Get variables
              Substitution s = result.unify(query);
              Set<Variable> variables = new HashSet<>();
              for (int i = 0; i <= c.getMaxVariableNumber(); i++) {
                Variable v = c.getVariableByNumber(i);
                String boundValue = s.get(abcdatalog.ast.Variable.create("Var"+i)).toString();
                Integer boundInt = Integer.parseInt(boundValue.substring(2));
                if (!boundValue.equals("e_"+boundInt)) {
                  throw new RuntimeException("Invalid returned object (unimplemented): " + boundValue);
                }
                //bind scope
                c.addToScope(v,boundInt);
                variables.add(v);
              }
              //insert
              executeQuery(((QueryMatch) q).getDATA_INSERT());
              //unbind scope
              for (Variable v : variables) {
                c.removeFromScope(v);
              }
            }
            System.out.println("\u001b[33;1m</RESULTS>\u001b[0m\n");
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
    List<Query> schema = GraqlParser.graqlToAST(environment.schema);
    List<Query> data = GraqlParser.graqlToAST(environment.data);

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
    List<Query> tests = GraqlParser.graqlToAST(query);
    //ASSERT length(tests) == 1;
    if (tests.size() != 1) {
      //TODO note: having multiple here may be fine, I've restricted to just 1 for safety
      throw new InvalidParameterException(
          "Test was " + tests.size() + " queries, expected 1: " + query);
    }
    List<Map<Variable, String>> resultMaps = executeQuery(tests.get(0));
    List<Map<String,String>> toRet = new ArrayList<>();
    for(Map<Variable,String> resultMap: resultMaps) {
      Map<String,String> newMap = new HashMap<>();
      for (Entry<Variable,String> result: resultMap.entrySet()) {
        newMap.put("$"+result.getKey().getIdentifier(),result.getValue());
      }
      toRet.add(newMap);
    }
    return new Result(toRet);
    //TODO parse result map into Result obj using Context thing->const maps

  }

  public static void main(String[] args) {
    try {
      //FOR TESTING ONLY!!!!! DELETE AFTER

      if (false) {//Test from raw datalog file (deprecated?)
        File f = new File("tests/datalog2.test");
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line;
        StringBuilder schema = new StringBuilder();
        while ((line = br.readLine()) != null) {
          schema.append(line);
        }
        DatalogEngine engine = new SemiNaiveEngine(); //allows disunification
        System.out.println("Executing test datalog:");
        engine.init(DatalogParser.parseProgram(new DatalogTokenizer(new StringReader(schema.toString()))));
        String query = "query(X,Y).";
        Set<PositiveAtom> res = engine.query(DatalogParser.parseClauseAsPositiveAtom(new DatalogTokenizer(new StringReader(query))));
        for(PositiveAtom pa : res) {
          System.out.println(pa.getPred());
          for(Term t : pa.getArgs()) {
            System.out.println(t.toString());
          }
        }
        System.out.println(res);

      }
      if (false) {//Test AST->datalog
        DatalogExecutor de = new DatalogExecutor(new TestEnvironment("test_schema", "test_data"));
      }
      if (true) {//Test graql->AST
        String schema_string = "define\n"
            + "person sub entity,\n"
            + "  has name,\n"
            + "  plays employee;"
            + "organisation sub entity,\n"
            + "  has name,\n"
            + "  plays employer;"
            + "employment sub relation,\n"
            + "  relates employee,\n"
            + "  relates employer;";
        List<Query> ast = GraqlParser.graqlToAST(schema_string);
        System.out.print("AST:");
        DebugHelper.printObjectTree(ast);
//        final String ANSI_CLS = "\u001b[2J";
//        final String ANSI_HOME = "\u001b5n";
//        System.out.print(ANSI_CLS + ANSI_HOME);
//        System.out.flush();
        System.out.println("EOT");
      }
      if (false) {//Test graql->AST->datalog
        DatalogExecutor de = new DatalogExecutor(new TestEnvironment("test_schema", "test_data"));
        System.out.println("Coloring key:");
        System.out.println("\u001b[31mtypes                     red\u001b[0m");
        System.out.println("\u001b[32mroles(plays)              green\u001b[0m");
        System.out.println("\u001b[33mconstants                 yellow\u001b[0m");
        System.out.println("\u001b[34mattribute name            blue\u001b[0m");
        System.out.println("\u001b[35mthing(instance/node)      magenta\u001b[0m");
      }
    } catch (IOException | DatalogParseException | DatalogValidationException e) {
      e.printStackTrace();
    }

  }
}
