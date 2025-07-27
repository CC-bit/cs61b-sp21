package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.TreeMap;

import static gitlet.Utils.join;

public class Stage{
    public static final File STAGEAD = join(Repository.GITLET_DIR, "stageAd");
    public static final File STAGERM = join(Repository.GITLET_DIR, "stageRm");
    public static TreeMap<String, String> stageAd;
    public static ArrayList<String> stageRm;

    /** Adds a file from CWD to STAGE_DIR.
     * There should not be a file in STAGE_DIR with same name and version.
     */
    public static void add(String orgName, String hashName) throws IOException {
        Files.copy(
                join(Repository.CWD, orgName).toPath(),
                join(Repository.STAGE_DIR, hashName).toPath()
        );
        stageAd.put(orgName, hashName);
    }

    /** Remove a file from STAGE_DIR and the map in stageAd. */
    public static void rm(String orgName) {
        Utils.restrictedDelete(
                join(Repository.STAGE_DIR, stageAd.get(orgName))
        );
        stageAd.remove(orgName);
    }

    /** Clear STAGE_DIR and stageAd. */
    public static void clear() {
        for (String hashName : stageAd.values()) {
            Utils.restrictedDelete(
                    join(Repository.STAGE_DIR, hashName)
            );
        }
        stageAd.clear();
    }

}
