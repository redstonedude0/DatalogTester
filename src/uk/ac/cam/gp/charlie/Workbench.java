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
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import uk.ac.cam.gp.charlie.ast.queries.QueryDefineRule;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsert;
import uk.ac.cam.gp.charlie.ast.queries.match.ConditionIsa;
import uk.ac.cam.gp.charlie.ast.queries.match.QueryMatch;
import uk.ac.cam.gp.charlie.datalog.DatalogExecutor;
import uk.ac.cam.gp.charlie.datalog.tests.TESTAstInterpreter;
import uk.ac.cam.gp.charlie.graql.GraqlExecutor;
import uk.ac.cam.gp.charlie.graql.parsing.GraqlParser;

/**
 * Main file for running the interactive user workbench.
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
      System.out.println("    [9] Interactive Comparison Tests");

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
        case "9":
          interactive_comparison();
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
    String init = "";
    init += "instanceof(e_0,employment).";
    init += "instanceof(e_1,person).";
    init += "instanceof(e_2,person).";
    init += "t_playsrole(t_1,employee).";
    init += "t_playsrole(t_1,employer).";
    init += "instancerel(e_0,e_1,employee).";//e1,e2 are employees
    init += "instancerel(e_0,e_1,employer).";
    init += "test(a).";
    init += "ground(P1,R1,W1,W2) :- instanceof(P1,W1), t_playsrole(W2,R1).";
    init += "ground(P1,R1) :- ground(P1,R1,_,_).";
    init += "disjoint(P1,R1,P2,R2) :- ground(P1,R1), ground(P2,R2), P1 != P2.";
    init += "disjoint(P1,R1,P2,R2) :- ground(P1,R1), ground(P2,R2), R1 != R2.";
    init += "query(P1,P2,P3,P4) :- \n"
        + "                  instancerel(X,P1,P3),\n"
        + "                  instancerel(X,P2,P4),\n"
        + "                  disjoint(P1,P3,P2,P4),  \n"
        + "                  disjoint(P2,P4,P1,P3).  \n"
//        + "                        instanceof (Z,employment),\n"
//        + "                        instancerel(Z,P2,employee),\n"
//        + "                        instancerel(Z,Y ,employer),\n"
//        + "                        P1 != P2,\n"
//        + "                        not invariant_1_inv(P1,P2).\n"
//        + "invariant_1_inv(P1,P2) :-  instanceof(W,coworkers),\n"
//        + "                          instancerel(W,P1,employee),\n"
//        + "                          instancerel(W,P2,employee).";
    ;
    String query = "query(P1,P2,P3,P4).";
    de.init(DatalogParser.parseProgram(new DatalogTokenizer(new StringReader(init))));
    Set<PositiveAtom> b = de.query(
        DatalogParser.parseClauseAsPositiveAtom(new DatalogTokenizer(new StringReader(query))));
    System.out.println(b);

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
        System.out.println("Enter graql query to be completed, terminate with blank line to execute");
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
