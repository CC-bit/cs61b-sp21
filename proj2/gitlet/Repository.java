package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

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
        pointer.put("initCommit", initHash);
        pointer.put("head", "master");
        pointer.put("master", initHash);
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
        String fileHashCommit = getCommit("head").getFileHash(fileName);
        if (Objects.equals(fileHash, fileHashCommit)) {
            Stage.rm(fileName);
            return;
        }
        Stage.add(fileName);
        Stage.stageRm.remove(fileName);
    }

    /**
     * Copies the last commit as the new commit.
     * Updates the files in staging area.
     * Untracking files in stageRm area.
     */
    public static void commit(String msg) throws IOException {
        if (Stage.isEmpty() && Stage.stageRm.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        } else if (msg.isBlank()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Commit newCommit = new Commit(pointer.get(pointer.get("head")), msg);
        pointer.put(pointer.get("head"), newCommit.getHash());
        Stage.clear();
    }

    /**
     * If the file is staged for add, removes it from staging area.
     * If the file is tracked in current commit, deletes it from CWD
     * and stages it for rm.
     */
    public static void rm(String fileName) {
        boolean staged = Stage.contains(fileName);
        boolean commited = getCommit("head").containFile(fileName);
        if (!staged && !commited) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        if (staged) {
            Stage.rm(fileName);
        }
        File rmFile = join(CWD, fileName);
        if (commited && rmFile.exists()) {
            restrictedDelete(rmFile);
        }
        Stage.stageRm.add(fileName);
    }

    /** Log from head commit. */
    private static void log(String cid) {
        Commit node = getCommit(cid);
        node.display();
        if (cid.equals(pointer.get("initCommit"))) return;
        log(node.getParent());
    }

    /** Log. */
    public static void log() {
        log(pointer.get(pointer.get("head")));
    }

    /** Global-log. */
    public static void globalLog() throws IOException {
        try (Stream<Path> stream = Files.walk(COMMIT_DIR.toPath())) {
            stream.filter(Files::isRegularFile).forEach(cPath -> {
                Commit commit = readObject(cPath.toFile(), Commit.class);
                commit.display();
            });
        }
    }

    /** Find. */
    public static void find(String cMsg) throws IOException {
        AtomicInteger msgNum = new AtomicInteger(0);
        try (Stream<Path> stream = Files.walk(COMMIT_DIR.toPath())) {
            stream.filter(Files::isRegularFile).forEach(cPath -> {
                Commit commit = readObject(cPath.toFile(), Commit.class);
                if (cMsg.equals(commit.getMessage())) {
                    msgNum.incrementAndGet();
                    System.out.println(commit.getHash());
                }
            });
        }
        if (msgNum.get() == 0) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    /** Status. */
    public static void status() {
        List<String> stgFiles = plainFilenamesIn(STAGE_DIR);
        assert stgFiles != null;
        System.out.println("=== Branches ===");
        System.out.println("*" + pointer.get("head"));
        for (Map.Entry<String, String> entry : pointer.entrySet()) {
            if (entry.getKey().equals("head")) continue;
            System.out.println(entry.getValue());
        }
        System.out.println("\n=== Staged Files ===");
        for (String file : stgFiles) {
            System.out.println(file);
        }
        System.out.println("\nRemoved Files");
        for (String file : Stage.stageRm) {
            System.out.println(file);
        }
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        Commit current = getCommit("head");
        for (Map.Entry<String, String> entry : current.entrySet()) {
            String file = entry.getKey();
            File cwd = new File(file);
            if (cwd.exists()) {
                String cwdHash = sha1((Object) readContents(cwd));
                if (!entry.getValue().equals(cwdHash) && !Stage.contains(file, cwdHash)) {
                    System.out.println(file + " (modified)");
                }
            } else if (Stage.stageRm.contains(file)) {
                System.out.println(file + " (deleted)");
            }
        }
        for (String file : stgFiles) {
            String stgHash = sha1((Object) readContents(join(STAGE_DIR, file)));
            File cwd = join(file);
            if (cwd.exists()) {
                String cwdHash = sha1((Object) readContents(join(file)));
                if (!stgHash.equals(cwdHash)) {
                    System.out.println(file + " (modified)");
                }
            } else {
                System.out.println(file + " (deleted)");
            }
        }
        System.out.println("\n=== Untracked Files ===");
        List<String> cwd = plainFilenamesIn(CWD);
        assert cwd != null;
        cwd.remove(".gitlet");
        for (String file : cwd) {
            if (!current.containFile(file) && !Stage.contains(file)) {
                System.out.println(file);
            }
        }

    }

    /** Recover file status from commit id. */
    private static void recover(String cid) throws IOException {
        Commit commit = getCommit(cid); // Failure case at Commit.readCommit
        List<String> cwd = plainFilenamesIn(CWD);
        assert cwd != null;
        cwd.remove(".gitlet");
        for (String file : cwd) {
            String hash = sha1((Object) readContents(new File(file)));
            if (!commit.containFile(file) || !commit.containFile(file, hash)) {
                System.out.println("There is an untracked file in the way;" +
                        " delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        commit.writeCWD();
    }

    /** Checkout [branch name]. */
    public static void checkout(String branch) throws IOException {
        if (!pointer.containsKey(branch)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (pointer.get("head").equals(branch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        recover(branch);
        pointer.put("head", branch);
        Stage.clear();
    }
    /** Checkout -- [file name]. */
    public static void checkout(String line, String file) throws IOException {
        checkout("head", "--", file);
    }
    /** Checkout [commit id] -- [file name]. */
    public static void checkout(String cid, String line, String file) throws IOException {
        Commit commit = getCommit(cid); // Failure case at Commit.readCommit
        if (!commit.containFile(file)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        commit.writeCWD(file);
    }

    /** Branch. */
    public static void branch(String branchName) {
        if (pointer.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        pointer.put(branchName, pointer.get(pointer.get("head")));
    }

    /** Rm-branch. */
    public static void rmBranch(String branchName) {
        if (!pointer.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (pointer.get("head").equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        pointer.remove(branchName);
    }

    /** Reset. */
    public static void reset(String cid) throws IOException {
        recover(cid);
        pointer.put(pointer.get("head"), cid);
        Stage.clear();
    }

    /** Merge. */
    public static void merge(String branch) {
        
    }

    /** Returns the commit instance of head or certain branch or commit id. */
    private static Commit getCommit(String p) {
        // if p is a name(head or branch name), cid = get(p), else(p is hash) cid = p
        if (p.equals("head")) p = pointer.get(p);
        return Commit.readCommit(pointer.getOrDefault(p, p));
    }


}
