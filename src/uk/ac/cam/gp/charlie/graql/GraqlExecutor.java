package uk.ac.cam.gp.charlie.graql;
import static graql.lang.Graql.*;
import grakn.client.GraknClient;
import graql.lang.query.GraqlInsert;
import uk.ac.cam.gp.charlie.Executor;
import uk.ac.cam.gp.charlie.Result;
import uk.ac.cam.gp.charlie.TestEnvironment;

public class GraqlExecutor extends Executor {

  GraknClient.Session session;

  public GraqlExecutor(TestEnvironment environment) {
    GraknClient client = new GraknClient("localhost:48555");
    GraknClient.Session session = client.session("social_network");

    GraknClient.Transaction schemaTxn = session.transaction().write();
    System.out.println("Executing Graql Query: " + environment.schema);
    schemaTxn.execute((GraqlInsert) parse(environment.schema));
    schemaTxn.commit();

    GraknClient.Transaction dataTxn = session.transaction().write();
    System.out.println("Executing Graql Query: " + environment.data);
    dataTxn.execute((GraqlInsert) parse(environment.data));
    dataTxn.commit();
  }

  @Override
  protected Result execute(String query) {
    return null;
  }
}
