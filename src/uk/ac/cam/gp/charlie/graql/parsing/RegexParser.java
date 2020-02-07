package uk.ac.cam.gp.charlie.graql.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import uk.ac.cam.gp.charlie.DebugHelper;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefine;

public class RegexParser {

  /**
   * Hi Charles
   *
   * I noticed that the Graql parser file wasn't working for some inputs, I thought it might be worth trying it in regex?
   * I've written a basic Regex parser for parsing simple define statements which define entities and relations with 'has' attributes only (for now)
   *
   * If you think this is a better route feel free to use it, idm which one we use as long as it parses well
   * @param args
   */

  public static void main(String[] args) {
    String test = "define person sub entity,  has name  , has nickname;  \n \n organisation sub\nentity\n, has \nname   \n;";
    List<Query> res = parse(test);
    System.out.println("RESULT OF PARSING:");
    DebugHelper.printObjectTree(res);
  }

  private static String regex(String regex) {
    regex = regex.replaceAll("<define_block>","(<wso>define(<ws><define>)+)");
    regex = regex.replaceAll("<define>","(((?<DEFINEHEADIDENT><identifier>)<ws>sub<ws>(?<DEFINEHEADSUBS><identifier>))(<define_has>|<define_plays>|<define_relates>)*<wso>;)");
    regex = regex.replaceAll("<define_relates>","(<wso>,<ws>relates<ws><identifier>)"); //'relates' definition
    regex = regex.replaceAll("<define_plays>","(<wso>,<ws>plays<ws><identifier>)"); //'plays' definition
    regex = regex.replaceAll("<define_has>","(<wso>,<ws>has<ws>(?<DEFINEHASIDENT><identifier>))"); //'has' definition
    regex = regex.replaceAll("<identifier>","(<alpha>(<alphanum>)+)");//Identifier
    regex = regex.replaceAll("<alphanum>","(<alpha>|<num>|-)"); //Alphanumeric character (or _,-)
    regex = regex.replaceAll("<num>","([0-9])"); //Numeric character
    regex = regex.replaceAll("<alpha>","([a-z]|[A-Z]|_)"); //Alphabetic (or _) character
    regex = regex.replaceAll("<wso>","(<ws>)?"); //Whitespace optional
    regex = regex.replaceAll("<ws>","( |\n|\r)+"); //Whitespace
    return regex;
  }

  private static Matcher matcher(String graql, String regex) {
    return Pattern.compile(regex(regex)).matcher(graql);
  }

  public static List<Query> parse(String graql) {
    List<Query> toRet = new ArrayList<>();
    Stream<MatchResult> s = matcher(graql,"<define_block>").results();
    s.forEach(matchResult -> toRet.addAll(parseDefineBlock(graql.substring(matchResult.start(), matchResult.end()))));

    return toRet;
  }

  private static List<QueryDefine> parseDefineBlock(String graql) {
    List<QueryDefine> toRet = new ArrayList<>();
    Stream<MatchResult> s = matcher(graql,"<define>").results();
    s.forEach(matchResult -> toRet.add(parseDefine(graql.substring(matchResult.start(),matchResult.end()))));
    return toRet;
  }

  private static QueryDefine parseDefine(String graql) {
    Matcher m = matcher(graql,"<define>");
    m.matches();
    String ident = m.group("DEFINEHEADIDENT");
    String subs = m.group("DEFINEHEADSUBS");
    QueryDefine q = new QueryDefine(ident,QueryDefine.getFromIdentifier(subs));
    Stream<MatchResult> s = matcher(graql,"<define_has>").results();
    s.forEach(matchResult -> q.attributes.add(parseDefineHas(graql.substring(matchResult.start(),matchResult.end()))));
    return q;
  }

  private static Attribute parseDefineHas(String graql) {
    Matcher m = matcher(graql,"<define_has>");
    m.matches();
    String ident = m.group("DEFINEHASIDENT");
    return Attribute.fromIdentifier(ident);
  }





//    s.forEach(matchResult -> System.out.println("match: "+graql.substring(matchResult.start(),matchResult.end())));

}
