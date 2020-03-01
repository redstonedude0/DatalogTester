package uk.ac.cam.gp.charlie.query_generator;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a relation in the schema of the query generator
 * @author gc579@cam.ac.uk
 */
public class Relation {
    String name;
    Set<Entity> players;    // set of entities playing in the relation

    public Relation(String name) {
        this.name = name;
        players = new HashSet<>();
    }

    public void addPlayer(Entity e){
        e.addRelation(this);
        players.add(e);
    }
}
