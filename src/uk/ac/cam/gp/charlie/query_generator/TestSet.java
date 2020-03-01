package uk.ac.cam.gp.charlie.query_generator;

import uk.ac.cam.gp.charlie.TestEnvironment;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gc579@cam.ac.uk
 */
public class TestSet {
    TestEnvironment datalogEnv;
    TestEnvironment graqlEnv;
    List<String> queries;

    public TestSet(List<String> schema, List<String> data, List<String> queries){
        String schemastr = String.join("\n", schema);
        String datastr = String.join("\n", data);
        graqlEnv = new TestEnvironment(schemastr, datastr);

        schemastr = schema.stream().filter(q -> !q.contains("attribute")).collect(Collectors.joining("\n"));
        datalogEnv = new TestEnvironment(schemastr, datastr);

        this.queries = queries;
    }
}
