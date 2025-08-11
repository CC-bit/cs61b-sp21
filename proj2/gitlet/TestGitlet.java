package gitlet;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static gitlet.Utils.*;

public class TestGitlet {

    @org.junit.Test
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

    @org.junit.Test
    public void dateTime() {
        Instant instant = Instant.now();
        ZonedDateTime dateTime = instant.atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E MMM d HH:mm:ss yyyy Z");
        String print = dateTime.format(formatter);
        System.out.println(print);

    }

    @Test
    public void writeCWDTest() throws IOException {
        String fileHash = "63ebcd876198409bd2b8bf58609678ba04f7303c";
        Path c = Path.of("testing").resolve("test02-basic-checkout_5");
        Path b = c.resolve(".gitlet").resolve("blobs");
        Path dir = b.resolve(fileHash.substring(0,2));
        if (!Files.isDirectory(dir)) {
            System.out.println("Dir not exists: " + dir);
        }
        /*
        Path CWD = Path.of("testing").resolve("test02-basic-checkout_4");
        Path BLOB_DIR = CWD.resolve(".gitlet").resolve("blobs");
        String fileName = "wug.txt";
        String fileHash = "63ebcd876198409bd2b8bf58609678ba04f7303c";
        Path dir = BLOB_DIR.resolve(fileHash.substring(0,2));
        if (!Files.isDirectory(dir)) {
            System.out.println("Dir not exists: " + dir);
        }

        Path source = BLOB_DIR.resolve(fileHash.substring(0,2)).resolve(fileHash.substring(2));
        Path target = CWD.resolve(fileName);
        if (!Files.exists(source)) {
            throw new GitletException("Can't find blob file with hash: " + fileHash);
        }
        if (Files.isDirectory(target)) {
            throw new GitletException("Target file is a directory.");
        }
        Files.copy(source, target, REPLACE_EXISTING);

         */

    }
}
