package uk.ac.cam.gp.charlie;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import uk.ac.cam.gp.charlie.graql.GraqlExecutor;

public class Main {

  public static void main(String[] args) {
    TestEnvironment env = new TestEnvironment(readFile("social_network_schema.gql"),
            readFile("social_network_data.gql"));
    GraqlExecutor graqlExecutor = new GraqlExecutor(env);
    Result result = graqlExecutor.execute(readFile("social_network_queries.gql"));
  }

  private static String readFile(String filePath){
    // inspired by https://howtodoinjava.com/java/io/java-read-file-to-string-examples/
    StringBuilder builder = new StringBuilder();
    try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8)){
      stream.forEach(s -> builder.append(s).append("\n"));
    }catch (IOException e){
      e.printStackTrace();
    }
    return builder.toString();
  }

}
