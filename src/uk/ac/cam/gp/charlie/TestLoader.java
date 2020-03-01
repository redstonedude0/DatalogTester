package uk.ac.cam.gp.charlie;

import uk.ac.cam.gp.charlie.datalog.DatalogExecutor;
import uk.ac.cam.gp.charlie.graql.GraqlExecutor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * @author hrjh2@cam.ac.uk
 */
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

  public static TestResults runComparisonTests(File f) {
    return runComparisonTests(loadTestsFromFile(f));
  }

  private static TestResults runComparisonTests(TestFile tf) {
    try (
        GraqlExecutor ge = new GraqlExecutor(new TestEnvironment(makeGraqlSafe(tf.te.schema),makeGraqlSafe(tf.te.data)));
        DatalogExecutor de = new DatalogExecutor(new TestEnvironment(makeDatalogSafe(tf.te.schema),makeDatalogSafe(tf.te.data)));
    ) {
      boolean passed = true;
      int id = 1;
      TestResults results = new TestResults(tf.name,tf.tests.size());
      for (String query : tf.tests) {
        String result;
        try {
          Result gr = ge.execute(makeGraqlSafe(query));
          Result dr = de.execute(makeDatalogSafe(query));
          System.out.println("DL RESULTS:");
          dr.print();
          System.out.println("GRAQL RESULTS:");
          gr.print();
          if (!dr.equals(gr)) {
            throw new RuntimeException("Results not equal");
          }
          results.addPassedTest(query);
        } catch (Throwable t) {
          System.out.println("Error: " + t.getMessage());
          results.addFailedTest(query);
          passed = false;
          t.printStackTrace();
        }
        id++;
      }
      results.soutAll();
      return results;
    } catch (Exception e) {
      throw new RuntimeException("Error during execution",e);
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


  public static List<Entry<String,File>> listFiles() {
    return listFiles("");
  }

  private static List<Entry<String,File>> listFiles(String prepend) {
    File f = new File("tests"+prepend+"/");
    List<Entry<String,File>> list = new ArrayList<>();
    for (File file : f.listFiles()) {
      if (file.isDirectory()) {
        list.addAll(listFiles(prepend+"/"+file.getName()));
      } else if (file.isFile()) {
        list.add(new SimpleEntry<>(prepend + "/" + file.getName(), file));
      }
    }
    return list;
  }


}