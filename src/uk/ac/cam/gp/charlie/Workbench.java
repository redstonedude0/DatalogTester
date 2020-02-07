package uk.ac.cam.gp.charlie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import uk.ac.cam.gp.charlie.datalog.DatalogExecutor;

/**
 * Main file for running the interactive user workbench.
 *
 *
 */
public class Workbench {

  public static void main(String[] args) throws IOException {
    System.out.println("Enter graql query to be completed, terminate with blank line");
    //TODO: does not work for now as graqlparser does not parse properly
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    TestEnvironment te = new TestEnvironment("\n","\n");
    DatalogExecutor de = new DatalogExecutor(te);
    while (true) {
      String input = "";
      while (true) {
        String line = br.readLine();
        input += line + "\n";
        if (line.equals("")) {
          break;
        }
      }
      System.out.println(input);
      Result r = de.execute(input);
      System.out.println("Result");
      System.out.println(r);
    }

  }


}
