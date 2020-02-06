package uk.ac.cam.gp.charlie.graql;
import static graql.lang.Graql.*;
import grakn.client.GraknClient;
import graql.lang.query.GraqlInsert;
import uk.ac.cam.gp.charlie.Executor;
import uk.ac.cam.gp.charlie.Result;
import uk.ac.cam.gp.charlie.TestEnvironment;

import java.util.Random;


public class GraqlExecutor extends Executor {

  private GraknClient.Session session;

  /********
   * Note to whoever is writing this code:
   * Please include the relevant grakn/graql libraries in the /lib/ folder (with junit and hamcrest)
   * Thanks ~Harrison
   * Need to use maven ;)
   */

  public GraqlExecutor(TestEnvironment environment) {
    GraknClient client = new GraknClient("localhost:48555");
    session = client.session(randomString());

    GraknClient.Transaction schemaTxn = session.transaction().write();
    parseList(environment.schema).forEach(schemaTxn::execute);
    schemaTxn.commit();

    GraknClient.Transaction dataTxn = session.transaction().write();
    parseList(environment.data).forEach(dataTxn::execute);
    dataTxn.commit();
  }

  @Override
  protected Result execute(String query) {
    return null;
  }

  private String randomString() {
    Random random = new Random();

    int char_a = 'a';
    int char_z = 'z';
    int length = 10;

    String str = random.ints(char_a, char_z + 1)
            .limit(length)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();

    System.out.println(str);
    return str;
  }
}
