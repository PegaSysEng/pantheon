import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

public class TestRegEx {

  @Test
  public void a(){
    final String input = "0.1.17-SNAPSHOT";
    final String regex = "/(\\d+)/";


   final String[] matches= Pattern.compile(regex).split(input);

   System.out.println("break");

  }
}
