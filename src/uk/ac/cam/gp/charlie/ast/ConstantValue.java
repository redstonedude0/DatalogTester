package uk.ac.cam.gp.charlie.ast;


import java.util.ArrayList;
import java.util.List;

/**
 * Represents a constant
 */
public class ConstantValue extends AttributeValue {

  public Object value;
  private static List<ConstantValue> instances = new ArrayList<>();

  private ConstantValue(Object value) {
    this.value = value;
    instances.add(this);
  }

  public static ConstantValue fromValue(Object value) {
    for(ConstantValue v : instances) {
      if (v.value.equals(value)) {
        return v;
      }
    }
    return new ConstantValue(value);
  }

}
