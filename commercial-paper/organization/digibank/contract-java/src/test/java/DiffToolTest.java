import lib.diff_match_patch;
import org.junit.jupiter.api.Test;

public class DiffToolTest {

    @Test
    public void testDiffTool() {
        String old  = "hello world!\nwhats uup?";
        String to = "hello world!\nwhat is up, my dude?";
        String calc = new diff_match_patch().calc(old, to);
        System.out.println(calc.replace("\n", "\\n"));
    }
}
