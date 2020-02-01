package uk.ac.cam.gp.charlie.datalog.interpreter;

import uk.ac.cam.gp.charlie.TestEnvironment;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.Define;
import uk.ac.cam.gp.charlie.ast.DefineEntity;
import uk.ac.cam.gp.charlie.ast.DefineRelation;
import uk.ac.cam.gp.charlie.ast.Plays;

/**
 * Interprets to and from Context (An AST representation)
 */
public class ASTInterpreter {

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
     *    relates [p<employee>]
     * ]
     * where x<> indicates an object pointer to a Plays or Attrribute relation
     *
     * theoretical IR:
     *
     * t_subs(t_person,t_entity).
     * t_hasattr(t_person,name).
     * t_playsrole(t_person,employee).
     *
     *
     * t_subs(t_organisation,t_entity).
     * t_hasattr(t_organisation,name).
     * t_playsrole(t_organisation,employer).
     *
     * t_subs(t_employment,t_relation).
     * t_relates(t_employment,employee).
     * t_relates(t_employment,employer).
     *
     * t_subs(t_coworkers,t_relation)
     * t_relates(t_coworkers,employee)
     *
     * (applied to data)
     * instanceof(e_1,t_person).
     * instanceof(e_2,t_person).
     * instanceof(e_3,t_organisation).
     * instanceof(e_4,t_employment).
     * instanceattr(e_1,name,str_Bob).
     * instanceattr(e_2,name,str_Alice).
     * instanceattr(e_3,name,str_Uni).
     * instancerel(e_4,employee,e_1).
     * instancerel(e_4,employer,e_3).
     *
     *
     * final conversion:
     * t_subs(t_0,t_entity).
     * t_hasattr(t_0,a_0).
     * t_playsrole(t_0,r_0).
     *
     *
     * t_subs(t_1,t_entity).
     * t_hasattr(t_1,a_0).
     * t_playsrole(t_1,r_1).
     *
     * t_subs(t_2,t_relation).
     * t_relates(t_2,r_0).
     * t_relates(t_2,r_1).
     *
     * t_subs(t_3,t_relation)
     * t_relates(t_3,r_0)
     *
     */
    StringBuilder toRet = new StringBuilder();
    for (Define define: c.schema) {
      if (define instanceof DefineEntity) {
        DefineEntity entity = (DefineEntity) define;
        toRet.append(String.format("t_subs(t_%d,t_entity).\n", c.typeNumber));
        for (Attribute attribute : entity.attributes) {
          toRet.append(String
              .format("t_hasattr(t_%d,a_%d).\n", c.typeNumber, c.getAttributeNumber(attribute)));
        }
        for (Plays play : entity.plays) {
          toRet.append(
              String.format("t_playsrole(t_%d,r_%d).\n", c.typeNumber, c.getPlaysNumber(play)));
        }
      } else if (define instanceof DefineRelation) {
        DefineRelation entity = (DefineRelation) define;
        toRet.append(String.format("t_subs(t_%d,t_relation).\n", c.typeNumber));
        for (Plays play : entity.relates) {
          toRet.append(String
              .format("t_relates(t_%d,r_%d).\n", c.typeNumber, c.getPlaysNumber(play)));
        }
      } else {
        throw new RuntimeException("unknown define type");
      }
      toRet.append("\n");
      c.typeDefinitions.put(c.typeNumber,define);
      c.typeNumber++;
    }
    return toRet.toString();
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
