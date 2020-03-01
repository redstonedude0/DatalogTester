package uk.ac.cam.gp.charlie.query_generator;

import uk.ac.cam.gp.charlie.TestEnvironment;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a batch of queries to be run on the two implementations
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

        // filter the attribute definitions as they are currently not supported in the Datalog
        schemastr = schema.stream().filter(q -> !q.contains("attribute")).collect(Collectors.joining("\n"));
        datalogEnv = new TestEnvironment(schemastr, datastr);

        this.queries = queries;
    }
}
