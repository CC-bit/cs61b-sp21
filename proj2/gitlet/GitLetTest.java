package gitlet;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static gitlet.Utils.*;

public class GitLetTest {

    @Test
    public void addTest() throws IOException {
        File stage = join("testing", "add_4", ".gitlet", "stage", "wug.txt");
        String stageHash = sha1((Object) readContents(stage));
        System.out.println("stage hash: " + stageHash);

        File src = join("testing", "src", "wug.txt");
        File blob = join("testing", "add_4", ".gitlet", "blob", "wug.txt");
        String srcHash = sha1((Object) readContents(src));
        Files.copy(
                src.toPath(),
                blob.toPath()
        );
        String blobHash = sha1((Object) readContents(blob));
        System.out.println("src hash:   " + srcHash);
        System.out.println("blob hash:  " + blobHash);
    }

    @Test
    public void ca() {
        List<String> allFiles = plainFilenamesIn(Repository.CWD);
        for (String file : allFiles) {
            System.out.println(file);
        }
    }

    @Test
    public void dateTime() {
        Instant instant = Instant.now();
        ZonedDateTime dateTime = instant.atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E MMM d HH:mm:ss yyyy Z");
        String print = dateTime.format(formatter);
        System.out.println(print);

    }
}
