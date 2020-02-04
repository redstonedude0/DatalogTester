package uk.ac.cam.gp.charlie.datalog;

import abcdatalog.ast.Clause;
import abcdatalog.ast.PositiveAtom;
import abcdatalog.ast.validation.DatalogValidationException;
import abcdatalog.engine.DatalogEngine;
import abcdatalog.engine.bottomup.sequential.SemiNaiveEngine;
import abcdatalog.parser.DatalogParseException;
import abcdatalog.parser.DatalogParser;
import abcdatalog.parser.DatalogTokenizer;
import java.io.File;
import java.io.StringReader;
import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import uk.ac.cam.gp.charlie.DebugHelper;
import uk.ac.cam.gp.charlie.Executor;
import uk.ac.cam.gp.charlie.Result;
import uk.ac.cam.gp.charlie.TestEnvironment;
import uk.ac.cam.gp.charlie.TestLoader;
import uk.ac.cam.gp.charlie.ast.Variable;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefine;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsert;
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
  private Map<Variable, String> executeQuery(Query q) {
    /*
      This function will take the query, convert it to datalog and put in in to the context datalog clause set.
      It will use and modify variables in the context to properly store the current thing ids, etc. It will
      create relevant pointers into the clause set to allow deletion to work properly.
    */
    if (q instanceof QueryDefine) {//defining
      c.datalog.addAll(ASTInterpreter.toDatalog(q, c));
    } else if (q instanceof QueryInsert) {//inserting
      c.datalog.addAll(ASTInterpreter.toDatalog(q, c));
    } else if (false /*query match*/) {
      //engine = new RecursiveQsqEngine(); //doesn't allow disunification
      engine = new SemiNaiveEngine(); //allows disunification
      //engine.init(clauses);
      //Tokenize, parse, and query(execute)
      //DatalogTokenizer to = new DatalogTokenizer(new StringReader(test[1]));
      //while (to.hasNext()) {
      //  Set<PositiveAtom> atoms = engine.query(DatalogParser.parseClauseAsPositiveAtom(to));
      //  //TODO - for testing we are just printing results, later this will be returned as a Result obj.
      //  //Note: will need to post-process the objects using lookups (+ queries?)
      //  System.out.println("Queried:");
      //  System.out.println(atoms);
      //}
    } else {
      throw new RuntimeException("Unsupported query type during datalog query execution");
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
    Map<Variable, String> resultMap = executeQuery(tests.get(0));
    //TODO parse result map into Result obj using Context thing->const maps

    return null;
  }

  public static void main(String[] args) {
    //FOR TESTING ONLY!!!!! DELETE AFTER

    if (false) {
      //Test from raw datalog file
      TestLoader.runTestsFromFile(DatalogExecutor.class, new File("tests/datalog2.test"));
    }
    if (false) {
      //Test AST->datalog
      DatalogExecutor de = new DatalogExecutor(new TestEnvironment("test_schema", "test_data"));
    }
    if (true) {
      //Test graql->AST
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
      System.out.println("EOT");
    }
    if (false) {
      //Test graql->AST->datalog
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
      String data_string = "";
      DatalogExecutor de = new DatalogExecutor(new TestEnvironment(schema_string,"test_data"));
    }
  }
}
