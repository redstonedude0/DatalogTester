package uk.ac.cam.gp.charlie.query_generator;

import java.util.*;

/**
 * Represents an entity in the schema of the query generator
 * @author gc579@cam.ac.uk
 */
public class Entity {
    String name;
    Set<Entity> closure;        // set of the current entity and all its descendants
    List<Relation> relations;   // relations in which the entity can play
    Entity parent;              // direct super-entity (null if it's entity)
    List<String> instances;     // names of the instances of the current entity generated

    Entity(String name) {
        this.name = name;
        closure = new HashSet<>();
        closure.add(this);
        instances = new ArrayList<>();
        relations = new ArrayList<>();
    }

    private void isParent(Entity child){
        // child has entered in the subtree of the current entity
        closure.addAll(child.closure);
        if(parent != null) parent.isParent(child);
    }

    void isChild(Entity parent){
        // parent is the direct super-entity of the current entity
        if (equals(parent)) return;
        this.parent = parent;
        parent.isParent(this);
    }

    void addInstance(String name){
        instances.add(name);
    }

    String getRandomInstance(Random random){
        // gets a random instance that is in one of the entities in the closure
        Entity e = getRandomClosureEntity(random);
        int ninstance = random.nextInt(e.instances.size());
        return e.instances.get(ninstance);
    }

    private Entity getRandomClosureEntity(Random random){
        int n = random.nextInt(closure.size());
        for (Entity e : closure) {
            if (n-- == 0) return e;
        }
        return null;
    }

    void addRelation(Relation r){
        // adds a relation directly to the current entity
        for(Entity e: closure){
            e.relations.add(r);
        }
    }
}
