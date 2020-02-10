package uk.ac.cam.gp.charlie;

import abcdatalog.ast.PositiveAtom;
import abcdatalog.ast.Term;
import abcdatalog.ast.validation.DatalogValidationException;
import abcdatalog.engine.DatalogEngine;
import abcdatalog.engine.bottomup.sequential.SemiNaiveEngine;
import abcdatalog.parser.DatalogParseException;
import abcdatalog.parser.DatalogParser;
import abcdatalog.parser.DatalogTokenizer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.datalog.DatalogExecutor;
import uk.ac.cam.gp.charlie.datalog.tests.TESTAstInterpreter;
import uk.ac.cam.gp.charlie.graql.parsing.GraqlParser;
import uk.ac.cam.gp.charlie.graql.parsing.RegexParser;

/**
 * Main file for running the interactive user workbench.
 */
public class Workbench {

  public static void main(String[] args)
      throws IOException, DatalogParseException, DatalogValidationException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      System.out.println("##################################");
      System.out.println("# GraQL Basic Test Workbench CLI #");
      System.out.println("##################################");
      System.out.println("Select Task:");
      System.out.println("  Interactive Interface:");
      System.out.println("    (1) Interactive Graql->AST->Datalog input [ONLY SCHEMA]");
      System.out.println("    (2) Interactive Graql->Datalog input w/ preloaded AST");
      System.out.println("    (3) Interactive Graql/Datalog Testing w/ result comparison [TO BE ADDED]");
      System.out.println("  Static Tests:");
      System.out.println("    (4) Run TESTASTInterpreter for JUnit tests of AST->Datalog");
      System.out.println("    (5) Run raw datalog from datalog.test");
      System.out.println("    (6) Run stock Graql->AST test");

      String input = br.readLine();
      switch (input) {
        case "1":
          interactive_basic();
          break;
        case "2":
          interactive_datalogPreloaded();
          break;
        case "3":
          System.out.println("Unimplemented.");
          break;
        case "4":
          static_TESTASTInterpreter();
          break;
        case "5":
          static_datalogFile();
          break;
        case "6":
          static_graqlAST();
          break;
        default:
          System.out.println("Unknown input " + input);
      }
    }
  }

  public static void interactive_datalogPreloaded()
      throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    //TODO: does not work for now as graqlparser does not parse properly
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    TestEnvironment te = new TestEnvironment("", "");
    DatalogExecutor de = new DatalogExecutor(te);
    DebugHelper.VERBOSE_DATALOG = true;
    DebugHelper.VERBOSE_RESULTS = true;
    DatalogExecutor.parser = new GraqlParser();
    List<Query> data = TESTAstInterpreter.getTestEnv1();
    for (Query q: data) {
      Method m = de.getClass().getDeclaredMethod("executeQuery", Query.class);
      m.setAccessible(true);
      m.invoke(de,q);
    }
    DatalogExecutor.parser = new RegexParser();
//    Map<String,Query> shortcuts = new HashMap();
    interactiveLoop(de);
    DebugHelper.VERBOSE_DATALOG = false;
    DebugHelper.VERBOSE_RESULTS = false;

  }


  public static void interactive_basic() throws IOException {
    //TODO: does not work for now as graqlparser does not parse properly
    TestEnvironment te = new TestEnvironment("\n", "\n");
    DatalogExecutor de = new DatalogExecutor(te);
    DatalogExecutor.parser = new RegexParser();
    DebugHelper.VERBOSE_AST = true;
    DebugHelper.VERBOSE_DATALOG = true;
    DebugHelper.VERBOSE_RESULTS = true;
    interactiveLoop(de);
    DatalogExecutor.parser = new GraqlParser();
    DebugHelper.VERBOSE_AST = false;
    DebugHelper.VERBOSE_DATALOG = false;
    DebugHelper.VERBOSE_RESULTS = false;
  }

  private static void interactiveLoop(DatalogExecutor de) throws IOException {
    System.out.println("Enter graql query to be completed, terminate with blank line to execute");
    System.out.println("Enter \"exit\" to exit to menu");
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      String input = "";
      while (true) {
        String line = br.readLine();
        input += line + "\n";
        if (line.equals("")) {
          break;
        }
      }
      if (input.equals("exit\n\n")) {
        break;
      }
      Result r = de.execute(input);
      System.out.println("Result");
      System.out.println(r);
    }
  }

  public static void static_datalogFile()
      throws IOException, DatalogParseException, DatalogValidationException {
    File f = new File("tests/datalog.test");
    BufferedReader br = new BufferedReader(new FileReader(f));
    String line;
    StringBuilder schema = new StringBuilder();
    while ((line = br.readLine()) != null) {
      schema.append(line);
    }
    DatalogEngine engine = new SemiNaiveEngine(); //allows disunification
    System.out.println("Executing test datalog:");
    engine.init(
        DatalogParser.parseProgram(new DatalogTokenizer(new StringReader(schema.toString()))));
    String query = "query(X,Y).";
    Set<PositiveAtom> res = engine.query(
        DatalogParser.parseClauseAsPositiveAtom(new DatalogTokenizer(new StringReader(query))));
    for (PositiveAtom pa : res) {
      System.out.println(pa.getPred());
      for (Term t : pa.getArgs()) {
        System.out.println(t.toString());
      }
    }
    System.out.println(res);
  }

  public static void static_TESTASTInterpreter() {
    JUnitCore junit = new JUnitCore();
    junit.addListener(new TextListener(System.out));
    junit.run(TESTAstInterpreter.class);
  }


  public static void static_graqlAST() {
    //Test Graql->AST
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
    List<Query> ast = DatalogExecutor.parser.graqlToAST(schema_string);
    System.out.print("AST:");
    DebugHelper.printObjectTree(ast);
    System.out.println("EOT");
  }


}
