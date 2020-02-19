package uk.ac.cam.gp.charlie.graql.parsing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.ac.cam.gp.charlie.ast.Attribute;
import uk.ac.cam.gp.charlie.ast.queries.Query;
import uk.ac.cam.gp.charlie.ast.queries.QueryDefine;

public class RegexParser extends GraqlParser {

  public static void main(String[] args) {
    iterate(matcher("$a", regex("<test>"))).forEachRemaining(new Consumer<MatchResult>() {
      @Override
      public void accept(MatchResult matchResult) {
        System.out.println(matchResult.start()+":"+matchResult.end());
      }
    });
  }

  private static String regex(String regex) {
    regex = regex.replaceAll("<insert_block>","(<wso>insert(<ws><insert>)+)");
    regex = regex.replaceAll("<insert>", "(((?<INSERTHEADIDENT><variable_identifier>)<ws>isa<ws>(?<INSERTHEADISA><identifier>))(<insert_has>)*<wso>;)");
    regex = regex.replaceAll("<insert_has>", "(<wso>,<ws>has<ws>(?<INSERTHASIDENT><identifier><ws><wso><property_identifier>))");
    regex = regex.replaceAll("<variable_identifier>","(\\$<alpha>(<alphanum>)+)");
    regex = regex.replaceAll("<property_identifier>","(\"<alpha>(<alphanumspace>)+)\"");
    regex = regex.replaceAll("<alphanumspace>","(<alpha>|<num>|-|<ws>|=)");
    regex = regex.replaceAll("<test1>", "[(]<identifier>:<ws>");
    regex = regex.replaceAll("<test>", "\\$a");



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
