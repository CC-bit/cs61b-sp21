package gitlet;

import org.junit.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static gitlet.Utils.readContents;
import static gitlet.Utils.sha1;

public class TestGitlet {

    @Test
    public void dateTime() {
        Instant instant = Instant.now();
        ZonedDateTime dateTime = instant.atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E MMM d HH:mm:ss yyyy Z");
        String print = dateTime.format(formatter);
        System.out.println(print);

    }

    @Test
    public void rmAreaTest() {
        Path cwd = Path.of("testing").resolve("test23-global-log_0");
        Repository repo = new Repository(cwd);
        Path hWug = cwd.resolve("h.txt");
        String hHash = sha1((Object) readContents(hWug));
        System.out.println(hHash);
    }

}
