package uk.ac.cam.gp.charlie;

import abcdatalog.ast.PositiveAtom;
import abcdatalog.ast.Term;
import abcdatalog.ast.validation.DatalogValidationException;
import abcdatalog.engine.DatalogEngine;
import abcdatalog.engine.bottomup.sequential.SemiNaiveEngine;
import abcdatalog.parser.DatalogParseException;
import abcdatalog.parser.DatalogParser;
import abcdatalog.parser.DatalogTokenizer;
import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.ConstantValue;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.Variable;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.ast.queries.match.ConditionIsa;
import uk.ac.cam.gp.charlie.ast.queries.match.QueryMatch;
import uk.ac.cam.gp.charlie.datalog.DatalogExecutor;
import uk.ac.cam.gp.charlie.datalog.tests.TESTAstInterpreter;
import uk.ac.cam.gp.charlie.graql.GraqlExecutor;
import uk.ac.cam.gp.charlie.graql.parsing.GraqlParser;
import uk.ac.cam.gp.charlie.graql.parsing.RegexParser;

/**
 * Main file for running the interactive user workbench.
 */
public class Workbench {

  public static void main(String[] args)
      throws Exception {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      System.out.println("##################################");
      System.out.println("# GraQL Basic Test Workbench CLI #");
      System.out.println("##################################");
      System.out.println("Select Task:");
      System.out.println("  Interactive Interface:");
      System.out.println("    (1) Interactive Graql->AST->Datalog input [ONLY SCHEMA]");
      System.out.println("    (2) Interactive Graql->Datalog input w/ preloaded AST");
      System.out
          .println("    (3) Interactive Graql/Datalog Testing w/ result comparison [TO BE ADDED]");
      System.out.println("  Static Tests:");
      System.out.println("    (4) Run TESTASTInterpreter for JUnit tests of AST->Datalog");
      System.out.println("    (5) Run raw datalog from datalog.test");
      System.out.println("    (6) Run stock Graql->AST test");
      System.out.println("    (7) Run GRAKN graql test");
      System.out.println("    [8] Interactive Datalog Interface");

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
        case "7":
          static_graknconnection();
          break;
        case "8":
          interactive_datalog();
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
    for (Query q : data) {
      Method m = de.getClass().getDeclaredMethod("executeQuery", Query.class);
      m.setAccessible(true);
      m.invoke(de, q);
    }
    DatalogExecutor.parser = new RegexParser();
    Map<String, Query> shortcuts = new HashMap();
    shortcuts.put("match_names", query_matchNames());
    shortcuts.put("match_del_alice", query_matchDelAlice());
    shortcuts.put("match_del_alice_e", query_matchDelAliceEmployment());
    interactiveLoop(de, shortcuts);
    DebugHelper.VERBOSE_DATALOG = false;
    DebugHelper.VERBOSE_RESULTS = false;

  }

  public static void interactive_datalog()
      throws Exception {
    //TODO: does not work for now as graqlparser does not parse properly
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    TestEnvironment te = new TestEnvironment("", "");
    DatalogEngine de = new SemiNaiveEngine();
    String init = "";
    init += "isa(e_1,person).";
    init += "isa(e_2,person).";
    init += "isa(e_3,person).";
    init += "isa(e_1,e_1,friendship).";
    init += "isa(e_2,e_1,friendship).";
    init += "isa(e_3,e_1,friendship).";
    init += "isa(e_1,e_2,friendship).";
    init += "isa(e_2,e_2,friendship).";
    init += "isa(e_3,e_2,friendship).";
    init += "isa(e_1,e_3,friendship).";
    init += "isa(e_2,e_3,friendship).";
    init += "isa(e_3,e_3,friendship).";
//    init += "friends(X,Y) :- isa(X,person), isa(Y,person).";
//    String query = "friends(X,Y).";
//    init += "agg(C,A,B) :- C = dl_concat(A,B).";
//    init += "isa(D,friends) :- D = .";
//    String query = "isa(D,friends).";
//    init += "isa(e_1,e_2,employment).";
//    String query = "isa.";
    init += "query1(X,Y) :- isa(X,person), isa(Y,person).";
    init += "inconsistent(rule_1) :- query1(Y,Z), not isa(Y,Z,friendship).";
//    init += "isa(e_4,person).";
    String query = "inconsistent(X).";
    de.init(DatalogParser.parseProgram(new DatalogTokenizer(new StringReader(init))));
    Set<PositiveAtom> b = de.query(
        DatalogParser.parseClauseAsPositiveAtom(new DatalogTokenizer(new StringReader(query))));
    System.out.println(b);
  }

  public static void interactive_basic()
      throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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

  private static void interactiveLoop(DatalogExecutor de)
      throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    interactiveLoop(de, new HashMap<>());
  }

  private static void interactiveLoop(DatalogExecutor de, Map<String, Query> shortcuts)
      throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    System.out.println("Enter graql query to be completed, terminate with blank line to execute");
    System.out.println("Enter \"exit\" to exit to menu");
    for (String shortcut : shortcuts.keySet()) {
      System.out.println("Enter \"" + shortcut + "\" as a shortcut");
    }
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    loop:
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
      for (String shortcut : shortcuts.keySet()) {
        if (input.equals(shortcut + "\n\n")) {
          GraqlParser overload = new GraqlParser() {
            @Override
            public List<Query> graqlToAST(String query) {
              return Lists.asList(shortcuts.get(shortcut), new Query[0]);
            }
          };
          GraqlParser oldParse = DatalogExecutor.parser;
          DatalogExecutor.parser = overload;
          de.execute("");
          DatalogExecutor.parser = oldParse;
          continue loop;
        }
      }
      de.execute(input);
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

  public static void static_graknconnection() throws InterruptedException {
    Function<String, String> readFile = filePath -> {
      // inspired by https://howtodoinjava.com/java/io/java-read-file-to-string-examples/
      StringBuilder builder = new StringBuilder();
      try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
        stream.forEach(s -> builder.append(s).append("\n"));
      } catch (IOException e) {
        e.printStackTrace();
      }
      return builder.toString();
    };

    TestEnvironment env = new TestEnvironment(readFile.apply("social_network_schema.gql"),
        readFile.apply("social_network_data.gql"));
    GraqlExecutor graqlExecutor = new GraqlExecutor(env);
    Result result = graqlExecutor.execute(readFile.apply("social_network_queries.gql"));
    result.print();
    Thread.sleep(1000);
  }

  private static Query query_matchNames() {
    Variable var_n = Variable.fromIdentifier("n");
    Variable var_x = Variable.fromIdentifier("x");
    QueryMatch matchget = new QueryMatch();
    matchget.setActionGet(Lists.asList(var_n, new Variable[0]));
    ConditionIsa cond = new ConditionIsa(var_x, "person");
    cond.has.put(Attribute.fromIdentifier("name"), var_n);
    matchget.conditions.add(cond);
    return matchget;
  }

  private static Query query_matchDelAliceEmployment() {
    Variable var_y = Variable.fromIdentifier("y");
    Variable var_x = Variable.fromIdentifier("x");
    QueryMatch matchdel = new QueryMatch();
    matchdel.setActionDelete(var_y);
    ConditionIsa cond_1 = new ConditionIsa(var_x, "person");
    cond_1.has.put(Attribute.fromIdentifier("name"), ConstantValue.fromValue("Alice"));
    ConditionIsa cond_2 = new ConditionIsa(var_y, "employment");
    cond_2.relates.add(new SimpleEntry<>(Plays.fromIdentifier("employee"), var_x));
    matchdel.conditions.add(cond_1);
    matchdel.conditions.add(cond_2);
    return matchdel;
  }

  private static Query query_matchDelAlice() {
    Variable var_x = Variable.fromIdentifier("x");
    QueryMatch matchdel = new QueryMatch();
    matchdel.setActionDelete(var_x);
    ConditionIsa cond = new ConditionIsa(var_x, "person");
    cond.has.put(Attribute.fromIdentifier("name"), ConstantValue.fromValue("Alice"));
    matchdel.conditions.add(cond);
    return matchdel;
  }


}
