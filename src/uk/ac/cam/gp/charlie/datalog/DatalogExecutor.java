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

public class DatalogExecutor extends Executor {

  private DatalogEngine engine;

  public DatalogExecutor() {
  }

  private void resetEngine() {
    engine = new RecursiveQsqEngine();
  }

  @Override
  public Result execute(Test t) {
    try {
      resetEngine();
      String program = "edge(0,1). edge(1,2). tc(X,Y) :- edge(X,Y)."
          + "tc(X,Y) :- edge(X,Z), tc(Z,Y). cycle :- tc(X,X).";
      DatalogTokenizer to = new DatalogTokenizer(new StringReader(program));
      Set<Clause> ast = DatalogParser.parseProgram(to);
      PredicateSym edge = PredicateSym.create("edge", 2);
      PredicateSym tc = PredicateSym.create("tc", 2);
      PredicateSym cycle = PredicateSym.create("cycle", 0);
      engine.init(ast);
      String tests = "tc(0,Y).";
      to = new DatalogTokenizer(new StringReader(tests));
      while (to.hasNext()) {
        Set<PositiveAtom> atoms = engine.query(DatalogParser.parseClauseAsPositiveAtom(to));
        System.out.println("Queried:");
        System.out.println(atoms);
      }
    } catch (DatalogParseException | DatalogValidationException e) {
      System.err.println("Error during execution");
      e.printStackTrace();
      System.exit(0);
    }
    return null;
  }

  public static void main(String[] args) {
    //FOR TESTING ONLY!!!!! DELETE AFTER

    DatalogExecutor de = new DatalogExecutor();
    de.execute(null);
  }
}
