package uk.ac.cam.gp.charlie.datalog;

import abcdatalog.ast.Clause;
import abcdatalog.ast.PositiveAtom;
import abcdatalog.ast.validation.DatalogValidationException;
import abcdatalog.engine.DatalogEngine;
import abcdatalog.engine.bottomup.sequential.SemiNaiveEngine;
import abcdatalog.parser.DatalogParseException;
import abcdatalog.parser.DatalogParser;
import abcdatalog.parser.DatalogTokenizer;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import uk.ac.cam.gp.charlie.Executor;
import uk.ac.cam.gp.charlie.Result;
import uk.ac.cam.gp.charlie.TestEnvironment;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefine;
import uk.ac.cam.gp.charlie.datalog.interpreter.ASTInterpreter;
import uk.ac.cam.gp.charlie.datalog.interpreter.Context;
import uk.ac.cam.gp.charlie.graql.GraqlParser;

public class DatalogExecutor extends Executor {

  private DatalogEngine engine; //The engine to use for datalog interpretation
  private Context c = new Context(); //The context of the test environment, contains the ASTs representing the environment
  private Set<Clause> datalog_environment;//The datalog environment, derived from c.

  /**
   * Create a DatalogExecutor from a TestEnvironment. Initialises the context and environment
   *
   * @param environment the environment the tests will be run in (schema+data)
   */
  public DatalogExecutor(TestEnvironment environment) {
    try {
      //Convert the Graql environment to a Context
      List<Query> schema = GraqlParser.graqlToAST(environment.schema);
      List<Query> data = GraqlParser.graqlToAST(environment.data);

      c.queryList.addAll(schema);
      c.queryList.addAll(data);
      //TODO remove
      c.TEST_REMOVE = environment.schema + " " + environment.data;

      String datalog = ASTInterpreter.toDatalog(c); //Convert the Context to Datalog

      DatalogTokenizer to = new DatalogTokenizer(
          new StringReader(datalog)); //tokenise and parse the datalog
      datalog_environment = DatalogParser
          .parseProgram(to); //not equal to the graql ASTs stored in Context.
    } catch (DatalogParseException e) {
      e.printStackTrace();
    }
  }

  @Override
  public Result execute(String query) {
    try {
      //Using the context, form the datalog
      String[] test = ASTInterpreter.toDatalog(query, c);
      Set<Clause> clauses = new HashSet<>();
      if (!test[0].equals("")) { //need to re-do engine
        engine = null;
        clauses = DatalogParser.parseProgram(new DatalogTokenizer(new StringReader(test[0])));
      }
      if (engine == null) { //new clauses or 1st time run
        clauses.addAll(datalog_environment);
        //engine = new RecursiveQsqEngine(); //doesn't allow disunification
        engine = new SemiNaiveEngine(); //allows disunification

        engine.init(clauses);
      }

      //Tokenize, parse, and query(execute)
      DatalogTokenizer to = new DatalogTokenizer(new StringReader(test[1]));
      while (to.hasNext()) {
        Set<PositiveAtom> atoms = engine.query(DatalogParser.parseClauseAsPositiveAtom(to));
        //TODO - for testing we are just printing results, later this will be returned as a Result obj.
        //Note: will need to post-process the objects using lookups (+ queries?)
        System.out.println("Queried:");
        System.out.println(atoms);
      }
    } catch (DatalogParseException | DatalogValidationException e) {
      System.err.println("Error during execution");
      e.printStackTrace();
    }
    return null;
  }

  public static void main(String[] args) {
    //FOR TESTING ONLY!!!!! DELETE AFTER
//    TestLoader.runTestsFromFile(DatalogExecutor.class, new File("tests/datalog2.test"));
    DatalogExecutor de = new DatalogExecutor(new TestEnvironment("test_schema","test_data"));
    System.out.println(ASTInterpreter.toDatalog(de.c));

    //    DatalogExecutor de = new DatalogExecutor(new TestEnvironment("",""));
//    de.c = Context.generateExample();
//    de.execute("");
  }
}
