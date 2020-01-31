package uk.ac.cam.gp.charlie;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class TestLoader {

  /**
   * @param clazz
   * @param f
   */
  public static void runTestsFromFile(Class<? extends Executor> clazz, File f) {
    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
      StringBuilder contents = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        contents.append(line);
      }
      runTests(clazz,contents.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * TODO: Modify to optimise. Only need to be building TestEnvironment once for 2 Class's
   * @param clazz
   * @param fileContents
   */
  public static void runTests(Class<? extends Executor> clazz, String fileContents) {
    String[] bits = fileContents.split("###Schema###");
    fileContents = bits[1];
    bits = fileContents.split("###Data###");
    String schema = bits[0];
    fileContents = bits[1];
    bits = fileContents.split("###Test###");
    String data = bits[0];
    String queryBlock = bits[1];
    String[] queries = queryBlock.split("\n");


    TestEnvironment testEnvironment = new TestEnvironment(schema,data);
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
  }

}
