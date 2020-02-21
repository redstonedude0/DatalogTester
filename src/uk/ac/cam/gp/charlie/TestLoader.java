package uk.ac.cam.gp.charlie;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import uk.ac.cam.gp.charlie.datalog.DatalogExecutor;
import uk.ac.cam.gp.charlie.graql.GraqlExecutor;

public class TestLoader {

  private static class TestFile {
    TestEnvironment te;
    List<String> tests = new ArrayList<>();
  }

  public static TestFile loadTestsFromFile(File f) {
    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
      StringBuilder contents = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        contents.append(line).append("\n");
      }
      return loadTestsFromFileContents(contents.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static TestFile loadTestsFromFileContents(String fileContents) {
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
    tf.te = new TestEnvironment(schema,data);
    for (String query: queries) {
      if (!query.equals("\n") && !query.equals("")) {
        tf.tests.add(query);
      }
    }
    return tf;
  }

  public static void runComparisonTests(File f) {
    runComparisonTests(loadTestsFromFile(f));
  }

  public static void runComparisonTests(TestFile tf) {
    for (String query : tf.tests) {
      runComparisonTest(tf.te,query);
    }
  }

  private static String makeDatalogSafe(String query) {
    //Lines starting '~' are graql only
    StringBuilder ret = new StringBuilder();
    String[] parts = query.split("\n");
    for(String s : parts) {
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
    for(String s : parts) {
      if (s.startsWith("~")) {
        s = s.substring(1);
      }
      ret.append(s).append("\n");
    }
    return ret.toString();
  }

  public static void runComparisonTest(TestEnvironment te, String query) {
    DatalogExecutor de = new DatalogExecutor(te);
    GraqlExecutor ge = new GraqlExecutor(te);
    Result dr = de.execute(makeDatalogSafe(query));
    Result gr = ge.execute(makeGraqlSafe(query));
    if (!dr.equals(gr)) {
      System.out.println("DL RESULTS:");
      dr.print();
      System.out.println("GRAQL RESULTS:");
      gr.print();
      throw new RuntimeException("Results not equal");
    }
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
