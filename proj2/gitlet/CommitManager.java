package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import static gitlet.Utils.plainFilenamesIn;
import static gitlet.Utils.readObject;

public class CommitManager {
    /** The commit directory. */
    private final Path COMMIT_DIR;
    /** A map of commit id to commit instance. */
    private final TreeMap<String, Commit> commitCache = new TreeMap<>();

    public CommitManager(Path gitletPath) {
        this.COMMIT_DIR = gitletPath.resolve("commits");
    }
    Path getCommitsPath() {
        return COMMIT_DIR;
    }
    void cacheCommit(String id, Commit commit) {
        commitCache.put(id, commit);
    }


    Commit readCommit(String id) {
        if (commitCache.containsKey(id)) {
            return commitCache.get(id);
        }
        // find the target commit
        Path folder = COMMIT_DIR.resolve(id.substring(0, 2));
        String restCid = id.substring(2);
        int commitNum = 0;
        String commit = "";
        if (Files.exists(folder)) {
            List<String> allFiles = plainFilenamesIn(folder);
            assert allFiles != null;
            for (String file : allFiles) {
                if (file.startsWith(restCid)) {
                    commit = file;
                    commitNum++;
                }
            }
        }
        // maybe more than one commit(short id)
        if (commitNum == 0) {
            throw new GitletException("No commit with that id exists.");
        }
        if (commitNum > 1) {
            throw new GitletException("Ambiguous commit id.");
        }
        Commit target = readObject(folder.resolve(commit), Commit.class);
        commitCache.put(id, target);
        return target;
    }


    /** Serialize a commit into COMMIT_DIR. */
    public void writeCommit(Commit c) throws IOException {
        Path subHash = COMMIT_DIR.resolve(c.getID().substring(0, 2));
        Files.createDirectory(subHash);
        Utils.writeObject(subHash.resolve(c.getID().substring(2)), c);
    }

    /** Display information. */
    public void display(Commit commit) {
        String secPid = commit.getSecondParentID();
        System.out.println("===");
        System.out.println("commit " + commit.getID());
        if (secPid != null) {
            System.out.println(
                    "Merge: "
                            + commit.getParentID().substring(0, 7) + " "
                            + secPid.substring(0, 7)
            );
        }
        ZonedDateTime zonedDateTime = commit.getTime().atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("E MMM d HH:mm:ss yyyy Z").withLocale(Locale.ENGLISH);
        System.out.println("Date: " + zonedDateTime.format(formatter));
        System.out.println(commit.getMessage() + "\n");
    }
}
