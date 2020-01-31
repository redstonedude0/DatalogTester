package uk.ac.cam.gp.charlie.datalog.interpreter;

import uk.ac.cam.gp.charlie.TestEnvironment;
import uk.ac.cam.gp.charlie.datalog.interpreter.ast.Define;

public class GraqlInterpreter {

  public static Context toContext(TestEnvironment environment) {


    return null;
  }

  public static String toDatalog(Context c) {
    //EXAMPLE::
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
    return null;
  }

  public static String toDatalog(String graql, Context c) {
    //TODO:

    //tokenise graql

    //parse into AST

    //compile into datalog

    return graql;
  }

}
