package uk.ac.cam.gp.charlie;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class Executor {

  protected abstract Result execute(String query);

  public final Map<String,Result> executeBatch(Set<String> tests) {
    Map<String,Result> toReturn = new HashMap<>();
    for (String t: tests) {
      toReturn.put(t,execute(t));
    }
    return toReturn;
  }

}
