package uk.ac.cam.gp.charlie.query_generator;

import uk.ac.cam.gp.charlie.graql.GraqlExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QueryGenerator {
    private static final int N_ENTITIES = 5;
    private static final int N_RELATIONS = 3;
    private static final int OBJS_PER_ENTITY = 5;
    private static final int OBJS_PER_RELATION = 10;
    private static final int[] TRAVERSE_QUERIES = {3, 3, 3};

    public static TestSet generateTestSet(){
        Random random = new Random();
        List<String> graqlSchema = new ArrayList<>();

        // generate attributes
        {
            String str = "define name sub attribute, datatype string;";
            graqlSchema.add(str);
        }

        // generate entities
        List<Entity> entities = new ArrayList<>();
        for(int i = 0; i<N_ENTITIES; i++){
            Entity entity = new Entity(randomString());
            entities.add(entity);
            entity.isChild(entities.get(random.nextInt(entities.size())));
            String str = "define " + entity.name + " sub " +
                    (entity.parent == null ? "entity" : entity.parent.name) +
                    ", plays " + entity.name + "R, has name;";
            graqlSchema.add(str);
        }

        {
            // needed for the roles to be defined
            String str = "define " + randomString() + " sub relation";
            for(Entity e: entities){
                str += ", relates " + e.name + "R";
            }
            str += ";";
            graqlSchema.add(0, str);
        }

        // generate relations
        List<Relation> relations = new ArrayList<>();
        for(int i = 0; i<N_RELATIONS; i++){
            Relation relation = new Relation(randomString());
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

        List<String> graqlData = new ArrayList<>();

        // insert entity instances
        for(Entity e : entities){
            for(int i = 0; i<OBJS_PER_ENTITY; i++){
                String name = randomString();
                e.addInstance(name);
                String str = "insert $p isa " + e.name + ", has name \"" + name + "\";";
                graqlData.add(str);
            }
        }

        // insert relation instances
        for(Relation r : relations){
            for(int i = 0; i<OBJS_PER_RELATION; i++){
                String str = "match ";
                for(Entity e: r.players){
                    String instance = e.getRandomInstance();
                    str += " $" + e.name + " isa " + e.name + ", has name \"" + instance + "\";";
                }
                str += " insert $" + randomString() + " (";
                for(Entity e: r.players){
                    str += " " + e.name + "R: $" + e.name + ", ";
                }
                str = str.substring(0, str.length()-2); // remove last comma
                str += ") isa " + r.name + "; ";
                graqlData.add(str);
            }
        }

        List<String> graqlQueries = new ArrayList<>();

        // traverse
        int length = 0;
        while(length < TRAVERSE_QUERIES.length){
            length++;
            for(int i = 0; i<TRAVERSE_QUERIES[length-1]; i++){
                String str = "match ";
                String lastId = "$" + randomString();
                Entity lastEntity = entities.get(random.nextInt(entities.size()));
                while(lastEntity.relations.size()==0) lastEntity = entities.get(random.nextInt(entities.size()));

                for(int j = 0; j<length; j++){
                    Relation r = lastEntity.relations.get(random.nextInt(lastEntity.relations.size()));
                    str += " (" + lastEntity.name + "R: " + lastId;
                    String newId = lastId;
                    Entity newEntity = lastEntity;
                    for(Entity e: r.players){
                        if (!e.equals(lastEntity)){
                            newId = "$" + randomString();
                            newEntity = e;
                            str += ", " + newEntity.name + "R: " + newId;
                        }
                    }
                    str += ") isa " + r.name + "; ";
                    lastEntity = newEntity;
                    lastId = newId;
                }
                str += "get;";
                graqlQueries.add(str);
            }
        }

        graqlSchema.forEach(System.out::println);
        graqlData.forEach(System.out::println);
        graqlQueries.forEach(System.out::println);

        return new TestSet(graqlSchema, graqlData, graqlQueries);
    }


    private static String randomString() {
        Random random = new Random();
        int char_a = 'a';
        int char_z = 'z';
        int length = 10;
        String str = random.ints(char_a, char_z + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return str;
    }

    public static void main(String[] args) {
        TestSet ts = generateTestSet();
        GraqlExecutor gex = new GraqlExecutor(ts.env);
        ts.queries.forEach(gex::execute);

    }


}
