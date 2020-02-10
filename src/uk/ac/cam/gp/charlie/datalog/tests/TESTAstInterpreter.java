package uk.ac.cam.gp.charlie.datalog.tests;

import abcdatalog.ast.Clause;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefine;
import uk.ac.cam.gp.charlie.datalog.interpreter.ASTInterpreter;
import uk.ac.cam.gp.charlie.datalog.interpreter.Context;

public class TESTAstInterpreter {

  public void assertDatalogEqual(Set<Clause> datalog_parsed, Set<String> datalog_raw) {
    Set<String> mutableDatalog = new HashSet<>(datalog_raw);
    for (Clause c : datalog_parsed) {
      String c_raw = c.toString();
      if (mutableDatalog.contains(c_raw)) {
        mutableDatalog.remove(c_raw);
      } else {
        throw new RuntimeException("Datalog not equal:\n  Actual Clause:"+c_raw);
      }
    }
    if (mutableDatalog.size() != 0) {
      throw new RuntimeException("Datalog not equal:\n  Missing clauses:"+mutableDatalog);
    }
  }

  @Test
  public void test_defineEntity_basic() {
    //Arrange
    Set<String> datalog = new HashSet<>();
    QueryDefine q = new QueryDefine("person",QueryDefine.getFromIdentifier("entity"));
    datalog.add("t_subs(t_0, entity).");
    //Act
    Set<Clause> s = ASTInterpreter.toDatalog(q,new Context());
    //Assert
    assertDatalogEqual(s,datalog);
  }

  @Test
  public void test_defineEntity_advanced() {
    //Arrange
    Set<String> datalog = new HashSet<>();
    QueryDefine q = new QueryDefine("person",QueryDefine.getFromIdentifier("entity"));
    datalog.add("t_subs(t_0, entity).");
    q.attributes.add(Attribute.fromIdentifier("name"));
    datalog.add("t_hasattr(t_0, a_0).");
    q.attributes.add(Attribute.fromIdentifier("nickname"));
    datalog.add("t_hasattr(t_0, a_1).");
    q.plays.add(Plays.fromIdentifier("friend"));
    datalog.add("t_playsrole(t_0, r_0).");
    q.plays.add(Plays.fromIdentifier("employee"));
    datalog.add("t_playsrole(t_0, r_1).");
    //Act
    Set<Clause> s = ASTInterpreter.toDatalog(q,new Context());
    //Assert
    assertDatalogEqual(s,datalog);
  }

}
