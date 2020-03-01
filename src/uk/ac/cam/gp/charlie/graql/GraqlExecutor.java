package uk.ac.cam.gp.charlie.graql;

import grakn.client.GraknClient;
import grakn.client.answer.ConceptMap;
import graql.lang.query.GraqlGet;
import uk.ac.cam.gp.charlie.DebugHelper;
import uk.ac.cam.gp.charlie.Executor;
import uk.ac.cam.gp.charlie.Result;
import uk.ac.cam.gp.charlie.TestEnvironment;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static graql.lang.Graql.parseList;


/**
 * @author gc579@cam.ac.uk
 */
public class GraqlExecutor extends Executor {

  private GraknClient.Session session;
  private GraknClient client;

  public GraqlExecutor(TestEnvironment environment) {
    if (!DebugHelper.VERBOSE_GRAQL) {
      DebugHelper.absorbOutput();
    }
    client = new GraknClient("localhost:48555");
    session = client.session(randomString());
    DebugHelper.restoreOutput();

    if (DebugHelper.VERBOSE_GRAQL_INPUT) {
      System.out.println("Running:" + environment.schema);
    }
    if (!DebugHelper.VERBOSE_GRAQL) {
      DebugHelper.absorbOutput();
    }
    GraknClient.Transaction schemaTxn = session.transaction().write();
    parseList(environment.schema).forEach(schemaTxn::execute);
    schemaTxn.commit();
    DebugHelper.restoreOutput();

    if(environment.data.length() > 0) {
      if (DebugHelper.VERBOSE_GRAQL_INPUT) {
        System.out.println("Running:" + environment.data);
      }
      if (!DebugHelper.VERBOSE_GRAQL) {
        DebugHelper.absorbOutput();
      }
      GraknClient.Transaction dataTxn = session.transaction().write();
      parseList(environment.data).forEach(dataTxn::execute);
      dataTxn.commit();
      DebugHelper.restoreOutput();
    }
  }

  @Override
  public void close() {
    super.close();
    session.close();
    client.close();
  }

  @Override
  public Result execute(String query) {
    GraknClient.Transaction readTxn = session.transaction().read();
    if (DebugHelper.VERBOSE_GRAQL_INPUT) {
      System.out.println("Executing Graql Queries: " + query);
    }
    if (!DebugHelper.VERBOSE_GRAQL) {
      DebugHelper.absorbOutput();
    }
    List<List<ConceptMap>> graqlResults =
            parseList(query).map(q -> readTxn.stream((GraqlGet) q).collect(Collectors.toList()))
                    .collect(Collectors.toList());

    Result result = Result.fromGrakn(graqlResults);
    readTxn.close();
    DebugHelper.restoreOutput();
    return result;
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
