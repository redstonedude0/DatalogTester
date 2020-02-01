package uk.ac.cam.gp.charlie.graql;

import java.util.ArrayList;
import java.util.List;
import uk.ac.cam.gp.charlie.ast.Define;

/**
 * Parse Graql into ASTs
 */
public class GraqlParser {

  /**
   * Convert a test environment schema (written in graql) into a list of ASTs
   *
   * @param schema the graql schema to convert
   * @return A list of ASTs representing the schema
   */
  public static List<Define> schemaToAST(String schema) {
    List<Define> toRet = new ArrayList<>();
    //this should parse the schema into a list of defines
    return toRet;
  }

  /**
   * Convert a test environment data (written in graql) into a list of ASTs
   *
   * @param data the graql data to convert
   * @return A list of asts representing the schema
   *
   * TODO: note - should not return List<String>, that is a placeholder. Please return a list of
   * ASTs
   */
  public static List<String> dataToAST(String data) {
    List<String> toRet = new ArrayList<>();
    return toRet;
  }

}
