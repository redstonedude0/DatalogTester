package uk.ac.cam.gp.charlie.graql.parsing;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.ac.cam.gp.charlie.DebugHelper;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.AttributeValue;
import uk.ac.cam.gp.charlie.ast.Plays;
import uk.ac.cam.gp.charlie.ast.Variable;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefine;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefineRule;
import uk.ac.cam.gp.charlie.ast.queries.QueryInsert;
import uk.ac.cam.gp.charlie.ast.queries.match.ConditionIsa;
import uk.ac.cam.gp.charlie.ast.queries.match.ConditionNeq;
import uk.ac.cam.gp.charlie.ast.queries.match.MatchCondition;
import uk.ac.cam.gp.charlie.ast.queries.match.QueryMatch;

public class RegexParser extends GraqlParser {

  public static void main(String[] args) {
    int mode = 4;
    RegexParser re = new RegexParser();
    List<Query> test=null;
    String graql = "";
    if (mode == 0) {
      graql = "define person sub entity, has name;";
    }
    if (mode == 1) {
      graql = "define rule-a sub rule, when { $x isa person; $y isa person; }, then {(friend:$x,friend:$y) isa friends;};";
    }
    if (mode == 2) {
      graql = "match $x isa person, has name $n; get $n;";
    }
    if (mode == 4) {
      graql = "match (employee: $x) isa employment; delete $x;";
    }
    if (mode == 3) {
      graql = "match $x isa person, has name $n; get $n;";
      Iterator<MatchResult> s = iterate(matcher(graql,"<match>"));
      s.forEachRemaining(m -> System.out.println(m.start()+":"+m.end()));
    }
    test = re.graqlToAST(graql);
    DebugHelper.printObjectTree(test);
  }

  private static String regex(String regex, String... tags) {
    //Create a list of tags this regex may have, all not in the 'tags' param will be deleted from the string
    List<String> registeredTags = new ArrayList<>();
    regex = regex.replace("<block>","(<define_block>|<insert_block>|<match>)");



    //<editor-fold desc="Match statements">
    regex = regex.replace("<match>","(<wso>match<ws>(?<MATCHCONDS>(<wso><match_condition>)+)<wso>(?<MATCHACT><match_action>))");
    registeredTags.add("MATCHCONDS");
    registeredTags.add("MATCHACT");
    regex = regex.replace("<match_condition>","(<neq_condition>|<isa_has_condition>|<rel_ent_isa_condition>|<rel_isa_condition>)");
    regex = regex.replace("<match_action>","(<get_all>|<get_some>|<insert_block>|<delete>)");

    regex = regex.replace("<get_all>","(get<wso>;)");
    regex = regex.replace("<get_some>","(get<wso><variable>(<wso>,<wso><variable>)*<wso>;)");
    regex = regex.replace("<delete>","(delete<ws><variable><wso>;)");
    //</editor-fold>


    //<editor-fold desc="Insert statements">
    regex = regex.replace("<insert_block>","(NONMATCHINGSEQUENCE)");//placeholder
    //</editor-fold>

    //<editor-fold desc="Conditions (for Matches and Rules)">
    regex = regex.replace("<isa_has_condition>","((?<ISAHAS1><variable>)<ws>isa<ws>(?<ISAHAS2><identifier>)(,<wso><has_subcondition>)*;)");
    registeredTags.add("ISAHAS1");
    registeredTags.add("ISAHAS2");
    //</editor-fold>

    regex = regex.replace("<define_block>","(<wso>define(<ws>(<define>|<define_rule>))+)");
    //<editor-fold desc="Defines for rules">
    regex = regex.replace("<define_rule>","((?<DEFINERULEIDENT><identifier>)<ws>sub<ws>(?<DEFINERULESUBS><identifier>)<wso>,<wso>when<wso><when_block><wso>,<wso>then<wso><then_block><wso>;)");
    registeredTags.add("DEFINERULEIDENT");
    registeredTags.add("DEFINERULESUBS");
    regex = regex.replace("<when_block>","(\\{(<wso><when_condition>)*<wso>})");
    regex = regex.replace("<then_block>","(\\{<wso><rel_isa_condition><wso>})");
    regex = regex.replace("<when_condition>","(<rel_isa_condition>|<ent_isa_condition>|<rel_ent_isa_condition>|<has_condition>|<neq_condition>)");
    regex = regex.replace("<has_condition>","((?<HAS1><variable>)<ws><has_subcondition>(,<wso><has_subcondition>)*;)");
    registeredTags.add("HAS1");
    regex = regex.replace("<has_subcondition>","(has<ws>(?<HASSUB1><identifier>)<ws>(?<HASSUB2><variable>)<wso>)");
    registeredTags.add("HASSUB1");
    registeredTags.add("HASSUB2");
    //</editor-fold>

    regex = regex.replace("<neq_condition>","((?<NEQ1><variable>)<wso>!=<wso>(?<NEQ2><variable>)<wso>;)");
    registeredTags.add("NEQ1");
    registeredTags.add("NEQ2");
    regex = regex.replace("<ent_isa_condition>","((?<ENT1><variable>)<ws>isa<ws>(?<ENT2><identifier>)<wso>;)");
    registeredTags.add("ENT1");
    registeredTags.add("ENT2");
    regex = regex.replace("<rel_ent_isa_condition>","((?<REL2><variable>)<wso><rel_isa_condition>)");
    registeredTags.add("REL2");
    regex = regex.replace("<rel_isa_condition>","(<relation_entry><wso>isa<ws>(?<REL1><identifier>)<wso>;)");
    registeredTags.add("REL1");
    regex = regex.replace("<relation_entry>","(\\(<relation_subentry>(,<wso><relation_subentry>)*\\))");
    regex = regex.replace("<relation_subentry>","(<wso>(?<RELSUB1><identifier>)<wso>:<wso>(?<RELSUB2><variable>)<wso>)");
    registeredTags.add("RELSUB1");
    registeredTags.add("RELSUB2");
    //</editor-fold>


    //<editor-fold desc="Defines for entities and relations">
    regex = regex.replace("<define>","(((?<DEFINEHEADIDENT><identifier>)<ws>sub<ws>(?<DEFINEHEADSUBS><identifier>))(<define_has>|<define_plays>|<define_relates>)*<wso>;)");
    registeredTags.add("DEFINEHEADIDENT");
    registeredTags.add("DEFINEHEADSUBS");
    //'relates' definition
    regex = regex.replace("<define_relates>","(<wso>,<ws>relates<ws><identifier>)");
    //'plays' definition
    regex = regex.replace("<define_plays>","(<wso>,<ws>plays<ws><identifier>)");
    //'has' definition
    regex = regex.replace("<define_has>","(<wso>,<ws>has<ws>(?<DEFINEHASIDENT><identifier>))");
    registeredTags.add("DEFINEHASIDENT");
    //</editor-fold>


    //<editor-fold desc="Basic Definitions (variables, identifiers, whitespace, etc)">
    //Variable - $<identifier>
    regex = regex.replace("<variable>","(\\$(?<VARIABLE><identifier>))");
    registeredTags.add("VARIABLE");
    //Identifier - <alpha><alphanum>*
    regex = regex.replace("<identifier>","(<alpha>(<alphanum>)*)");
    //Alphanumeric character (or _,-)
    regex = regex.replace("<alphanum>","(<alpha>|<num>|-)");
    //Numeric character
    regex = regex.replace("<num>","([0-9])");
    //Alphabetic (or _) character
    regex = regex.replace("<alpha>","([a-z]|[A-Z]|_)");
    //Whitespace (optional)
    regex = regex.replace("<wso>","(<ws>)?");
    //One or more whitespace
    regex = regex.replace("<ws>","( |\n|\r)+");
    //</editor-fold>

    for(String tag: tags) {
      registeredTags.remove(tag);
    }
    for(String tag: registeredTags) {
      regex = regex.replace("?<"+tag+">","");
    }
    return regex;
  }

  private static Matcher matcher(String graql, String regex, String... tags) {
    Matcher m = Pattern.compile(regex(regex,tags)).matcher(graql);
    return m;
  }

  private static Iterator<MatchResult> iterate(Matcher m) {
    List<MatchResult> matches = new ArrayList<>();
    while (m.find()) {
      matches.add(m.toMatchResult());
    }
    return matches.iterator();
  }

  @Override
  public List<Query> graqlToAST(String graql) {
    List<Query> toRet = new ArrayList<>();
    Iterator<MatchResult> s = iterate(matcher(graql,"<block>"));
    s.forEachRemaining(matchResult -> toRet.addAll(parseBlock(graql.substring(matchResult.start(), matchResult.end()))));
    return toRet;
  }

  private static List<Query> parseBlock(String graql) {
    List<Query> toRet = new ArrayList<>();
    Iterator<MatchResult> s;
    s = iterate(matcher(graql,"<define_block>"));
    s.forEachRemaining(matchResult -> toRet.addAll(parseDefineBlock(graql.substring(matchResult.start(), matchResult.end()))));
    s = iterate(matcher(graql,"<insert_block>"));
//    s.forEachRemaining(matchResult -> toRet.addAll(parseInsertBlock(graql.substring(matchResult.start(), matchResult.end()))));
    s = iterate(matcher(graql,"<match>"));
    s.forEachRemaining(matchResult -> toRet.add(parseMatch(graql.substring(matchResult.start(), matchResult.end()))));
    return toRet;
  }

  //<editor-fold desc="Parsing for match">
  private static QueryMatch parseMatch(String graql) {
    QueryMatch q = new QueryMatch();
    Matcher m = matcher(graql,"<match>","MATCHCONDS");
    m.matches();
    String conds = m.group("MATCHCONDS");
    q.conditions = parseMatchConditions(conds);

    m = matcher(graql,"<match>","MATCHACT");
    m.matches();
    String action = m.group("MATCHACT");
    parseMatchAction(q,action);
    return q;
  }

  private static void parseMatchAction(QueryMatch q, String graql) {
    Matcher m = matcher(graql,"<get_all>");
    if (m.matches()) {
      q.setActionGet(new ArrayList<>());
      return;
    }
    m = matcher(graql,"<delete>");
    if (m.matches()) {
      m = matcher(graql,"<delete>","VARIABLE");
      m.matches();
      String var = m.group("VARIABLE");
      q.setActionDelete(Variable.fromIdentifier(var));
      return;
    }
    m = matcher(graql,"<get_some>");
    if (m.matches()) {
      List<Variable> toAction = new ArrayList<>();
      Iterator<MatchResult> s = iterate(matcher(graql,"<variable>","VARIABLE"));
      s.forEachRemaining(matchResult -> {
        String var = graql.substring(matchResult.start(),matchResult.end());
        toAction.add(Variable.fromIdentifier(var.substring(1)));
      });
      q.setActionGet(toAction);
      return;
    }
    m = matcher(graql,"<insert_block>");
    if (m.matches()) {
//      q.setActionInsert(parseInsertBlock());
      return;
    }
    throw new RuntimeException("Match Action didn't match regex");
  }

  private static List<MatchCondition> parseMatchConditions(String graql) {
    List<MatchCondition> toRet = new ArrayList<>();
    Iterator<MatchResult> s;
    //<neq_condition>|<isa_has_condition>|<rel_ent_isa_condition>|<rel_isa_condition>
    s = iterate(matcher(graql,"<rel_isa_condition>"));
    s.forEachRemaining(matchResult -> toRet.add(parseCondition_isa_rel(null,graql.substring(matchResult.start(),matchResult.end()))));
    s = iterate(matcher(graql,"<isa_has_condition>"));
    s.forEachRemaining(matchResult -> toRet.add(parseCondition_isa_has(graql.substring(matchResult.start(),matchResult.end()))));
    s = iterate(matcher(graql,"<rel_ent_isa_condition>"));
    s.forEachRemaining(matchResult -> toRet.add(parseCondition_isa_relent(graql.substring(matchResult.start(),matchResult.end()))));
    s = iterate(matcher(graql,"<neq_condition>"));
    s.forEachRemaining(matchResult -> toRet.add(parseCondition_neq(graql.substring(matchResult.start(),matchResult.end()))));
    return toRet;
  }

  //</editor-fold>

  //<editor-fold desc="Parsing for define_block">
  private static List<Query> parseDefineBlock(String graql) {
    List<Query> toRet = new ArrayList<>();
    Iterator<MatchResult> s = iterate(matcher(graql,"<define>"));
    s.forEachRemaining(matchResult -> toRet.add(parseDefine(graql.substring(matchResult.start(),matchResult.end()))));
    s = iterate(matcher(graql,"<define_rule>"));
    s.forEachRemaining(matchResult -> toRet.add(parseDefineRule(graql.substring(matchResult.start(),matchResult.end()))));
    return toRet;
  }

  private static QueryDefine parseDefine(String graql) {
    Matcher m = matcher(graql,"<define>","DEFINEHEADIDENT","DEFINEHEADSUBS");
    m.matches();
    String ident = m.group("DEFINEHEADIDENT");//"person"
    String subs = m.group("DEFINEHEADSUBS");//"entity"
    QueryDefine q = new QueryDefine(ident,QueryDefine.getFromIdentifier(subs));
    Iterator<MatchResult> s = iterate(matcher(graql,"<define_has>"));
    s.forEachRemaining(matchResult -> q.attributes.add(parseDefineHas(graql.substring(matchResult.start(),matchResult.end()))));
    return q;
  }

  private static Attribute parseDefineHas(String graql) {
    Matcher m = matcher(graql,"<define_has>","DEFINEHASIDENT");
    m.matches();
    String ident = m.group("DEFINEHASIDENT");//"name"
    return Attribute.fromIdentifier(ident);
  }


  private static QueryDefineRule parseDefineRule(String graql) {
    Matcher m = matcher(graql,"<define_rule>","DEFINERULEIDENT","DEFINERULESUBS");
    m.matches();
    String ident = m.group("DEFINERULEIDENT");//"person"
    String subs = m.group("DEFINERULESUBS");//"rule"
    QueryDefineRule q = new QueryDefineRule(ident,QueryDefineRule.getFromIdentifier(subs));
    Iterator<MatchResult> s = iterate(matcher(graql,"<when_block>"));
    MatchResult whenBlock = s.next();
    q.when = parseWhenBlock(graql.substring(whenBlock.start(),whenBlock.end()));
    s = iterate(matcher(graql,"<then_block>"));
    MatchResult thenBlock = s.next();
    q.then = parseThenBlock(graql.substring(thenBlock.start(),thenBlock.end()));
    return q;
  }

  private static QueryInsert parseThenBlock(String graql) {
    Iterator<MatchResult> s = iterate(matcher(graql,"<rel_isa_condition>"));
    MatchResult relIsaCond = s.next();
    ConditionIsa cond = parseCondition_isa_rel(null,graql.substring(relIsaCond.start(),relIsaCond.end()));
    QueryInsert q = new QueryInsert(null,cond.type);
    q.plays = cond.relates;
    //not has for now?
    return q;
  }

  private static List<MatchCondition> parseWhenBlock(String graql) {
    List<MatchCondition> toRet = new ArrayList<>();
    Iterator<MatchResult> s;
    //<rel_isa_condition>|<ent_isa_condition>|<rel_ent_isa_condition>|<has_condition>|<neq_condition>
    s = iterate(matcher(graql,"<rel_isa_condition>"));
    s.forEachRemaining(matchResult -> toRet.add(parseCondition_isa_rel(null,graql.substring(matchResult.start(),matchResult.end()))));
    s = iterate(matcher(graql,"<ent_isa_condition>"));
    s.forEachRemaining(matchResult -> toRet.add(parseCondition_isa_ent(graql.substring(matchResult.start(),matchResult.end()))));
    s = iterate(matcher(graql,"<rel_ent_isa_condition>"));
    s.forEachRemaining(matchResult -> toRet.add(parseCondition_isa_relent(graql.substring(matchResult.start(),matchResult.end()))));
    s = iterate(matcher(graql,"<has_condition>"));
    s.forEachRemaining(matchResult -> toRet.add(parseCondition_has(graql.substring(matchResult.start(),matchResult.end()))));
    s = iterate(matcher(graql,"<neq_condition>"));
    s.forEachRemaining(matchResult -> toRet.add(parseCondition_neq(graql.substring(matchResult.start(),matchResult.end()))));
    return toRet;
  }
  //</editor-fold>

  private static ConditionNeq parseCondition_neq(String graql) {
    //$v1 != $v2;
    Matcher m = matcher(graql,"<neq_condition>","NEQ1","NEQ2");
    m.matches();
    String lhs = m.group("NEQ1");//"$v1"
    lhs = lhs.substring(1);
    String rhs = m.group("NEQ2");//"$v2"
    rhs = rhs.substring(1);
    ConditionNeq cond = new ConditionNeq(Variable.fromIdentifier(lhs),Variable.fromIdentifier(rhs));
    return cond;
  }

  private static ConditionIsa parseCondition_isa_ent(String graql) {
    //$v1 isa type1;
    Matcher m = matcher(graql,"<ent_isa_condition>","ENT1","ENT2");
    m.matches();
    String var = m.group("ENT1");//"$v1"
    var = var.substring(1);
    String type = m.group("ENT2");//"type1"
    ConditionIsa cond = new ConditionIsa(Variable.fromIdentifier(var),type);
    return cond;
  }

  private static ConditionIsa parseCondition_isa_has(String graql) {
    //$p isa person, has full-name $n;
    Matcher m = matcher(graql,"<isa_has_condition>","ISAHAS1","ISAHAS2");
    m.matches();
    String var = m.group("ISAHAS1");//"$p"
    var = var.substring(1);
    String type = m.group("ISAHAS2");//"person"
    ConditionIsa cond = new ConditionIsa(Variable.fromIdentifier(var),type);
    Iterator<MatchResult> s = iterate(matcher(graql,"<has_subcondition>"));
    s.forEachRemaining(matchResult -> {
      Entry<Attribute,AttributeValue> entry = parseCondition_hassubentry(graql.substring(matchResult.start(),matchResult.end()));
      cond.has.put(entry.getKey(),entry.getValue());
    });
    return cond;
  }


  private static ConditionIsa parseCondition_isa_rel(Variable retVar,String graql) {
    //(lab1: $v1, lab2: $v2) isa type1;
    Matcher m = matcher(graql,"<rel_isa_condition>","REL1");
    m.matches();
    String type = m.group("REL1");//"type1"
    ConditionIsa cond = new ConditionIsa(retVar,type);
    Iterator<MatchResult> s = iterate(matcher(graql,"<relation_subentry>"));
    s.forEachRemaining(matchResult -> cond.relates.add(parseCondition_relationsubentry(graql.substring(matchResult.start(),matchResult.end()))));
    return cond;
  }

  private static ConditionIsa parseCondition_isa_relent(String graql) {
    //$v1 (lab1: $v2) isa type1;
    Matcher m = matcher(graql,"<rel_ent_isa_condition>","REL2");
    m.matches();
    String var = m.group("REL2");//"var"
    var = var.substring(1);
    return parseCondition_isa_rel(Variable.fromIdentifier(var),graql);
  }

  private static Entry<Plays,Variable> parseCondition_relationsubentry(String graql) {
    //lab1: $v1
    Matcher m = matcher(graql,"<relation_subentry>","RELSUB1","RELSUB2");
    m.matches();
    String lab = m.group("RELSUB1");//"lab1"
    String var = m.group("RELSUB2");//"$v1"
    var = var.substring(1);
    return new SimpleEntry<>(Plays.fromIdentifier(lab), Variable.fromIdentifier(var));
  }

  private static ConditionIsa parseCondition_has(String graql) {
    //$v1 has n1 $v2, has n2 $v3;
    Matcher m = matcher(graql,"<has_condition>","HAS1");
    m.matches();
    String var = m.group("HAS1");//"$v1"
    var = var.substring(1);
    ConditionIsa cond = new ConditionIsa(Variable.fromIdentifier(var),"thing");
    Iterator<MatchResult> s = iterate(matcher(graql,"<has_subcondition>"));
    s.forEachRemaining(matchResult -> {
      Entry<Attribute,AttributeValue> entry = parseCondition_hassubentry(graql.substring(matchResult.start(),matchResult.end()));
      cond.has.put(entry.getKey(),entry.getValue());
    });
    return cond;
  }

  private static Entry<Attribute, AttributeValue> parseCondition_hassubentry(String graql) {
    //has n1 $v2
    Matcher m = matcher(graql,"<has_subcondition>","HASSUB1","HASSUB2");
    m.matches();
    String lab = m.group("HASSUB1");//"v1"
    String var = m.group("HASSUB2");//"$v2"
    var = var.substring(1);
    return new SimpleEntry<>(Attribute.fromIdentifier(lab),Variable.fromIdentifier(var));
  }


}
