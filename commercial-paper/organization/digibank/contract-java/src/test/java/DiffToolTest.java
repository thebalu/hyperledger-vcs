import lib.diff_match_patch;
import org.junit.jupiter.api.Test;

public class DiffToolTest {

    @Test
    public void testDiffTool() {
        String to  = "<html>\n" +
                "<body>\n" +
                "    <h1>Company emails</h1>\n" +
                "    <p> Magnetocorp: magnetocorp@gmail.com</p>\n" +
                "    <p> Digibank: digibank_new@gmail.com</p>\n" +
                "</body>\n" +
                "</html>";
        String old = "<html>\n" +
                "<body>\n" +
                "    <h1>Company emails</h1>\n" +
                "    <p> Magnetocorp: magnetocorp@gmail.com</p>\n" +
                "    <p> Digibank: digibank@gmail.com</p>\n" +
                "</body>\n" +
                "</html>\n";
        String calc = new diff_match_patch().calc(old, to);
        System.out.println(calc.replace("\n", "\\n"));
    }
}
