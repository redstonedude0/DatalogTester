package uk.ac.cam.gp.charlie.query_generator;

import uk.ac.cam.gp.charlie.TestEnvironment;

import java.util.List;

public class TestSet {
    TestEnvironment env;
    List<String> queries;

    public TestSet(List<String> schema, List<String> data, List<String> queries){
        String schemastr = String.join("\n", schema);
        String datastr = String.join("\n", data);
        env = new TestEnvironment(schemastr, datastr);
        this.queries = queries;
    }
}
