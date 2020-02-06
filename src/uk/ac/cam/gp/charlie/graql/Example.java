package uk.ac.cam.gp.charlie.graql;

import grakn.client.GraknClient;
import grakn.client.answer.ConceptMap;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlQuery;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static graql.lang.Graql.*;


public class Example {

    private static String readLineByLineJava8(String filePath){
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8)){
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }catch (IOException e){
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    private static void printResult(GraknClient.Transaction readTxn, GraqlQuery query){
        if (query instanceof GraqlGet) {
            Stream<ConceptMap> answers = readTxn.stream((GraqlGet) query);
            answers.forEach(answer -> System.out.println(answer.get("fn").asAttribute().value()));
        } else {
            readTxn.execute(query);
        }
    }

    public static void main(String[] args) {
        GraknClient client = new GraknClient("localhost:48555");
        GraknClient.Session session = client.session("social_network4");

        /*GraknClient.Transaction schemaTxn = session.transaction().write();
        String schema = readLineByLineJava8("schema.gql");
        System.out.println("Executing schema Query ");
        parseList(schema).forEach(schemaTxn::execute);
        schemaTxn.commit();

        GraknClient.Transaction dataTxn = session.transaction().write();
        String data = readLineByLineJava8("data.gql");
        System.out.println("Executing insert Query ");
        parseList(data).forEach(dataTxn::execute);
        dataTxn.commit();*/

        GraknClient.Transaction readTxn = session.transaction().read();
        String query = "match $tra (traveler: $per) isa travel; (located-travel: $tra, travel-location: $loc) isa location-of-travel; $loc has name \"French Lick\"; $per has full-name $fn; get $fn;";
        System.out.println("Executing Graql Query: " + query);
        parseList(query).forEach(q -> printResult(readTxn, (GraqlGet) q));
        readTxn.close();

        // Insert a person using a WRITE transaction
        /*GraknClient.Transaction writeTransaction = session.transaction().write();
        GraqlInsert insertQuery = Graql.insert(var("x").isa("person").has("email", "z@email.com"));
        List<ConceptMap> insertedId = writeTransaction.execute(insertQuery);
        System.out.println("Inserted a person with ID: " + insertedId.get(0).get("x").id());
        // to persist changes, a write transaction must always be committed (closed)
        writeTransaction.commit();*/

        /*GraknClient.Transaction dataTxn = session.transaction().write();
        String data = "insert $x isa person, has email \"s3@email.com\"; \ninsert $x isa person, has email \"s2@email.com\";";
        System.out.println("Executing Graql Query: " + data);
        parseList(data).forEach(dataTxn::execute);
        dataTxn.commit();*/

        // Read the person using a READ only transaction
        /*GraknClient.Transaction readTransaction = session.transaction().read();
        String query = "insert $x isa person, has email \"s3@email.com\"; \ninsert $x isa person, has email \"s2@email.com\";";
        System.out.println("Executing Graql Query: " + data);
        parseList(data).forEach(dataTxn::execute);
        readTransaction.close();*/

        /*GraqlGet getQuery = Graql.match(var("_").isa("person").has("email", var("e"))).get().limit(10);
        Stream<ConceptMap> answers = readTransaction.stream(getQuery);
        answers.forEach(answer -> System.out.println(answer.get("e").asAttribute().value()));*/

        // transactions, sessions and clients must always be closed
        session.close();
        client.close();
    }

}
