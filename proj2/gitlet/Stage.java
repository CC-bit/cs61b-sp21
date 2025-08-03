package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static gitlet.Utils.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Stage{
    public static final File STAGERM = join(Repository.GITLET_DIR, "stageRm");
    public static ArrayList<String> stageRm;

    /** Adds a file from CWD to STAGE_DIR.
     * There should not be a file in STAGE_DIR with same name and version.
     */
    public static void add(String fileName) throws IOException {
        Files.copy(
                join(Repository.CWD, fileName).toPath(),
                join(Repository.STAGE_DIR, fileName).toPath(),
                REPLACE_EXISTING
        );
    }

    /** Remove a file from STAGE_DIR and the map in stageAd. */
    public static void rm(String fileName) {
        join(Repository.STAGE_DIR, fileName).delete();
    }

    /** Returns if stage area contains a file. */
    public static boolean contains(String file) {
        List<String> allFiles = plainFilenamesIn(Repository.STAGE_DIR);
        assert allFiles != null;
        return allFiles.contains(file);
    }
    /** Returns if stage area contains a file with that exact hash. */
    public static boolean contains(String file, String hash) {
        List<String> allFiles = plainFilenamesIn(Repository.STAGE_DIR);
        assert allFiles != null;
        return (allFiles.contains(file)
                && hash.equals(sha1((Object) readContents(join(Repository.STAGE_DIR, file)))));
    }

    /** Returns if stage area is empty. */
    public static boolean isEmpty() {
        return plainFilenamesIn(Repository.STAGE_DIR) == null;
    }

    /** Clear the STAGE_DIR. */
    public static void clear() {
        List<String> allFiles = plainFilenamesIn(Repository.STAGE_DIR);
        if (allFiles == null) {
            return;
        }
        for (String file : allFiles) {
            join(Repository.STAGE_DIR, file).delete();
        }
    }
}
