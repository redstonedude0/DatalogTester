package uk.ac.cam.gp.charlie.query_generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Entity {
    String name;
    Set<Entity> closure;
    Entity parent;

    public Entity(String name) {
        this.name = name;
        closure = new HashSet<>();
        closure.add(this);
    }

    private void isParent(Entity child){
        closure.addAll(child.closure);
        if(parent != null) parent.isParent(child);
    }

    public void isChild(Entity parent){
        if (equals(parent)) return;
        this.parent = parent;
        parent.isParent(this);
    }
}
