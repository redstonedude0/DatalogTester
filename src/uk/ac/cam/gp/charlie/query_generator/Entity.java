package uk.ac.cam.gp.charlie.query_generator;

import java.util.*;

public class Entity {
    String name;
    Set<Entity> closure;
    Entity parent;
    List<String> instances;


    Entity(String name) {
        this.name = name;
        closure = new HashSet<>();
        closure.add(this);
        instances = new ArrayList<>();
    }

    private void isParent(Entity child){
        closure.addAll(child.closure);
        if(parent != null) parent.isParent(child);
    }

    void isChild(Entity parent){
        if (equals(parent)) return;
        this.parent = parent;
        parent.isParent(this);
    }

    void addInstance(String name){
        instances.add(name);
    }

    String getRandomInstance(){
        Random random = new Random();
        Entity e = getRandomClosureEntity();
        int ninstance = random.nextInt(e.instances.size());
        return e.instances.get(ninstance);
    }

    private Entity getRandomClosureEntity(){
        Random random = new Random();
        int n = random.nextInt(closure.size());
        for (Entity e : closure) {
            if (n-- == 0) return e;
        }
        return null;
    }
}
