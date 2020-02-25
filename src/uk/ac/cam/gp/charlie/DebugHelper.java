package uk.ac.cam.gp.charlie;

import abcdatalog.util.substitution.Substitution;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.LoggerFactory;
import uk.ac.cam.gp.charlie.ast.Variable;
import uk.ac.cam.gp.charlie.ast.queries.match.QueryMatch;
import uk.ac.cam.gp.charlie.ast.queries.match.QueryMatch.Action;

/**
 * Class to help with debugging, and provides interactive console output
 */
public class DebugHelper {

  //global variables used to enable/disable verbose output across several classes
  public static boolean VERBOSE_AST = false;
  public static boolean VERBOSE_DATALOG = false;
  public static boolean VERBOSE_RESULTS = false;
  public static boolean VERBOSE_GRAQL = false;
  public static boolean VERBOSE_GRAQL_INPUT = true;
  public static boolean DUMP_DATALOG_ON_RESULT = false;

  /**
   * This will truncate the tree to depth 10, use this to print a tree in a (nice enough) way
   */
  public static void printObjectTree(Object o) {
    printObjectTree(o, 0,10);
  }

  //print out indent as necessary
  private static String idnt(int idnt) {
    StringBuilder s = new StringBuilder();
    for (int i = 0; i < idnt; i++) {
      s.append("  ");
    }
    return s.toString();
  }


  /**
   * Print the object as a tree
   */
  private static void printObjectTree(Object o, int idnt, int cycles) {
    cycles--;
    //ASSERT: The cursor is on a line, at the correct indent level for text to start,
    //idnt(idnt) will align text as recommended by the parent,
    //this returns at end of written line to the parent (the parent should newline if necessary)
    if (cycles == 0) {
      System.out.print("[Maximum output depth reached for AST]");
      return;
    }
    try { //specific classes has custom output
      if (o instanceof List) {
        List l = (List) o;
        if (l.size() == 0) {
          System.out.println("[]");
          return;
        }
        System.out.println("[");
        for (Object item : l) {
          System.out.print(idnt(idnt));
          printObjectTree(item, idnt,cycles);
          System.out.println();
        }
        idnt--;
        System.out.print(idnt(idnt) + "]");
        return;
      }
      if (o instanceof Map) {
        Map m = (Map) o;
        if (m.size() == 0) {
          System.out.print("{}");
          return;
        }
        System.out.println("{");
        for (Object item : m.entrySet()) {
          System.out.print(idnt(idnt));
          printObjectTree(item, idnt,cycles);
          System.out.println();
        }
        idnt--;
        System.out.print(idnt(idnt) + "}");
        return;
      }
      if (o instanceof String) {
        System.out.print("\"" + o + "\"");
        return;
      }
      if (o == null) {
        System.out.print("<<null>>");
        return;
      }
      //Else arbitrary class, with some fields.
      Field[] fields = o.getClass().getFields();
      //Print the class name
      System.out.print(o.getClass().getSimpleName());
      //If simple classes, or special classes, display content in a particular way
      if (o instanceof Variable) {
        Field f = o.getClass().getDeclaredField("identifier");
        f.setAccessible(true);
        String ident = (String) f.get(o);
        System.out.print("($"+ident+")");
        return;
      } else if (o instanceof Map.Entry) {
        printObjectTree(((Entry) o).getKey(),idnt,cycles);
        System.out.print("->");
        printObjectTree(((Entry) o).getValue(),idnt,cycles);
        return;
      } else if (o instanceof QueryMatch) {
        Field f = o.getClass().getDeclaredField("action");
        f.setAccessible(true);
        Action acc = ((Action) f.get(o));
        System.out.print("("+acc.name()+") ");
        switch (acc) {
          case GET:
            printObjectTree(((QueryMatch)o).getDATA_GET(),idnt+1,cycles);
            break;
          case DELETE:
            printObjectTree(((QueryMatch)o).getDATA_DELETE(),idnt+1,cycles);
            break;
          case INSERT:
            printObjectTree(((QueryMatch)o).getDATA_INSERT(),idnt+1,cycles);
            break;
        }
        System.out.println();
        System.out.print(idnt(idnt)+"MATCH ");
        printObjectTree(((QueryMatch) o).conditions,idnt+1,cycles);
        return;
      } else {
        //not a known class, dump fields
        //only 1 field - display inline
        if (fields.length == 1) {
          if (fields[0].get(o) instanceof String) {
            System.out.print("{" + ((String) fields[0].get(o))
                    .replaceAll("\n", "\\n") + "}");
            return;
          }
        }
        //otherwise multiple fields, display in a block
        System.out.println(" {");
        for (Field f : fields) {
          if (!Modifier.isStatic(f.getModifiers()) || !Modifier.isFinal(f.getModifiers())) {
            System.out.print(idnt(idnt+1) + f.getName() + ":");
            printObjectTree(f.get(o), idnt+2,cycles);
            System.out.println();
          }//else static final fields are ignored
        }
        System.out.print(idnt(idnt)+"}");
      }
    } catch (IllegalAccessException | NoSuchFieldException e) {
      System.out.print("#ERROR WHILE FETCHING#");
    }
  }

//  public static PrintStream out = null;
  public static Level loggerLevel = null;
  public static void absorbOutput() {
    Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.setLevel(Level.OFF);
//    out = System.out;
//    System.setOut(new PrintStream(new ByteArrayOutputStream()));
  }
  public static void restoreOutput() {
    if (loggerLevel != null) {
      Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
      logger.setLevel(loggerLevel);
    }
//    if (out != null) {
//      System.setOut(out);
//      out = null;
//    }
  }

}
