package uk.ac.cam.gp.charlie.graql.parsing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefine;

public class RegexParser extends GraqlParser {

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
    Matcher m = Pattern.compile(regex(regex)).matcher(graql);
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
    Iterator<MatchResult> s = iterate(matcher(graql,"<define_block>"));
    s.forEachRemaining(matchResult -> toRet.addAll(parseDefineBlock(graql.substring(matchResult.start(), matchResult.end()))));

    return toRet;
  }

  private static List<QueryDefine> parseDefineBlock(String graql) {
    List<QueryDefine> toRet = new ArrayList<>();
    Iterator<MatchResult> s = iterate(matcher(graql,"<define>"));
    s.forEachRemaining(matchResult -> toRet.add(parseDefine(graql.substring(matchResult.start(),matchResult.end()))));
    return toRet;
  }

  private static QueryDefine parseDefine(String graql) {
    Matcher m = matcher(graql,"<define>");
    m.matches();
    String ident = m.group("DEFINEHEADIDENT");//"person"
    String subs = m.group("DEFINEHEADSUBS");//"entity"
    QueryDefine q = new QueryDefine(ident,QueryDefine.getFromIdentifier(subs));
    Iterator<MatchResult> s = iterate(matcher(graql,"<define_has>"));
    s.forEachRemaining(matchResult -> q.attributes.add(parseDefineHas(graql.substring(matchResult.start(),matchResult.end()))));
    return q;
  }

  private static Attribute parseDefineHas(String graql) {
    Matcher m = matcher(graql,"<define_has>");
    m.matches();
    String ident = m.group("DEFINEHASIDENT");//"name"
    return Attribute.fromIdentifier(ident);
  }

}
