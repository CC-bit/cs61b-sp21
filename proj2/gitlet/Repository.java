package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.TreeMap;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The staging area. */
    public static final File STAGE_DIR = join(GITLET_DIR, "stage");
    /** The blob directory. */
    public static final File BLOB_DIR = join(GITLET_DIR, "blob");
    /** The commit directory. */
    public static final File COMMIT_DIR = join(GITLET_DIR, "commit");
    /** The pointer file. */
    public static final File POINTER = join(GITLET_DIR, "pointer");
    /** A map of pointer name like "master" to commit hash. */
    public static TreeMap<String, String> pointer;

    /**
     * Does required filesystem operations to allow for persistence.
     * (creates any necessary folders or files)
     * <p>
     * .gitlet/ -- top level folder for all persistent data in your lab12 folder
     *    - blob/ -- folder containing all blobs
     *    - commit/ -- folder containing all the persistent data for commits
     */
    public static void init() throws IOException {
        if (GITLET_DIR.exists()) {
            System.out.println(
                    "A Gitlet version-control system already exists in the current directory."
            );
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        STAGE_DIR.mkdir();
        BLOB_DIR.mkdir();
        COMMIT_DIR.mkdir();
        Commit initCommit = new Commit();
        String initHash = initCommit.getHash();
        pointer.put("master", initHash);
        pointer.put("head", initHash);
    }

    /**
     * Makes a copy of the file in CWD to the STAGE_DIR.
     * It will overwrite the old version in the STAGE_DIR.
     * If the version in CWD is identical to that in current commit,
     * does not stage and removes it from STAGE_DIR(if there be).
     * If the file is staged for rm before add, cancel the stage for rm.
     */
    public static void add(String fileName) throws IOException {
        File f = join(CWD, fileName);
        if (!f.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        String fileHash = Utils.sha1((Object) readContents(f));
        Commit head = Commit.readCommit(pointer.get("head"));
        String fileHashCommit = head.getFileHash(fileName);
        String fileHashStaged = Stage.stageAd.get(fileName);
        if (Objects.equals(fileHash, fileHashCommit)) {
            Stage.rm(fileName);
            return;
        }
        if (!Objects.equals(fileHash, fileHashStaged)) {
            Stage.add(fileName, fileHash);
        }
        Stage.stageRm.remove(fileName);
    }

    /**
     * Copies the last commit as the new commit.
     * Updates the files in staging area.
     * Untracking files in stageRm area.
     */
    public static void commit(String msg) throws IOException {
        if (Stage.stageAd.isEmpty() && Stage.stageRm.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        } else if (msg.isBlank()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Commit newCommit = new Commit(getPointer("head"), msg);
        pointer.put("head", newCommit.getHash());
        Stage.clear();
    }

    /**
     * If the file is staged for add, removes it from staging area.
     * If the file is tracked in current commit, deletes it from CWD
     * and stages it for rm.
     */
    public static void rm(String fileName) {
        boolean staged = Stage.stageAd.containsKey(fileName);
        boolean commited = getPointer("head").containFile(fileName);
        if (!staged && !commited) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        if (staged) {
            Stage.rm(fileName);
        }
        File rmFile = join(CWD, fileName);
        if (commited && rmFile.exists()) {
            rmFile.delete();
        }
        Stage.stageRm.add(fileName);
    }

    /** Returns the pointer commit of master, head, branches. */
    private static Commit getPointer(String p) {
        return Commit.readCommit(pointer.get(p));
    }
}
