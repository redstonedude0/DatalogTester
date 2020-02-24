package uk.ac.cam.gp.charlie.graql.parsing;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.ac.cam.gp.charlie.DebugHelper;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.AttributeValue;
import uk.ac.cam.gp.charlie.ast.ConstantValue;
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

/**
 * Parse Graql into an ASTE
 */
public class GraqlParser {

  //TODO remove testing method (move to unit tests)
  public static void main(String[] args) {
    boolean testA = true;
    if (testA) {
      //Charles Testing
      iterate(matcher(" $x isa person; $y isa person;", regex("(<ws>((<insert_entity>|<insert_rel>)(<wso><insert_has>)*<wso>;))+"))).forEachRemaining(new Consumer<MatchResult>() {
        @Override
        public void accept(MatchResult matchResult) {
          System.out.println(matchResult.start()+":"+matchResult.end());
        }
      });
    } else {
      //Harrison Testing
      int mode = 7;
      GraqlParser re = new GraqlParser();
      List<Query> test = null;
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
        Iterator<MatchResult> s = iterate(matcher(graql, "<match>"));
        s.forEachRemaining(m -> System.out.println(m.start() + ":" + m.end()));
      }
      if (mode == 5) {
        graql = "insert $p isa person, has name \"Bob\";";
      }
      if (mode == 6) {
        graql = "insert $x (friend:$p1, friend:$z) isa friendship;";
      }
      if (mode == 7) {
        graql = "match\n"
            + "    $x isa person, has name \"Alice\";\n"
            + "    $y (employee: $x) isa employment;\n"
            + "delete $y;";
      }
      test = re.graqlToAST(graql);
      DebugHelper.printObjectTree(test);
    }
  }

  /**
   * Parse augmented regex to regular regex
   *
   * The augmented regex allows for "<term>" tags to represent specific terms in the grammar
   *
   * See the contents of the method for a full list of terms,
   *
   * example usage: "regex("<block>")"
   * @return
   */
  private static String regex(String regex, String... tags) {
    //Create a list of tags this regex may have, all not in the 'tags' param will be deleted from the string
    List<String> registeredTags = new ArrayList<>();
    //The graql can be considered a <block>*
    regex = regex.replace("<block>*","<block>*+");//greedy
    regex = regex.replace("<block>","(<define_block>|<insert_block>|<match>)");

    //<editor-fold desc="Match statements">
    //A match statement
    regex = regex.replace("<match>","(<wso>match<ws>(?<MATCHCONDS>(<wso><match_condition>)+)<wso>(?<MATCHACT><match_action>))");
    registeredTags.add("MATCHCONDS");
    registeredTags.add("MATCHACT");
    //A condition within the 'match' body
    regex = regex.replace("<match_condition>","(<neq_condition>|<isa_has_condition>|<rel_ent_isa_condition>|<has_condition>|<rel_isa_condition>)");
    //An action to take based on the match
    regex = regex.replace("<match_action>","(<get_all>|<get_some>|<insert_block>|<delete>)");
    //Actions
    regex = regex.replace("<get_all>","(get<wso>;)");
    regex = regex.replace("<get_some>","(get<wso><variable>(<wso>,<wso><variable>)*+<wso>;)");
    regex = regex.replace("<delete>","(delete<ws><variable><wso>;)");
    //</editor-fold>

    //<editor-fold desc="Insert statements">
    //A block of insert statements
    regex = regex.replace("<insert_block>","(<wso>insert(<ws><insert>)++<wso>)");
    //A single insert statement
    regex = regex.replace("<insert>", "((<insert_entity>|<insert_rel>)(<wso><insert_has>)*<wso>;)");
    //Inserting an entity
    regex = regex.replace("<insert_entity>", "((?<INSERTHEADIDENTENT><variable>)<ws>isa<ws>(?<INSERTHEADISAENT><identifier>))");
    registeredTags.add("INSERTHEADIDENTENT");
    registeredTags.add("INSERTHEADISAENT");
    //Inserting a relation
    regex = regex.replace("<insert_rel>", "(((?<INSERTHEADIDENTREL><variable>)?)<wso><relation_entry><wso>isa<ws>(?<INSERTHEADISAREL><identifier>))");
    registeredTags.add("INSERTHEADIDENTREL");
    registeredTags.add("INSERTHEADISAREL");
    //Tags already added
    //TODO - change to use has_condition (to allow variable has'es)
    regex = regex.replace("<insert_has>", "(,<ws>has<ws>(?<INSERTHASIDENT><identifier>)<ws><literal>)");
    registeredTags.add("INSERTHASIDENT");
    //Todo - $x has attr "VAL"; needs to be added (not the same as has above!!!!)
    //TODO - $x has attr $v; also needs to be added
//    regex = regex.replace("<insert_attribute>", "(<wso>;<ws><wso>(?<INSERTATTIDENT><variable>)<ws><literal>)");
//    registeredTags.add("INSERTATTIDENT");
    //</editor-fold>

    regex = regex.replace("<define_block>","(<wso>define(<ws>(<define>|<define_rule>))+)");
    //<editor-fold desc="Defines for rules">
    regex = regex.replace("<define_rule>","((?<DEFINERULEIDENT><identifier>)<ws>sub<ws>(?<DEFINERULESUBS><identifier>)<wso>,<wso><when_block><wso>,<wso><then_block><wso>;)");
    registeredTags.add("DEFINERULEIDENT");
    registeredTags.add("DEFINERULESUBS");
    regex = regex.replace("<when_block>","(when<wso>\\{(<wso><when_condition>)*<wso>})");
    regex = regex.replace("<then_block>","(then<wso>\\{<wso><rel_isa_condition><wso>})");
    regex = regex.replace("<when_condition>","(<rel_isa_condition>|<ent_isa_condition>|<rel_ent_isa_condition>|<has_condition>|<neq_condition>)");
    //</editor-fold>

    //<editor-fold desc="Conditions (for Matches and Rules)">
    //$x isa type, has attr $val, has attr "lit";
    regex = regex.replace("<isa_has_condition>","((?<ISAHAS1><variable>)<ws>isa<ws>(?<ISAHAS2><identifier>)(,<wso><has_subcondition>)*;)");
    registeredTags.add("ISAHAS1");
    registeredTags.add("ISAHAS2");
    //has condition
    regex = regex.replace("<has_condition>","((?<HAS1><variable>)<ws><has_subcondition>(,<wso><has_subcondition>)*;)");
    registeredTags.add("HAS1");
    regex = regex.replace("<has_subcondition>","(has<ws>(?<HASSUB1><identifier>)<ws>((?<HASSUB2><variable>)|(<literal>))<wso>)");
    registeredTags.add("HASSUB1");
    registeredTags.add("HASSUB2");
    //$x != $y
    regex = regex.replace("<neq_condition>","((?<NEQ1><variable>)<wso>!=<wso>(?<NEQ2><variable>)<wso>;)");
    registeredTags.add("NEQ1");
    registeredTags.add("NEQ2");
    //$x isa type;
    regex = regex.replace("<ent_isa_condition>","((?<ENT1><variable>)<ws>isa<ws>(?<ENT2><identifier>)<wso>;)");
    registeredTags.add("ENT1");
    registeredTags.add("ENT2");
    //$x (lab1:$v1,lab2:$v2) isa type;
    regex = regex.replace("<rel_ent_isa_condition>","((?<REL2><variable>)<wso><rel_isa_condition>)");
    registeredTags.add("REL2");
    //(lab1: $v1, lab2: $v2) isa type;
    regex = regex.replace("<rel_isa_condition>","(<relation_entry><wso>isa<ws>(?<REL1><identifier>)<wso>;)");
    registeredTags.add("REL1");
    //(lab1: $v1, lab2: $v2) //[Used in above conditions]
    regex = regex.replace("<relation_entry>","(\\(<relation_subentry>(,<wso><relation_subentry>)*\\))");
    //Note that the relation_subentry permits labels to be omitted in regex,
    //however this isn't valid for an insert - only a match.
    //lab1: $v1 //[Used in above conditions]
    regex = regex.replace("<relation_subentry>","(<wso>(((?<RELSUB1><identifier>)<wso>:<wso>)?)(?<RELSUB2><variable>)<wso>)");
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
    //String literal for inserts
    regex = regex.replace("<literal>","(<string_lit>|<num_lit>|<date_lit>|<bool_lit>)");
    regex = regex.replace("<string_lit>", "(\"(?<STRINGLIT>(<char>)*+)\")");
    registeredTags.add("STRINGLIT");
    regex = regex.replace("<num_lit>", "(?<NUMLIT>((<num>|\\.)+))");
    registeredTags.add("NUMLIT");
    regex = regex.replace("<date_lit>", "(?<DATELIT>((<num>|-)+))");
    registeredTags.add("DATELIT");
    regex = regex.replace("<bool_lit>", "(?<BOOLLIT>(?i)(true|false)(?-i))");
    registeredTags.add("BOOLLIT");
    //Variable - $<identifier>
    regex = regex.replace("<variable>","(\\$(?<VARIABLE><identifier>))");
    registeredTags.add("VARIABLE");
    //Identifier - <alpha><alphanum>*
    regex = regex.replace("<identifier>","(<alpha>(<alphanum>)*+)");
    //standard character, for strings
    regex = regex.replace("<char>","(<alphanum>|<ws>|=|\\+|@|\\.|\\$)");
    //Alphanumeric character (or _,-)
    regex = regex.replace("<alphanum>","(<alpha>|<num>|-)");
    //Numeric character
    regex = regex.replace("<num>","([0-9])");
    //Alphabetic (or _) character
    regex = regex.replace("<alpha>","([a-z]|[A-Z]|_)");
    //Whitespace (optional)
    regex = regex.replace("<wso>","(<ws>)?");
    //One or more whitespace
    regex = regex.replace("<ws>","( |\n|\r|\t)+");
    //</editor-fold>

    for(String tag: tags) {
      registeredTags.remove(tag);
    }
    for(String tag: registeredTags) {
      regex = regex.replace("?<"+tag+">","");
    }
    return regex;
  }

  /**
   * Return a matcher for given graql, regex, and tags
   * @return
   */
  private static Matcher matcher(String graql, String regex, String... tags) {
    Matcher m = Pattern.compile(regex(regex,tags)).matcher(graql);
    return m;
  }

  /**
   * Return an iterator over all results of a matcher
   * @return
   */
  private static Iterator<MatchResult> iterate(Matcher m) {
    List<MatchResult> matches = new ArrayList<>();
    while (m.find()) {
      matches.add(m.toMatchResult());
    }
    return matches.iterator();
  }

  /**
   * Return a list of all matches
   * NOTE: Only gives iterative matches, not the exhaustive set, see Java regex documentation for more details
   * (e.g. I think matching "ab cd" with regex "[a-z]*" returns "ab","cd" but not "a","b","c","d",or ""
   * @return
   */
  private static List<String> matches(String graql, String regex, String... tags){
    Matcher m = matcher(graql,regex,tags);
    List<String> matches = new ArrayList<>();
    while (m.find()) {
      MatchResult mr = m.toMatchResult();
      matches.add(graql.substring(mr.start(),mr.end()));
    }
    return matches;
  }

  private static String removeComments(String graql) {
    StringBuilder ret = new StringBuilder();
    String[] parts = graql.split("\n");
    for(String s : parts) {
      if (!s.startsWith("#")) {
        ret.append(s).append("\n");
      }
    }
    return ret.toString();
  }

  /**
   * Convert a graql string into an AST (only public function in this class)
   * @param graql
   * @return
   */
  public List<Query> graqlToAST(String graql) {
    graql = removeComments(graql);
    List<Query> toRet = new ArrayList<>();
    List<String> ss = matches(graql,"<block>*");
    ss.forEach(s -> toRet.addAll(parseBlock(s)));
    return toRet;
  }

  private static List<Query> parseBlock(String graql) {
    List<Query> toRet = new ArrayList<>();
    List<String> ss;
    ss = matches(graql,"<define_block>");
    ss.forEach(s -> toRet.addAll(parseDefineBlock(s)));
    ss = matches(graql,"<insert_block>");
    ss.forEach(s -> toRet.addAll(parseInsertBlock(s)));
    ss = matches(graql,"<match>");
    ss.forEach(s -> toRet.add(parseMatch(s)));
    return toRet;
  }

  private static List<QueryInsert> parseInsertBlock(String graql) {
    List<QueryInsert> toRet = new ArrayList<>();
    List<String> ss = matches(graql,"<insert>");
    ss.forEach(s -> toRet.add(parseInsert(s)));
    return toRet;
  }

  private static QueryInsert parseInsert(String graql) {
    Matcher m = matcher(graql,"<insert>","INSERTHEADIDENTENT","INSERTHEADISAENT","INSERTHEADIDENTREL","INSERTHEADISAREL");
    m.matches();
    String subsent = m.group("INSERTHEADISAENT");//"person"
    String subsrel = m.group("INSERTHEADISAREL");//"person"
    if (subsent != null ^ subsrel == null) {
      throw new RuntimeException("Parsing error");
    }//else only 1 null
    String var= m.group("INSERTHEADIDENTENT");//"x";
    String subs = subsent;
    if (subsent == null) {
      subs = subsrel;
      var = m.group("INSERTHEADIDENTREL");//"x"
    } else {
      if (var == null) {
        throw new RuntimeException("Somehow got a null entity return variable on insert - parsing error");
      }
    }
    Variable v = null;
    if (var != null) {
      var = var.substring(1);
      v = Variable.fromIdentifier(var);
    }
    QueryInsert q = new QueryInsert(v,subs);
    List<String> ss = matches(graql,"<insert_has>");
    ss.forEach(s -> {
      Entry<Attribute,AttributeValue> entry = parseInsertHas(s);
      q.attributes.put(entry.getKey(),entry.getValue());
    });

    ss = matches(graql,"<relation_entry>");
    if (ss.size() != 0) {
      String relationEntry = ss.get(0);
      q.plays = parseRelationEntry(relationEntry);
    }
    return q;
  }

  private static List<Entry<Plays,Variable>> parseRelationEntry(String graql) {
    //(lab1: $v1, lab2: $v2)
    List<Entry<Plays,Variable>> toRet = new ArrayList<>();
    List<String> ss = matches(graql,"<relation_subentry>");
    ss.forEach(s -> {
      toRet.add(parseCondition_relationsubentry(s));
    });
    return toRet;
  }

  private static Entry<Attribute,AttributeValue> parseInsertHas(String graql) {
    Matcher m = matcher(graql,"<insert_has>","INSERTHASIDENT","STRINGLIT","NUMLIT","DATELIT","BOOLLIT");
    m.matches();
    String ident = m.group("INSERTHASIDENT");//"name"
    String lit1 = m.group("STRINGLIT");//"Bob"
    String lit2 = m.group("NUMLIT");//36
    String lit3 = m.group("DATELIT");//2000-01-01
    String lit4 = m.group("BOOLLIT");//true
    Object lit = lit1;
    if (lit == null) {
      if (lit2 != null) {
        lit = Double.parseDouble(lit2);
      } else {
        if (lit3 != null) {
          lit = lit3;
        } else {
          lit = Boolean.parseBoolean(lit4);
        }
      }
    }
    return new SimpleEntry<>(Attribute.fromIdentifier(ident), ConstantValue.fromValue(lit));
  }

  //TODO organise the methods in this class, and surround appropriate groups with editor fold comments
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
      List<String> ss = matches(graql,"<variable>");
      ss.forEach(var -> {
        toAction.add(Variable.fromIdentifier(var.substring(1)));
      });
      q.setActionGet(toAction);
      return;
    }
    m = matcher(graql,"<insert_block>");
    if (m.matches()) {
      //TODO - should be insert block - change querymatch to have a list<insertquery>
      q.setActionInsert(parseInsertBlock(graql).get(0));
      return;
    }
    throw new RuntimeException("Match Action didn't match regex");
  }

  private static List<MatchCondition> parseMatchConditions(String graql) {
    List<MatchCondition> toRet = new ArrayList<>();
    List<String> ss;
    ss = matches(graql,"<match_condition>");
    ss.forEach(s -> toRet.add(parseMatchCondition(s)));
    return toRet;
  }

  private static MatchCondition parseMatchCondition(String graql) {
    List<MatchCondition> toRet = new ArrayList<>();
    List<String> ss;
    //<neq_condition>|<isa_has_condition>|<rel_ent_isa_condition>|<rel_isa_condition>
    Matcher m = matcher(graql,"<rel_isa_condition>");
    if (m.matches()) {
      return parseCondition_isa_rel(null,graql);
    }
    m = matcher(graql,"<isa_has_condition>");
    if (m.matches()) {
      return parseCondition_isa_has(graql);
    }
    m = matcher(graql,"<rel_ent_isa_condition>");
    if (m.matches()) {
      return parseCondition_isa_relent(graql);
    }
    m = matcher(graql,"<neq_condition>");
    if (m.matches()) {
      return parseCondition_neq(graql);
    }
    m = matcher(graql,"<has_condition>");
    if (m.matches()) {
      return parseCondition_has(graql);
    }
    throw new RuntimeException("matched, and then didn't");
  }
  //</editor-fold>

  //<editor-fold desc="Parsing for define_block">
  private static List<Query> parseDefineBlock(String graql) {
    List<Query> toRet = new ArrayList<>();
    List<String> ss = matches(graql,"<define>");
    ss.forEach(s -> toRet.add(parseDefine(s)));
    ss = matches(graql,"<define_rule>");
    ss.forEach(s -> toRet.add(parseDefineRule(s)));
    return toRet;
  }

  private static QueryDefine parseDefine(String graql) {
    Matcher m = matcher(graql,"<define>","DEFINEHEADIDENT","DEFINEHEADSUBS");
    m.matches();
    String ident = m.group("DEFINEHEADIDENT");//"person"
    String subs = m.group("DEFINEHEADSUBS");//"entity"
    QueryDefine q = new QueryDefine(ident,QueryDefine.getFromIdentifier(subs));
    List<String> ss = matches(graql,"<define_has>");
    ss.forEach(s -> q.attributes.add(parseDefineHas(s)));
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
    List<String> ss = matches(graql,"<when_block>");
    String whenBlock = ss.get(0);
    q.when = parseWhenBlock(whenBlock);
    ss = matches(graql,"<then_block>");
    String thenBlock = ss.get(0);
    q.then = parseThenBlock(thenBlock);
    return q;
  }

  private static QueryInsert parseThenBlock(String graql) {
    List<String> ss = matches(graql,"<rel_isa_condition>");
    String relIsaCond = ss.get(0);
    ConditionIsa cond = parseCondition_isa_rel(null,relIsaCond);
    QueryInsert q = new QueryInsert(null,cond.type);
    q.plays = cond.relates;
    //not has for now?
    return q;
  }

  private static List<MatchCondition> parseWhenBlock(String graql) {
    List<MatchCondition> toRet = new ArrayList<>();
    List<String> ss;
    //<rel_isa_condition>|<ent_isa_condition>|<rel_ent_isa_condition>|<has_condition>|<neq_condition>
    ss = matches(graql,"<rel_isa_condition>");
    ss.forEach(s -> toRet.add(parseCondition_isa_rel(null,s)));
    ss = matches(graql,"<ent_isa_condition>");
    ss.forEach(s -> toRet.add(parseCondition_isa_ent(s)));
    ss = matches(graql,"<rel_ent_isa_condition>");
    ss.forEach(s -> toRet.add(parseCondition_isa_relent(s)));
    ss = matches(graql,"<has_condition>");
    ss.forEach(s -> toRet.add(parseCondition_has(s)));
    ss = matches(graql,"<neq_condition>");
    ss.forEach(s -> toRet.add(parseCondition_neq(s)));
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
    List<String> ss = matches(graql,"<has_subcondition>");
    ss.forEach(s -> {
      Entry<Attribute,AttributeValue> entry = parseCondition_hassubentry(s);
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
    List<String> ss = matches(graql,"<relation_subentry>");
    ss.forEach(s -> cond.relates.add(parseCondition_relationsubentry(s)));
    return cond;
  }

  private static ConditionIsa parseCondition_isa_relent(String graql) {
    //$v1 (lab1: $v2) isa type1;
    Matcher m = matcher(graql,"<rel_ent_isa_condition>","REL2");
    m.matches();
    String var = m.group("REL2");//"var"
    var = var.substring(1);
    List<String> ss = matches(graql,"<rel_isa_condition>");
    String subcondition = ss.get(0);
    return parseCondition_isa_rel(Variable.fromIdentifier(var),subcondition);
  }

  private static Entry<Plays,Variable> parseCondition_relationsubentry(String graql) {
    //lab1: $v1
    Matcher m = matcher(graql,"<relation_subentry>","RELSUB1","RELSUB2");
    m.matches();
    String lab = m.group("RELSUB1");//"lab1"//may not exist
    String var = m.group("RELSUB2");//"$v1"
    Plays plays = null;
    if (lab != null) {
      plays = Plays.fromIdentifier(lab);
    }
    var = var.substring(1);
    return new SimpleEntry<>(plays, Variable.fromIdentifier(var));
  }

  private static ConditionIsa parseCondition_has(String graql) {
    //$v1 has n1 $v2, has n2 $v3;
    Matcher m = matcher(graql,"<has_condition>","HAS1");
    m.matches();
    String var = m.group("HAS1");//"$v1"
    var = var.substring(1);
    ConditionIsa cond = new ConditionIsa(Variable.fromIdentifier(var),"concept");
    List<String> ss = matches(graql,"<has_subcondition>");
    ss.forEach(s -> {
      Entry<Attribute,AttributeValue> entry = parseCondition_hassubentry(s);
      cond.has.put(entry.getKey(),entry.getValue());
    });
    return cond;
  }

  private static Entry<Attribute, AttributeValue> parseCondition_hassubentry(String graql) {
    //has n1 $v2
    //has n1 "bob"
    Matcher m = matcher(graql,"<has_subcondition>","HASSUB1","HASSUB2","STRINGLIT","NUMLIT","DATELIT","BOOLLIT");
    m.matches();
    String lab = m.group("HASSUB1");//"v1"
    String var = m.group("HASSUB2");//"$v2"
    String lit1 = m.group("STRINGLIT");//"Bob"
    String lit2 = m.group("NUMLIT");//36
    String lit3 = m.group("DATELIT");//2000-01-01
    String lit4 = m.group("BOOLLIT");//true
    AttributeValue v = null;
    if (var != null) {
      var = var.substring(1);
      v = Variable.fromIdentifier(var);
    } else {
      Object lit = lit1;
      if (lit == null) {
        if (lit2 != null) {
          lit = Double.parseDouble(lit2);
        } else {
          if (lit3 != null) {
            lit = lit3;
          } else {
            lit = Boolean.parseBoolean(lit4);
          }
        }
      }
      v = ConstantValue.fromValue(lit);
    }
    return new SimpleEntry<>(Attribute.fromIdentifier(lab),v);
  }


}
