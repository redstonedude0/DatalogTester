package uk.ac.cam.gp.charlie.datalog.interpreter;

import uk.ac.cam.gp.charlie.TestEnvironment;
import uk.ac.cam.gp.charlie.datalog.interpreter.ast.Define;

/**
 * Interprets to and from Context (An AST representation)
 */
public class ContextInterpreter {

  /**
   * Convert an environment (written in graql) into an AST Context
   * @param environment the graql environment to convert
   * @return A Context representing the environment
   */
  public static Context toContext(TestEnvironment environment) {
    Context c = new Context();
    //TODO remove. For now just dumping the raw (hopefully datalog) text into the context).
    c.TEST_REMOVE = environment.schema + " " + environment.data;
    return c;
  }

  /**
   * Convert a Context to datalog (to be used for initialising the environment)
   * @param c The Context to convert
   * @return A string to pass into engine.init
   */
  public static String toDatalog(Context c) {
    //TODO actually code, for now just dumps test string through.
    /**
     * Need to convert:
     * [Define->DefineEntity
     *    identifier "person"
     *    attributes [a<name><1>]
     *    plays [p<employee>]
     * ,Define->DefineEntity
     *    identifier "organisation"
     *    attributes [a<name><2>]
     *    plays [p<employer>]
     * ,Define->DefineRelation
     *    identifier "employment"
     *    relates [p<employee>,p<employer>]
     * ,Define->DefineRelation
     *    identifier "coworkers"
     *    relates [p<employeer>]
     * ]
     * where x<> indicates an object pointer to a Plays or Attrribute relation
     *
     * to be converted into:
     *
     *
     *
     *
     *
     *
     */
    for (Define define: c.schema) {

    }
    return c.TEST_REMOVE;
  }

  /**
   * Convert a graql query to datalog under a given context
   * @param graqlQuery
   * @param c the context of the test environment
   * @return 2 Datalog strings (the first contains positive atoms to be added to the execution environment, the second contains queries)
   *
   * Example return:
   *   [
   *   0 -> "query_ab3376_hj7862_uuid_uuid_8832(Val_1,Val_2,Val_3) :- instanceof(Val_1,t_employment), ... .
   *   1 -> "query_ab3376_hj7862_uuid_uuid_8832(Val_1,Val_2,Val_3)."
   *   ]
   */
  public static String[] toDatalog(String graqlQuery, Context c) {
    //TODO:
    //tokenise graql
    //parse into AST
    //compile into datalog

    //TODO - for now just returning the query (hoping it's actually datalog not graql).
    String[] toRet = new String[2];
    toRet[0] = "";
    toRet[1] = graqlQuery;
    return toRet;
  }

}
