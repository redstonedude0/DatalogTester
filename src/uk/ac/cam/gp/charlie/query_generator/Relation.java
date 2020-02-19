package uk.ac.cam.gp.charlie.query_generator;

import java.util.HashSet;
import java.util.Set;

public class Relation {
    String name;
    Set<Entity> players;

    public Relation(String name) {
        this.name = name;
        players = new HashSet<>();
    }

    public void addPlayer(Entity e){
        players.add(e);
    }
}
