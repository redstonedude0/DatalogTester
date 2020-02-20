package uk.ac.cam.gp.charlie.query_generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QueryGenerator {
    private static final int N_ENTITIES = 10;
    private static final int N_RELATIONS = 5;

    public static TestSet generateTestSet(){
        List<String> graqlSchema = new ArrayList<>();
        Random random = new Random();

        // generate entities
        List<Entity> entities = new ArrayList<>();
        for(int i = 0; i<N_ENTITIES; i++){
            Entity entity = new Entity(randomString());
            entities.add(entity);
            entity.isChild(entities.get(random.nextInt(entities.size())));
            String str = "define " + entity.name + " sub " +
                    (entity.parent == null ? "entity" : entity.parent.name) +
                    ", plays " + entity.name + ";";
            graqlSchema.add(str);
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
                str += ", relates " + e.name;
            }
            str += ";";
            graqlSchema.add(str);
        }

        graqlSchema.forEach(System.out::println);

        return new TestSet();
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
        generateTestSet();
    }

}
