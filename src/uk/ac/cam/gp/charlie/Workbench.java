package uk.ac.cam.gp.charlie;

import abcdatalog.ast.Clause;
import abcdatalog.ast.PositiveAtom;
import abcdatalog.ast.Term;
import abcdatalog.ast.validation.DatalogValidationException;
import abcdatalog.engine.DatalogEngine;
import abcdatalog.engine.bottomup.sequential.SemiNaiveEngine;
import abcdatalog.parser.DatalogParseException;
import abcdatalog.parser.DatalogParser;
import abcdatalog.parser.DatalogTokenizer;
import com.google.common.collect.Lists;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.ConstantValue;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.Variable;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefineRule;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsert;
import uk.ac.cam.gp.charlie.ast.queries.match.ConditionIsa;
import uk.ac.cam.gp.charlie.ast.queries.match.QueryMatch;
import uk.ac.cam.gp.charlie.datalog.DatalogExecutor;
import uk.ac.cam.gp.charlie.datalog.tests.TESTAstInterpreter;
import uk.ac.cam.gp.charlie.graql.GraqlExecutor;
import uk.ac.cam.gp.charlie.graql.parsing.GraqlParser;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

import static uk.ac.cam.gp.charlie.query_generator.QueryGenerator.runRandomTests;

/**
 * Main file for running the interactive user workbench.
 * @author hrjh2@cam.ac.uk
 */
public class Workbench {

  public static void main(String[] args)
      throws Exception {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    //Loop for the main menu
    while (true) {
      System.out.println("##################################");
      System.out.println("# GraQL Basic Test Workbench CLI #");
      System.out.println("##################################");
      System.out.println("Select Task:");
      System.out.println("  Interactive Interface:");
      System.out.println("    (1) Interactive Graql->AST->Datalog");
      System.out.println("    (2) Interactive Graql->Datalog w/ shortcuts");
      System.out.println("    (3) Interactive Graql/Datalog Testing w/ result comparison");
      System.out.println("    (4) Interactive Comparison Tests");
      System.out.println("  Static Tests:");
      System.out.println("    (5) Run Datalog check");
      System.out.println("    (6) Run TESTASTInterpreter for JUnit tests of AST->Datalog");
      System.out.println("    (7) Run raw datalog from datalog.test");
      System.out.println("    (8) Run stock Graql->AST test");
      System.out.println("    (9) Run GRAKN graql test");
      System.out.println("  Random Tests:");
      System.out.println("    (10) Run random tests on Datalog and Graql");

      String input = br.readLine();
      switch (input) {
        case "1":
          interactive_basic();
          break;
        case "2":
          interactive_datalogPreloaded();
          break;
        case "3":
          interactive_live_comparison();
          break;
        case "4":
          interactive_comparison();
          break;
        case "5":
          interactive_datalog();
          break;
        case "6":
          static_TESTASTInterpreter();
          break;
        case "7":
          static_datalogFile();
          break;
        case "8":
          static_graqlAST();
          break;
        case "9":
          static_graknconnection();
          break;
        case "10":
          runRandomTests(0);
          break;
        default:
          System.out.println("Unknown input " + input);
      }
    }
  }

  /**
   * Interactive graql prompt to the datalog parser with some preloaded AST database
   */
  public static void interactive_datalogPreloaded()
      throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    //TODO: does not work for now as graqlparser does not parse properly
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    TestEnvironment te = new TestEnvironment("", "");
    DatalogExecutor de = new DatalogExecutor(te);
    DebugHelper.VERBOSE_DATALOG = true;
    DebugHelper.VERBOSE_RESULTS = true;
    List<Query> data = TESTAstInterpreter.getTestEnv1();
    for (Query q : data) {
      Method m = de.getClass().getDeclaredMethod("executeQuery", Query.class);
      m.setAccessible(true);
      m.invoke(de, q);
    }
    Map<String, Query> astShortcuts = new HashMap();
    astShortcuts.put("\u001b[34mmatch..get\u001b[0m all names", query_matchNames());//GRAQL
    astShortcuts.put("\u001b[34mmatch..delete\u001b[0m Alice from the database", query_matchDelAlice());//GRAQL
    astShortcuts.put("\u001b[34mmatch..delete\u001b[0m unemploy Alice", query_matchDelAliceEmployment());//GRAQL
    astShortcuts.put("\u001b[34mmatch..delete\u001b[0m Bob-Charlie coworkers relation",query_matchDelBC_coworkers());//GRAQL
    astShortcuts.put("\u001b[34mmatch..insert\u001b[0m employ Bob",query_match_employ_b());//GRAQL
    astShortcuts.put("\u001b[34mmatch..insert\u001b[0m employ Charlie",query_match_employ_c());//GRAQL
    Map<String, String> graqlShortcuts = new HashMap();
    graqlShortcuts.put("\u001b[34mmatch..insert\u001b[0m employ Charlie",
        "match\n"
            + "    $x isa person, has name \"Charlie\";\n"
            + "    $y isa organisation, has name \"Uni\";\n"
            + "insert (employee: $x, employer: $y) isa employment;");
    graqlShortcuts.put("\u001b[34mmatch..insert\u001b[0m employ Bob",
        "match\n"
            + "    $x isa person, has name \"Bob\";\n"
            + "    $y isa organisation, has name \"Uni\";\n"
            + "insert (employee: $x, employer: $y) isa employment;");
    graqlShortcuts.put("\u001b[34mmatch..delete\u001b[0m Bob-Charlie coworkers relation",
          "match\n"
        + "    $x (employee: $p1, employee: $p2) isa coworkers;\n"
        + "    $p1 isa person, has name \"Bob\";\n"
        + "    $p2 isa person, has name \"Charlie\";\n"
        + "delete $x;");
    graqlShortcuts.put("\u001b[34mmatch..delete\u001b[0m Alice from the database",
          "match\n"
        + "    $x isa person,\n"
        + "        has name \"Alice\";\n"
        + "delete $x;");
    graqlShortcuts.put("\u001b[34mdefine rule\u001b[0m everyone is friends (reflexive)","define\n rule-a sub rule, when {\n  $x isa person;\n  $y isa person;\n }, then {\n  (friend:$x,friend:$y) isa friends;\n };");
    graqlShortcuts.put("\u001b[34mdefine rule\u001b[0m to make people coworkers",
          "define\n"
        + " people-with-same-workplace-are-coworkers sub rule, when {\n"
        + "  (employer: $y, employee: $p1) isa employment;\n"
        + "  (employer: $y, employee: $p2) isa employment;\n"
        + "  $p1 != $p2;\n"
        + " }, then {\n"
        + "  (employee: $p1, employee: $p2) isa coworkers;\n"
        + " };\n");
    graqlShortcuts.put("\u001b[34mmatch..delete\u001b[0m unemploy Alice",
          "match\n"
        + "    $x isa person, has name \"Alice\";\n"
        + "    $y (employee: $x) isa employment;\n"
        + "delete $y;");
    graqlShortcuts.put("\u001b[34mmatch..get\u001b[0m all names",
        "match\n"
            + "    $x isa person,\n"
            + "        has name $n;\n"
            + "get $n;");

    interactiveLoop(de, astShortcuts,graqlShortcuts,false);
    DebugHelper.VERBOSE_DATALOG = false;
    DebugHelper.VERBOSE_RESULTS = false;

  }

  /**
   * Basic prompt to execute datalog, not particularly interactive atm, largely used for debugging
   */
  public static void interactive_datalog()
      throws Exception {
    //TODO: does not work for now as graqlparser does not parse properly
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    TestEnvironment te = new TestEnvironment("", "");
    DatalogEngine de = new SemiNaiveEngine();
    String init = "instanceattr(e_3, a_0, const_3).\n"
        + "t_subs(t_5, usflxmubia).\n"
        + "instanceattr(e_2, a_0, const_2).\n"
        + "t_hasattr(t_4, a_0).\n"
        + "instanceattr(e_14, a_0, const_14).\n"
        + "instanceattr(e_6, a_0, const_6).\n"
        + "t_subs(t_6, relation).\n"
        + "t_subs(t_1, entity).\n"
        + "instanceattr(e_0, a_0, const_0).\n"
        + "t_subs(t_7, relation).\n"
        + "instanceof(e_17, t_7).\n"
        + "instanceof(e_16, t_6).\n"
        + "instancerel(e_15, e_14, r_0).\n"
        + "instancerel(e_16, e_12, r_0).\n"
        + "instanceof(e_13, t_5).\n"
        + "t_subs(t_8, relation).\n"
        + "instancerel(e_19, e_12, r_0).\n"
        + "instanceof(e_18, t_8).\n"
        + "instanceattr(e_12, a_0, const_12).\n"
        + "instancerel(e_18, e_14, r_0).\n"
        + "instancerel(e_20, e_13, r_0).\n"
        + "instanceof(e_5, t_2).\n"
        + "instanceattr(e_5, a_0, const_5).\n"
        + "instanceattr(e_7, a_0, const_7).\n"
        + "t_subs(relation, concept).\n"
        + "instanceattr(e_10, a_0, const_10).\n"
        + "ground(E1, R1, W1, W2, W3) :- instanceof(E1, W1), instancerel(W2, W3, R1).\n"
        + "instanceof(e_10, t_4).\n"
        + "t_hasattr(t_1, a_0).\n"
        + "instanceof(Concept, Supertype) :- instanceof(Concept, Subtype), t_subs(Subtype, Supertype).\n"
        + "instanceof(e_8, t_3).\n"
        + "instanceattr(e_9, a_0, const_9).\n"
        + "instancerel(e_15, e_1, r_1).\n"
        + "instanceof(e_20, t_8).\n"
        + "instancerel(e_20, e_6, r_2).\n"
        + "t_subs(entity, concept).\n"
        + "instanceof(e_12, t_5).\n"
        + "instanceattr(e_8, a_0, const_8).\n"
        + "instanceof(e_15, t_6).\n"
        + "t_subs(rule, concept).\n"
        + "t_subs(t_4, entity).\n"
        + "t_hasattr(t_3, a_0).\n"
        + "instanceof(e_11, t_4).\n"
        + "instancerel(e_19, e_8, r_2).\n"
        + "instanceof(e_4, t_2).\n"
        + "instancerel(e_16, e_1, r_1).\n"
        + "instanceof(e_2, t_1).\n"
        + "ground(E1, R1) :- ground(E1, R1, _, _, _).\n"
        + "instanceof(e_1, t_1).\n"
        + "instanceattr(e_11, a_0, const_11).\n"
        + "instanceattr(e_1, a_0, const_1).\n"
        + "instanceattr(e_4, a_0, const_4).\n"
        + "t_subs(t_3, mbjhsbmvpo).\n"
        + "t_subs(t_0, relation).\n"
        + "t_hasattr(t_2, a_0).\n"
        + "disjoint(E1, R1, E2, R2) :- ground(E1, R1), ground(E2, R2), E1 != E2.\n"
        + "instanceof(e_6, t_3).\n"
        + "t_hasattr(t_5, a_0).\n"
        + "instanceof(e_3, t_2).\n"
        + "instanceof(e_19, t_8).\n"
        + "instancerel(e_17, e_0, r_1).\n"
        + "instanceof(e_7, t_3).\n"
        + "instanceof(e_9, t_4).\n"
        + "instancerel(e_17, e_7, r_2).\n"
        + "instancerel(e_18, e_8, r_2).\n"
        + "instanceof(e_14, t_5).\n"
        + "instanceof(e_0, t_1).\n"
        + "instanceattr(e_13, a_0, const_13).\n"
        + "t_subs(t_2, azjwbsgazg).\n"
        + "disjoint(E1, R1, E2, R2) :- ground(E1, R1), ground(E2, R2), R1 != R2."
        + "query(Var0,Var1,Var2,Var3,Var4) :- instanceattr(Var0,a_0,Var1), instanceof(Var2,t_8), instancerel(Var2,Var0,r_2), instancerel(Var2,Var3,r_0), disjoint(Var0,r_2,Var3,r_0), disjoint(Var3,r_0,Var0,r_2), instanceof(Var3,concept), instanceattr(Var3,a_0,Var4).\n";
    String query = "query(Var0,Var1,Var2,Var3,Var4).";
    de.init(DatalogParser.parseProgram(new DatalogTokenizer(new StringReader(init))));
    Set<PositiveAtom> b = de.query(
        DatalogParser.parseClauseAsPositiveAtom(new DatalogTokenizer(new StringReader(query))));
    System.out.println(b);

  }

  public static void interactive_live_comparison()
      throws IOException {
    //TODO: does not work for now as graqlparser does not parse properly
    String schema_datalog = "";
    String schema_graql = "";
    String data_datalog = "";
    String data_graql = "";
    DatalogExecutor de = null;
    GraqlExecutor ge = null;
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    System.out.println("Enter graql query to be compared");
    System.out.println("Start a line with '~' for graql-only parsing");
    System.out.println("Enter \"exit\" to exit to menu");
    System.out.println("Terminate with \"schema\" to append to schema");
    System.out.println("Termiante with \"data\" to append to data");
    System.out.println("Termiante with \"query\" to execute query");
    loop:
    while (true) {
      String input_graql = "";
      String input_datalog = "";
      String command = "";
      while (true) {
        String line = br.readLine();
        if (line.equals("query") || line.equals("schema") || line.equals("data")) {
          command = line;
          break;
        }
        line += "\n";
        if (!line.startsWith("~")) {
          input_datalog += line;
          input_graql += line;
        } else {
          input_graql += line.substring(1);
        }
      }
      //Trim trailing newlines
      input_datalog = input_datalog.replaceFirst("\n++$","");
      input_graql = input_graql.replaceFirst("\n++$","");
      if (input_graql.equals("exit")) {
        break;
      }
      if (command.equals("schema")) {
        schema_graql += input_graql;
        schema_datalog += input_datalog;
        System.out.println("Schema added");
        continue ;
      }
      if (command.equals("data")) {
        data_graql += input_graql;
        data_datalog += input_datalog;
        System.out.println("Data added");
        continue ;
      }
      if (command.equals("query")) {
        if (de == null) {
          de = new DatalogExecutor(new TestEnvironment(schema_datalog, data_datalog));
          ge = new GraqlExecutor(new TestEnvironment(schema_graql, data_graql));
        }
        Result r_d = de.execute(input_datalog);
        Result r_g = ge.execute(input_graql);
        System.out.println("Datalog Results:");
        r_d.print();
        System.out.println("Graql Results:");
        r_g.print();
        if (!r_d.equals(r_g)) {
          System.out.println("\u001b[31mNot Equal\u001b[0m");
        } else {
          System.out.println("\u001b[32mEqual\u001b[0m");
        }
        continue ;
      }
      System.out.println("ERROR - UNKNOWN COMMAND");
    }
  }

  /**
   * Run an interactive graql->datalog prompt with full verbosity
   */
  public static void interactive_basic()
      throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    //TODO: does not work for now as graqlparser does not parse properly
    TestEnvironment te = new TestEnvironment("\n", "\n");
    DatalogExecutor de = new DatalogExecutor(te);
    DebugHelper.VERBOSE_AST = true;
    DebugHelper.VERBOSE_DATALOG = true;
    DebugHelper.VERBOSE_RESULTS = true;
    interactiveLoop(de,new HashMap<>(), new HashMap<>(), true);
    DebugHelper.VERBOSE_AST = false;
    DebugHelper.VERBOSE_DATALOG = false;
    DebugHelper.VERBOSE_RESULTS = false;
  }

  /**
   * Initiate a basic interactive graql console with a specified executor and shortcuts
   * if extendedCommit is true then "\ncommit\n" is required rather than "\n\n" to commit
   * @param de
   */
  private static void interactiveLoop(DatalogExecutor de, Map<String, Query> astShortcuts, Map<String, String> graqlShortcuts, boolean extendedCommit)
      throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    boolean displayPrompt_ast = false;
    boolean displayPrompt_graql = true;
    loop:
    while (true) {
      if (displayPrompt_ast || displayPrompt_graql) {
        if (extendedCommit) {
          System.out
              .println("Enter graql query to be completed, terminate with 'commit' on blank line to execute");
        } else {
          System.out.println("Enter graql query to be completed, terminate with blank line to execute");
        }
        System.out.println("Enter \"exit\" to exit to menu");
        if (displayPrompt_ast) {
          int astNum = 0;
          for (String shortcut : astShortcuts.keySet()) {
            System.out.println(
                "Enter a" + astNum + " for \u001b[34mAST\u001b[0m shortcut \"" + shortcut + "\"");
            astNum++;
          }
        }
        if (displayPrompt_graql) {
          int graqlNum = 0;
          for (String shortcut : graqlShortcuts.keySet()) {
            System.out.println(
                "Enter g" + graqlNum + " for \u001b[34mGraql\u001b[0m shortcut \"" + shortcut
                    + "\"");
            graqlNum++;
          }
        }
        displayPrompt_ast = false;
        displayPrompt_graql = false;
      }
      String input = "";
      while (true) {
        String line = br.readLine();
        if (line.equals("") && !extendedCommit) {
          break;
        }
        if (line.equals("commit") && extendedCommit) {
          break;
        }
        input += line + "\n";
      }
      //Trim trailing newlines
      input = input.replaceFirst("\n++$","");
      if (input.equals("exit")) {
        break;
      }
      if (input.equals("help")) {
        displayPrompt_graql = true;
        continue;
      }
      if (input.equals("help_ast")) {
        displayPrompt_ast = true;
        continue;
      }
      if (input.equals("dump") || input.equals("dump raw")) {
        boolean raw = input.equals("dump raw");
        Set<Clause> clauses = de.c.datalog;
        for (Clause c: clauses) {
          String out = c.toString();
          if (!raw) {
            out = de.c.prettifyDatalog(out);
          }
          System.out.println(out);
        }
        continue;
      }
      if (input.startsWith("a")) {
        int astNum = 0;
        for (String shortcut : astShortcuts.keySet()) {
          if (input.equals("a"+astNum)) {
            DebugHelper.printObjectTree(astShortcuts.get(shortcut));
            br.readLine();
            //inject AST into parser
            GraqlParser overload = new GraqlParser() {
              @Override
              public List<Query> graqlToAST(String query) {
                return Lists.asList(astShortcuts.get(shortcut), new Query[0]);
              }
            };
            GraqlParser oldParse = DatalogExecutor.parser;
            DatalogExecutor.parser = overload;
            de.execute("");
            DatalogExecutor.parser = oldParse;
            continue loop;
          }
          astNum++;
        }
      }
      if (input.startsWith("g")) {
        int graqlNum = 0;
        for (String shortcut : graqlShortcuts.keySet()) {
          if (input.equals("g"+graqlNum)) {
            input = graqlShortcuts.get(shortcut);
            System.out.println("\u001b[34m"+input+"\u001b[0m");
            br.readLine();
          }
          graqlNum++;
        }
      }
      de.execute(input);
    }
  }

  /**
   * Load test file and allow for comparison tests
   * NOTE: Might not like initialising 2 engines :/
   */
  private static void interactive_comparison()
      throws Exception {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    List<Entry<String,File>> files = TestLoader.listFiles();
    loop:
    while (true) {
      System.out.println("Enter a number to run, or 'exit', or 'debug', or 'all'");
      int id = 0;
      for (Entry<String,File> fileEntry : files) {
        System.out.println(id + ") \u001b[34m" + fileEntry.getKey()+ "\u001b[0m");
        id++;
      }
      String input = br.readLine();
      if (input.equals("exit")) {
        break;
      }
      if (input.equals("debug")) {
        DebugHelper.VERBOSE_RESULTS = true;
        DebugHelper.VERBOSE_AST = true;
        DebugHelper.VERBOSE_DATALOG = true;
        DebugHelper.DUMP_DATALOG_ON_RESULT = true;
        continue;
      }
      if (input.equals("all")) {
        List<TestResults> allResults = new ArrayList<>();
        for (Entry<String,File> fileEntry: files) {
          TestResults results = TestLoader.runComparisonTests(fileEntry.getValue());
          allResults.add(results);
        }
        System.out.println("#####################");
        System.out.println("SUMMARY OF ALL TESTS:");
        System.out.println("#####################");
        for (TestResults result: allResults) {
          result.soutAll();
        }
        System.out.println("################");
        System.out.println("COMPACT SUMMARY:");
        System.out.println("################");
        int passed = 0;
        int failed = 0;
        for (TestResults result: allResults) {
          System.out.println(result.getConclusion()+ " - \u001b[36m" + result.testName + "\u001b[0m");
          passed += result.getPassed();
          failed += result.getFailed();
        }
        if (failed == 0) {
          System.out.println("\u001b[32m#################################\u001b[0m");
          System.out.println("\u001b[32m# ALL TESTS IN ALL FILES PASSED #\u001b[0m");
          System.out.println("\u001b[32m#################################\u001b[0m");
        } else {
          System.out.println("\u001b[31m#################################\u001b[0m");
          System.out.println("\u001b[31m#    SOME TESTS DID NOT PASS    #\u001b[0m");
          System.out.println("\u001b[31m#################################\u001b[0m");
          System.out.println("\u001b[31m"+failed+"/"+(passed+failed)+" Tests Failed, these were:\u001b[0m");
          for (TestResults result: allResults) {
            for (String failure: result.failedTests) {
              System.out.println("\u001b[31m- " + failure);
            }
          }
        }
        continue;
      }
      id = 0;
      Integer selected = Integer.parseInt(input);
      for (Entry<String,File> fileEntry: files) {
        if (selected == id) {
          TestLoader.runComparisonTests(fileEntry.getValue());
          break;
        }
        id++;
      }
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
    PrintStream ps = System.out;
    System.setOut(new PrintStream(new ByteArrayOutputStream()));
    GraqlExecutor graqlExecutor = new GraqlExecutor(env);
    Result result = graqlExecutor.execute(readFile.apply("social_network_queries.gql"));
    Thread.sleep(1000);
    System.setOut(ps); //un-absorb output
    result.print();
  }

  private static Query query_matchNames() {
    Variable var_n = Variable.fromIdentifier("n");
    Variable var_x = Variable.fromIdentifier("x");
    QueryMatch matchget = new QueryMatch();
    matchget.setActionGet(Lists.asList(var_n, new Variable[0]));
    ConditionIsa cond = new ConditionIsa(null, "person");
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

  private static Query query_matchDelBC_coworkers() {
    Variable var_x = Variable.fromIdentifier("x");
    Variable var_p2 = Variable.fromIdentifier("p2");
    Variable var_p1 = Variable.fromIdentifier("p1");
    QueryMatch matchdel = new QueryMatch();
    matchdel.setActionDelete(var_x);
    ConditionIsa cond = new ConditionIsa(var_x, "coworkers");
    cond.relates.add(new SimpleEntry<>(Plays.fromIdentifier("employee"),var_p1));
    cond.relates.add(new SimpleEntry<>(Plays.fromIdentifier("employee"),var_p2));
    matchdel.conditions.add(cond);
    ConditionIsa cond2 = new ConditionIsa(var_p1, "person");
    cond2.has.put(Attribute.fromIdentifier("name"),ConstantValue.fromValue("Bob"));
    matchdel.conditions.add(cond2);
    ConditionIsa cond3 = new ConditionIsa(var_p2, "person");
    cond3.has.put(Attribute.fromIdentifier("name"),ConstantValue.fromValue("Charlie"));
    matchdel.conditions.add(cond3);
    return matchdel;
  }

  private static Query query_match_employ_b() {
    Variable var_x = Variable.fromIdentifier("x");
    Variable var_y = Variable.fromIdentifier("y");
    QueryMatch match = new QueryMatch();

    ConditionIsa cond2 = new ConditionIsa(var_x, "person");
    cond2.has.put(Attribute.fromIdentifier("name"),ConstantValue.fromValue("Bob"));
    match.conditions.add(cond2);
    ConditionIsa cond3 = new ConditionIsa(var_y, "organisation");
    cond3.has.put(Attribute.fromIdentifier("name"),ConstantValue.fromValue("Uni"));
    match.conditions.add(cond3);

    QueryInsert insert = new QueryInsert(null,"employment");
    insert.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employee"),var_x));
    insert.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employer"),var_y));
    match.setActionInsert(insert);
    return match;
  }

  private static Query query_match_employ_c() {
    Variable var_x = Variable.fromIdentifier("x");
    Variable var_y = Variable.fromIdentifier("y");
    QueryMatch match = new QueryMatch();

    ConditionIsa cond2 = new ConditionIsa(var_x, "person");
    cond2.has.put(Attribute.fromIdentifier("name"),ConstantValue.fromValue("Charlie"));
    match.conditions.add(cond2);
    ConditionIsa cond3 = new ConditionIsa(var_y, "organisation");
    cond3.has.put(Attribute.fromIdentifier("name"),ConstantValue.fromValue("Uni"));
    match.conditions.add(cond3);

    QueryInsert insert = new QueryInsert(null,"employment");
    insert.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employee"),var_x));
    insert.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employer"),var_y));
    match.setActionInsert(insert);
    return match;
  }

  private static Query query_insertRule_coworkers() {
    /**
     * define people-with-same-workplace-are-coworkers sub rule, when {
     *    (employer: $y, employee: $p1) isa employment;
     *    (employer: $y, employee: $p2) isa employment;
     ***    $p1 != $p2;
     * }, then {
     *    (employee: $p1, employee: $p2) isa coworkers;
     * };
     */;
    Variable var_y = Variable.fromIdentifier("y");
    Variable var_p1 = Variable.fromIdentifier("p1");
    Variable var_p2 = Variable.fromIdentifier("p2");

    QueryDefineRule rule = new QueryDefineRule("people-with-same-workplace-are-coworkers",QueryDefineRule.getFromIdentifier("rule"));
    ConditionIsa cond_1 = new ConditionIsa(null,"employment");
    cond_1.relates.add(new SimpleEntry<>(Plays.fromIdentifier("employer"),var_y));
    cond_1.relates.add(new SimpleEntry<>(Plays.fromIdentifier("employee"),var_p1));
    rule.when.add(cond_1);
    ConditionIsa cond_2 = new ConditionIsa(null,"employment");
    cond_2.relates.add(new SimpleEntry<>(Plays.fromIdentifier("employer"),var_y));
    cond_2.relates.add(new SimpleEntry<>(Plays.fromIdentifier("employee"),var_p2));
    rule.when.add(cond_2);

    rule.then = new QueryInsert(null,"coworkers");
    rule.then.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employee"),var_p1));
    rule.then.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employee"),var_p2));
    return rule;
  }


  /*
  TODO -
  - match..insert and define-rule will add duplicates (e.g. 4-2 and 2-4 are coworkers relations). is that desirable?
   */
}
