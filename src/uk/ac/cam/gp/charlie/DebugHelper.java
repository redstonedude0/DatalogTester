package uk.ac.cam.gp.charlie;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import uk.ac.cam.gp.charlie.ast.Variable;

public class DebugHelper {

  public static boolean VERBOSE_AST = false;
  public static boolean VERBOSE_DATALOG = false;
  public static boolean VERBOSE_RESULTS = false;

  /**
   * If there's cycles this'll break horrendously, use this to print an AST in a (nice enough) way
   */
  public static void printObjectTree(Object o) {
    printObjectTree(o, 1);
  }

  private static String idnt(int idnt) {
    StringBuilder s = new StringBuilder();
    for (int i = 0; i < idnt; i++) {
      s.append("  ");
    }
    return s.toString();
  }


  private static void printObjectTree(Object o, int idnt) {
    try {
      if (o instanceof List) {
        List l = (List) o;
        if (l.size() == 0) {
          System.out.println("[]");
          return;
        }
        System.out.println();
        idnt--;
        System.out.println(idnt(idnt) + "[");
        idnt++;
        for (Object item : l) {
          printObjectTree(item, idnt);
        }
        idnt--;
        System.out.println(idnt(idnt) + "]");
        return;
      }
      if (o instanceof Map) {
        Map m = (Map) o;
        if (m.size() == 0) {
          System.out.println("{}");
          return;
        }
        System.out.println();
        idnt--;
        System.out.println(idnt(idnt) + "{");
        idnt++;
        for (Object item : m.entrySet()) {
          printObjectTree(item, idnt);
        }
        idnt--;
        System.out.println(idnt(idnt) + "}");
        return;
      }
      if (o instanceof String) {
        System.out.println("{" + o + "}");
        return;
      }
      if (o == null) {
        System.out.println(idnt(idnt) + "[null]");
        return;
      }
      Field[] fields = o.getClass().getFields();
      if (fields.length == 1) {
        if (fields[0].get(o) instanceof String) {
          System.out.println(
              idnt(idnt) + o.getClass().getSimpleName() + "{" + ((String) fields[0].get(o))
                  .replaceAll("\n", "\\n") + "}");
          return;
        }
      }
      System.out.print(idnt(idnt) + o.getClass().getSimpleName());
      if (o instanceof Variable) {
        Field f = o.getClass().getDeclaredField("identifier");
        f.setAccessible(true);
        String ident = (String) f.get(o);
        System.out.println("($"+ident+")");
      } else if (o instanceof Map.Entry) {
        printObjectTree(((Entry) o).getKey());
        System.out.print(idnt(idnt)+"         ->");
        printObjectTree(((Entry) o).getValue());
        System.out.println();
      } else {
        System.out.println();
      }
      idnt++;
      for (Field f : fields) {
        if (!Modifier.isStatic(f.getModifiers()) || !Modifier.isFinal(f.getModifiers())) {
          System.out.print(idnt(idnt) + f.getName() + ":");
          idnt++;
          printObjectTree(f.get(o), idnt);
          idnt--;
        }//else static final fields are ignored
      }
    } catch (IllegalAccessException | NoSuchFieldException e) {
      System.out.print("#");
    }
  }

}
