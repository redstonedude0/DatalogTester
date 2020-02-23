package uk.ac.cam.gp.charlie;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import uk.ac.cam.gp.charlie.datalog.DatalogExecutor;
import uk.ac.cam.gp.charlie.graql.GraqlExecutor;

public class TestLoader {

  private static class TestFile {

    TestEnvironment te;
    List<String> tests = new ArrayList<>();
    String name = "Unnamed Test";
  }

  public static TestFile loadTestsFromFile(File f) {
    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
      StringBuilder contents = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        contents.append(line).append("\n");
      }
      TestFile tf = loadTestsFromFileContents(contents.toString());
      tf.name = f.getName();
      return tf;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private static TestFile loadTestsFromFileContents(String fileContents) {
    String[] bits = fileContents.split("###Schema");
    fileContents = bits[1];
    bits = fileContents.split("###Data");
    String schema = bits[0];
    fileContents = bits[1];
    bits = fileContents.split("###Test");
    String data = bits[0];
    String queryBlock = bits[1];
    String[] queries = queryBlock.split("\n");
    TestFile tf = new TestFile();
    tf.te = new TestEnvironment(schema, data);
    for (String query : queries) {
      if (!query.equals("\n") && !query.equals("")) {
        tf.tests.add(query);
      }
    }
    return tf;
  }

  public static List<String> runComparisonTests(File f) {
    return runComparisonTests(loadTestsFromFile(f));
  }

  private static List<String> runComparisonTests(TestFile tf) {
    try (
        DatalogExecutor de = new DatalogExecutor(tf.te);
        GraqlExecutor ge = new GraqlExecutor(tf.te);
    ) {
      boolean passed = true;
      int id = 1;
      List<String> results = new ArrayList<>();
      results.add("\u001b[36m" + tf.name + "\u001b[0m Summary:");
      for (String query : tf.tests) {
        String result;
        try {
          Result dr = de.execute(makeDatalogSafe(query));
          Result gr = ge.execute(makeGraqlSafe(query));
          System.out.println("DL RESULTS:");
          dr.print();
          System.out.println("GRAQL RESULTS:");
          gr.print();
          if (!dr.equals(gr)) {
            throw new RuntimeException("Results not equal");
          }
          result = "Test \u001b[32mPassed\u001b[0m \u001b[36m" + tf.name + "\u001b[0m(" + id + "/"
              + tf.tests.size() + ") - \u001b[35m" + query + "\u001b[0m";
        } catch (Throwable t) {
          System.out.println("Error: " + t.getMessage());
          result = "Test \u001b[31mFailed\u001b[0m \u001b[36m" + tf.name + "\u001b[0m(" + id + "/"
              + tf.tests.size() + ") - \u001b[35m" + query + "\u001b[0m";
          passed = false;
        }
        System.out.println(result);
        results.add("  " + result);
        id++;
      }
      if (passed) {
        results.add("\u001b[32mAll Tests Passed\u001b[0m");
      } else {
        results.add("\u001b[31mSome Tests Failed\u001b[0m");
      }
      for (String result : results) {
        System.out.println(result);
      }
      return results;
    } catch (Exception e) {
      throw new RuntimeException("Error during execution");
    }
  }

  private static String makeDatalogSafe(String query) {
    //Lines starting '~' are graql only
    StringBuilder ret = new StringBuilder();
    String[] parts = query.split("\n");
    for (String s : parts) {
      if (!s.startsWith("~")) {
        ret.append(s).append("\n");
      }
    }
    return ret.toString();
  }

  private static String makeGraqlSafe(String query) {
    //Lines starting '~' are graql only
    StringBuilder ret = new StringBuilder();
    String[] parts = query.split("\n");
    for (String s : parts) {
      if (s.startsWith("~")) {
        s = s.substring(1);
      }
      ret.append(s).append("\n");
    }
    return ret.toString();
  }

  public static List<File> listFiles() {
    File f = new File("tests/1/");
    List<File> list = new ArrayList<>();
    for (File file : f.listFiles()) {
      list.add(file);
    }
    return list;
  }


}


/*

    Constructor[] constructors = clazz.getConstructors();
    for (Constructor constructor : constructors) {
      if (constructor.getParameterCount() == 1) {
        if (constructor.getParameterTypes()[0].equals(TestEnvironment.class)) {
          try {
            Executor executor = (Executor) constructor.newInstance(testEnvironment);
            for (String query: queries) {
              if (!query.equals("\n")) {
                executor.execute(query);
              }
            }
            return;
          } catch (InstantiationException|IllegalAccessException|InvocationTargetException e) {
            e.printStackTrace();
          }
        }
      }
    }
    System.out.println("Could not find a valid constructor");
 */
