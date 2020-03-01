package uk.ac.cam.gp.charlie.datalog.tests;

import static org.junit.Assert.assertEquals;

import abcdatalog.ast.Clause;
import abcdatalog.ast.PositiveAtom;
import com.google.common.collect.Lists;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.ConstantValue;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.Variable;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefine;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsert;
import uk.ac.cam.gp.charlie.ast.queries.match.ConditionIsa;
import uk.ac.cam.gp.charlie.ast.queries.match.QueryMatch;
import uk.ac.cam.gp.charlie.datalog.interpreter.ASTInterpreter;
import uk.ac.cam.gp.charlie.datalog.interpreter.Context;

/**
 * @author hrjh2@cam.ac.uk
 */
public class TESTAstInterpreter {

  public void assertDatalogEqual(Set<Clause> datalog_parsed, Set<String> datalog_raw) {
    Set<String> mutableDatalog = new HashSet<>(datalog_raw);
    for (Clause c : datalog_parsed) {
      String c_raw = c.toString();
      if (mutableDatalog.contains(c_raw)) {
        mutableDatalog.remove(c_raw);
      } else {
        throw new RuntimeException("Datalog not equal:\n  Actual Clause:"+c_raw+"\n  Remaining Clauses:"+mutableDatalog);
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

  @Test
  public void test_defineEntities_advanced() {
    //Arrange
    Set<String> datalog = new HashSet<>();
    QueryDefine q1 = new QueryDefine("person",QueryDefine.getFromIdentifier("entity"));
    datalog.add("t_subs(t_0, entity).");
    q1.attributes.add(Attribute.fromIdentifier("name"));
    datalog.add("t_hasattr(t_0, a_0).");
    q1.attributes.add(Attribute.fromIdentifier("nickname"));
    datalog.add("t_hasattr(t_0, a_1).");
    q1.plays.add(Plays.fromIdentifier("friend"));
    datalog.add("t_playsrole(t_0, r_0).");
    q1.plays.add(Plays.fromIdentifier("employee"));
    datalog.add("t_playsrole(t_0, r_1).");

    QueryDefine q2 = new QueryDefine("organisation",QueryDefine.getFromIdentifier("entity"));
    datalog.add("t_subs(t_1, entity).");
    q2.attributes.add(Attribute.fromIdentifier("name"));
    datalog.add("t_hasattr(t_1, a_0).");
    q2.attributes.add(Attribute.fromIdentifier("slogan"));
    datalog.add("t_hasattr(t_1, a_2).");
    q2.plays.add(Plays.fromIdentifier("market_agent"));
    datalog.add("t_playsrole(t_1, r_2).");
    q2.plays.add(Plays.fromIdentifier("employer"));
    datalog.add("t_playsrole(t_1, r_3).");
    Context c = new Context();
    //Act
    Set<Clause> s1 = ASTInterpreter.toDatalog(q1,c);
    Set<Clause> s2 = ASTInterpreter.toDatalog(q2,c);
    s1.addAll(s2);
    //Assert
    assertDatalogEqual(s1,datalog);
  }

  @Test
  public void test_defineMultiple_advanced() {
    //Arrange
    Set<String> datalog = new HashSet<>();
    QueryDefine q1 = new QueryDefine("person",QueryDefine.getFromIdentifier("entity"));
    datalog.add("t_subs(t_0, entity).");
    q1.attributes.add(Attribute.fromIdentifier("name"));
    datalog.add("t_hasattr(t_0, a_0).");
    q1.attributes.add(Attribute.fromIdentifier("nickname"));
    datalog.add("t_hasattr(t_0, a_1).");
    q1.plays.add(Plays.fromIdentifier("friend"));
    datalog.add("t_playsrole(t_0, r_0).");
    q1.plays.add(Plays.fromIdentifier("employee"));
    datalog.add("t_playsrole(t_0, r_1).");

    QueryDefine q2 = new QueryDefine("organisation",QueryDefine.getFromIdentifier("entity"));
    datalog.add("t_subs(t_1, entity).");
    q2.attributes.add(Attribute.fromIdentifier("name"));
    datalog.add("t_hasattr(t_1, a_0).");
    q2.attributes.add(Attribute.fromIdentifier("slogan"));
    datalog.add("t_hasattr(t_1, a_2).");
    q2.plays.add(Plays.fromIdentifier("market_agent"));
    datalog.add("t_playsrole(t_1, r_2).");
    q2.plays.add(Plays.fromIdentifier("employer"));
    datalog.add("t_playsrole(t_1, r_3).");

    QueryDefine q3 = new QueryDefine("coworkers",QueryDefine.getFromIdentifier("relation"));
    datalog.add("t_subs(t_2, relation).");
    q3.relates.add(Plays.fromIdentifier("employee"));
    datalog.add("t_relates(t_2, r_1).");

    QueryDefine q4 = new QueryDefine("friends",QueryDefine.getFromIdentifier("relation"));
    datalog.add("t_subs(t_3, relation).");
    q4.relates.add(Plays.fromIdentifier("friend"));
    datalog.add("t_relates(t_3, r_0).");

    Context c = new Context();
    //Act
    Set<Clause> s1 = ASTInterpreter.toDatalog(q1,c);
    Set<Clause> s2 = ASTInterpreter.toDatalog(q2,c);
    Set<Clause> s3 = ASTInterpreter.toDatalog(q3,c);
    Set<Clause> s4 = ASTInterpreter.toDatalog(q4,c);
    s1.addAll(s2);
    s1.addAll(s3);
    s1.addAll(s4);
    //Assert
    assertDatalogEqual(s1,datalog);
  }

  public static List<Query> getTestEnv1() {
    List<Query> toRun = new ArrayList<>();

    //
    //SCHEMA
    //
    Attribute nameAttribute = Attribute.fromIdentifier("name");

    Plays employee = Plays.fromIdentifier("employee");
//    Plays ceo = Plays.fromIdentifier("ceo");
    Plays employer = Plays.fromIdentifier("employer");
    Plays friend = Plays.fromIdentifier("friend");
//    Plays lover = Plays.fromIdentifier("lover");

    QueryDefine person = new QueryDefine("person",QueryDefine.getFromIdentifier("entity"));
    person.attributes.add(nameAttribute);
    person.plays.add(employee);
    person.plays.add(friend);
    toRun.add(person);

    QueryDefine organisation = new QueryDefine("organisation",QueryDefine.getFromIdentifier("entity"));
    organisation.attributes.add(nameAttribute);
    organisation.plays.add(employer);
    toRun.add(organisation);

    QueryDefine employment = new QueryDefine("employment",QueryDefine.getFromIdentifier("relation"));
    employment.relates.add(employee);
    employment.relates.add(employer);
    toRun.add(employment);

    QueryDefine coworkers = new QueryDefine("coworkers",QueryDefine.getFromIdentifier("relation"));
    coworkers.relates.add(employee);
    toRun.add(coworkers);

    QueryDefine friendship = new QueryDefine("friendship",QueryDefine.getFromIdentifier("relation"));
    friendship.relates.add(friend);
    toRun.add(friendship);
    //
    // DATA
    //
    Variable var_y = Variable.fromIdentifier("y");
    Variable var_x = Variable.fromIdentifier("x");
    Variable var_z = Variable.fromIdentifier("z");

    QueryInsert insert = new QueryInsert(var_y, "organisation");
    insert.attributes.put(Attribute.fromIdentifier("name"), ConstantValue.fromValue("Uni"));
    toRun.add(insert);

    insert = new QueryInsert(var_x, "person");
    insert.attributes.put(Attribute.fromIdentifier("name"), ConstantValue.fromValue("Alice"));
    toRun.add(insert);

    insert = new QueryInsert(var_x, "person");
    insert.attributes.put(Attribute.fromIdentifier("name"), ConstantValue.fromValue("Bob"));
    toRun.add(insert);

    insert = new QueryInsert(var_x, "person");
    insert.attributes.put(Attribute.fromIdentifier("name"), ConstantValue.fromValue("Charlie"));
    toRun.add(insert);

    insert = new QueryInsert(var_x, "person");
    insert.attributes.put(Attribute.fromIdentifier("name"), ConstantValue.fromValue("Dhruv"));
    toRun.add(insert);

    insert = new QueryInsert(var_x, "person");
    insert.attributes.put(Attribute.fromIdentifier("name"), ConstantValue.fromValue("Ellie"));
    toRun.add(insert);


    insert = new QueryInsert(var_z, "employment"); //employ ellie at uni
    insert.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employer"), var_y));
    insert.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employee"), var_x));
    toRun.add(insert);

    //Make alice and bob friends
    QueryInsert insertQuery = new QueryInsert(var_z, "friendship");
    insertQuery.plays.add(new SimpleEntry<>(Plays.fromIdentifier("friend"), var_x));
    insertQuery.plays.add(new SimpleEntry<>(Plays.fromIdentifier("friend"), var_y));
    QueryMatch match = new QueryMatch();
    match.setActionInsert(insertQuery);
    ConditionIsa cond_1 = new ConditionIsa(var_x, "person");
    cond_1.has.put(Attribute.fromIdentifier("name"), ConstantValue.fromValue("Bob"));
    ConditionIsa cond_2 = new ConditionIsa(var_y, "person");
    cond_2.has.put(Attribute.fromIdentifier("name"), ConstantValue.fromValue("Alice"));
    match.conditions.add(cond_1);
    match.conditions.add(cond_2);
    toRun.add(match);

    //Make everyone coworkers
    insertQuery = new QueryInsert(var_z, "coworkers");
    insertQuery.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employee"), var_x));
    insertQuery.plays.add(new SimpleEntry<>(Plays.fromIdentifier("employee"), var_y));
    match = new QueryMatch();
    match.setActionInsert(insertQuery);
    cond_1 = new ConditionIsa(var_x, "person");
    cond_2 = new ConditionIsa(var_y, "person");
    match.conditions.add(cond_1);
    match.conditions.add(cond_2);
    toRun.add(match);

    return toRun;
  }

  private Context setupTestEnv1() {
    List<Query> toRun = getTestEnv1();

    Context c = new Context();
    for(Query q : toRun) {
      ASTInterpreter.toDatalog(q,c);
    }
    return c;
  }

  @Test
  public void test_env1_peopleExist() {
    //Arrange
    Context c = setupTestEnv1();
    Set<String> datalog = new HashSet<>();

    Variable var_n = Variable.fromIdentifier("n");
    Variable var_x = Variable.fromIdentifier("x");
    QueryMatch q = new QueryMatch();
    q.setActionGet(Lists.asList(var_n,new Variable[0]));
    ConditionIsa cond = new ConditionIsa(var_x,"person");
    cond.has.put(Attribute.fromIdentifier("name"),var_n);
    q.conditions.add(cond);
    datalog.add("query(Var0, Var1) :- instanceof(Var0, t_0), instanceattr(Var0, a_0, Var1).");
    //Act
    Set<Clause> s = ASTInterpreter.toDatalog(q,c);
    PositiveAtom pa = ASTInterpreter.toExecutableDatalog(q,c);
    //Assert
    assertDatalogEqual(s,datalog);
    assertEquals(pa.toString(),"query(Var0, Var1)");
  }

}
