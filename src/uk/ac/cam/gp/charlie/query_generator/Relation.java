package uk.ac.cam.gp.charlie.query_generator;

import java.util.HashSet;
import java.util.Set;

/**
 * @author gc579@cam.ac.uk
 */
public class Relation {
    String name;
    Set<Entity> players;

    public Relation(String name) {
        this.name = name;
        players = new HashSet<>();
    }

    public void addPlayer(Entity e){
        e.addRelation(this);
        players.add(e);
    }
}
