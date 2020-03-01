package uk.ac.cam.gp.charlie.query_generator;

import uk.ac.cam.gp.charlie.DebugHelper;
import uk.ac.cam.gp.charlie.Result;
import uk.ac.cam.gp.charlie.datalog.DatalogExecutor;
import uk.ac.cam.gp.charlie.graql.GraqlExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates random schemas, data and queries
 * @author gc579@cam.ac.uk
 */
public class QueryGenerator {
    private static final int N_ENTITIES = 5;                    // number of entities to define
    private static final int N_RELATIONS = 3;                   // number of relations to define
    private static final int OBJS_PER_ENTITY = 3;               // number of objects to add per each entity
    private static final int OBJS_PER_RELATION = 10;            // number of instances to add per relation
    private static final int[] TRAVERSE_QUERIES = {3, 4, 5};    // number of traverse queries to generate, in order of length from 1

    private static void generateAttributes(List<String> graqlSchema){
        // defines a single attribute that the various entities will use to be distinguishable
        String str = "define name sub attribute, datatype string;";
        graqlSchema.add(str);
    }

    private static List<Entity> generateEntities(List<String> graqlSchema, Random random){
        // generates a random directed acyclic graph of entities with random name
        List<Entity> entities = new ArrayList<>();
        for(int i = 0; i<N_ENTITIES; i++){
            Entity entity = new Entity(randomString(random));
            entities.add(entity);
            entity.isChild(entities.get(random.nextInt(entities.size())));
            String str = "define " + entity.name + " sub " +
                    (entity.parent == null ? "entity" : entity.parent.name) +
                    ", plays " + entity.name + "R, has name;";
            graqlSchema.add(str);
        }
        return entities;
    }

    private static void defineRoles(List<String> graqlSchema, List<Entity> entities){
        // for simplicity each entity plays the same role in each relation it partecipate in and this is generated
        // by appending R to the name of the entity, this clause is used to define all of them at the beginning
        // in a dummy relation that will not be used

        String str = "define rolesDefiningRelation sub relation";
        for(Entity e: entities){
            str += ", relates " + e.name + "R";
        }
        str += ";";
        graqlSchema.add(0, str);
    }

    private static List<Relation> defineRelations(List<String> graqlSchema, List<Entity> entities, Random random){
        // defines a relations with a random number of players (atm limited between 1 and 3)
        List<Relation> relations = new ArrayList<>();
        for(int i = 0; i<N_RELATIONS; i++){
            Relation relation = new Relation(randomString(random));
            int nplayers = random.nextInt(2)+1;
            for (int j = 0; j<nplayers; j++){
                relation.addPlayer(entities.get(random.nextInt(entities.size())));
            }
            String str = "define " + relation.name + " sub relation";
            for(Entity e: relation.players){
                str += ", relates " + e.name + "R";
            }
            str += ";";
            relations.add(relation);
            graqlSchema.add(str);
        }
        return relations;
    }

    private static void insertEntities(List<String> graqlData, List<Entity> entities, Random random){
        // inserts a number of objects with random name to each entity
        for(Entity e : entities){
            for(int i = 0; i<OBJS_PER_ENTITY; i++){
                String name = randomString(random);
                e.addInstance(name);
                String str = "insert $p isa " + e.name + ", has name \"" + name + "\";";
                graqlData.add(str);
            }
        }
    }

    private static void insertRelations(List<String> graqlData, List<Relation> relations, Random random){
        // inserts a number of instances to each relation between objects in the closure of the entities of players
        for(Relation r : relations){
            for(int i = 0; i<OBJS_PER_RELATION; i++){
                String str = "match ";
                for(Entity e: r.players){
                    String instance = e.getRandomInstance(random);
                    str += " $" + e.name + " isa " + e.name + ", has name \"" + instance + "\";";
                }
                str += " insert $" + randomString(random) + " (";
                for(Entity e: r.players){
                    str += " " + e.name + "R: $" + e.name + ", ";
                }
                str = str.substring(0, str.length()-2); // remove last comma
                str += ") isa " + r.name + "; ";
                graqlData.add(str);
            }
        }
    }

    private static void traverseQueries(List<String> graqlQueries, List<Entity> entities, Random random){
        // generates based on random paths of different lengths taken from entities to relations to other entities
        // the name attributes of the instances of entities taken along the way are retrieved in order to be able to
        // compare them between the implementations
        int length = 0;
        while(length < TRAVERSE_QUERIES.length){
            length++;
            for(int i = 0; i<TRAVERSE_QUERIES[length-1]; i++){
                // generate a query of length length
                String str = "match ";
                String lastId = "$" + randomString(random);
                Entity lastEntity = entities.get(random.nextInt(entities.size()));
                while(lastEntity.relations.size()==0) lastEntity = entities.get(random.nextInt(entities.size()));

                str += " " + lastId + " has name " + lastId + "N; ";

                for(int j = 0; j<length; j++){
                    Relation r = lastEntity.relations.get(random.nextInt(lastEntity.relations.size()));
                    str += " (" + lastEntity.name + "R: " + lastId;
                    String newId = lastId;
                    Entity newEntity = lastEntity;
                    for(Entity e: r.players){
                        if (!e.equals(lastEntity)){
                            newId = "$" + randomString(random);
                            newEntity = e;
                            str += ", " + newEntity.name + "R: " + newId;
                        }
                    }
                    lastEntity = newEntity;
                    lastId = newId;
                    str += ") isa " + r.name + "; ";
                    str += " " + lastId + " has name " + lastId + "N; ";

                }
                str += " get; ";
                graqlQueries.add(str);
            }
        }
    }


    public static TestSet generateTestSet(Random random){
        // SCHEMA
        List<String> graqlSchema = new ArrayList<>();

        // generate attributes
        generateAttributes(graqlSchema);

        // generate entities
        List<Entity> entities = generateEntities(graqlSchema, random);

        // define roles, needed for the roles to be defined
        defineRoles(graqlSchema, entities);

        // generate relations
        List<Relation> relations = defineRelations(graqlSchema, entities, random);

        // DATA
        List<String> graqlData = new ArrayList<>();

        // insert entity instances
        insertEntities(graqlData, entities, random);

        // insert relation instances
        insertRelations(graqlData, relations, random);

        // QUERIES
        List<String> graqlQueries = new ArrayList<>();

        // traverse queries
        traverseQueries(graqlQueries, entities, random);

        graqlSchema.forEach(System.out::println);
        graqlData.forEach(System.out::println);
        graqlQueries.forEach(System.out::println);

        return new TestSet(graqlSchema, graqlData, graqlQueries);
    }


    private static String randomString(Random random) {
        // generates a random string of 10 characters
        int char_a = 'a';
        int char_z = 'z';
        int length = 10;
        String str = random.ints(char_a, char_z + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return str;
    }

    private static void compareResults(GraqlExecutor gex, DatalogExecutor dex, String query){
        // executes a query on the two implementations and compares the results
        Result r1 = gex.execute(query);
        Result r2 = dex.execute(query);
        if(!r1.equals(r2)){
            System.err.println("Difference on query: "  + query);
        }
    }

    public static void runTests(long seed){
        // runs a set of randomly generated tests and compares the results of the two implementations
        Random random = new Random(seed);
        TestSet ts = generateTestSet(random);
        GraqlExecutor gex = new GraqlExecutor(ts.graqlEnv);
        DatalogExecutor dex = new DatalogExecutor(ts.datalogEnv);
        ts.queries.forEach(q -> compareResults(gex, dex, q));
    }

    public static void main(String[] args) {
        runTests(0);
    }
}
