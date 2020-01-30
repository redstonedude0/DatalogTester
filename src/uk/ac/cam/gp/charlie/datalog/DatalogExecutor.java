package uk.ac.cam.gp.charlie.datalog;

import abcdatalog.ast.Clause;
import abcdatalog.engine.DatalogEngine;
import abcdatalog.ast.PositiveAtom;
import abcdatalog.parser.DatalogTokenizer;
import abcdatalog.parser.DatalogParser;
import abcdatalog.ast.PredicateSym;
import abcdatalog.parser.DatalogParseException;
import abcdatalog.ast.validation.DatalogValidationException;

import abcdatalog.engine.topdown.RecursiveQsqEngine;
import java.io.StringReader;
import java.util.Set;
import uk.ac.cam.gp.charlie.Executor;
import uk.ac.cam.gp.charlie.Result;
import uk.ac.cam.gp.charlie.Test;
import uk.ac.cam.gp.charlie.TestEnvironment;
import uk.ac.cam.gp.charlie.datalog.interpreter.Context;
import uk.ac.cam.gp.charlie.datalog.interpreter.GraqlInterpreter;

public class DatalogExecutor extends Executor {

  private DatalogEngine engine;
  private Context c;

  public DatalogExecutor(TestEnvironment environment) {
    try {
      engine = new RecursiveQsqEngine();
      c = GraqlInterpreter.toContext(environment);
      String datalog = GraqlInterpreter.toDatalog(c);
      DatalogTokenizer to = new DatalogTokenizer(new StringReader(datalog));
      Set<Clause> ast = DatalogParser.parseProgram(to);
      engine.init(ast);
    } catch (DatalogParseException|DatalogValidationException e) {
      e.printStackTrace();
    }
  }

  @Override
  public Result execute(Test t) {
    try {
      String test = GraqlInterpreter.toDatalog(t.query,c);
      DatalogTokenizer to = new DatalogTokenizer(new StringReader(test));
      while (to.hasNext()) {
        Set<PositiveAtom> atoms = engine.query(DatalogParser.parseClauseAsPositiveAtom(to));
        System.out.println("Queried:");
        System.out.println(atoms);
      }
    } catch (DatalogParseException e) {
      System.err.println("Error during execution");
      e.printStackTrace();
    }
    return null;
  }

  public static void main(String[] args) {
    //FOR TESTING ONLY!!!!! DELETE AFTER
  }
}
