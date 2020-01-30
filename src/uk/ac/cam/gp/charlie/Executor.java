package uk.ac.cam.gp.charlie;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class Executor {

  public abstract Result execute(Test t);

  public final Map<Test,Result> executeBatch(Set<Test> ts) {
    Map<Test,Result> toReturn = new HashMap<>();
    for (Test t: ts) {
      toReturn.put(t,execute(t));
    }
    return toReturn;
  }

}
