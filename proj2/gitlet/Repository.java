package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static gitlet.Utils.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {

    /** The current working directory. */
    public static final Path CWD = Paths.get(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final Path GITLET_DIR = CWD.resolve(".gitlet");
    /** The staging area. */
    public static final Path STAGE_DIR = GITLET_DIR.resolve("stage");
    /** The blob directory. */
    public static final Path BLOB_DIR = GITLET_DIR.resolve("blob");
    /** The commit directory. */
    public static final Path COMMIT_DIR = GITLET_DIR.resolve("commit");
    /** The pointer file. */
    public static final Path POINTER = GITLET_DIR.resolve("pointer");
    /** A map of pointer name like "master" to commit hash. */
    public static TreeMap<String, String> pointer;

    public static final Path STAGERM = GITLET_DIR.resolve("stageRm");
    public static ArrayList<String> stageRm;

    /**
     * Does required filesystem operations to allow for persistence.
     * (creates any necessary folders or files)
     * <p>
     * .gitlet/ -- top level folder for all persistent data in your lab12 folder
     *    - blob/ -- folder containing all blobs
     *    - commit/ -- folder containing all the persistent data for commits
     */
    public static void init() throws IOException {
        if (Files.exists(GITLET_DIR)) {
            System.out.println(
                    "A Gitlet version-control system already exists in the current directory."
            );
            System.exit(0);
        }
        Files.createDirectory(GITLET_DIR);
        Files.createDirectory(STAGE_DIR);
        Files.createDirectory(BLOB_DIR);
        Files.createDirectory(COMMIT_DIR);
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
        Path filePath = CWD.resolve(fileName);
        if (!Files.exists(filePath)) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        String fileHash = Utils.sha1((Object) readContents(filePath));
        String fileHashCommit = getCommit("head").getFileHash(fileName);
        if (Objects.equals(fileHash, fileHashCommit)) {
            rmStage(fileName);
            return;
        }
        addStage(fileName);
        stageRm.remove(fileName);
    }

    /**
     * Copies the last commit as the new commit.
     * Updates the files in staging area.
     * Untracking files in stageRm area.
     */
    public static void commit(String msg) throws IOException {
        if (isEmptyStage() && stageRm.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        } else if (msg.isBlank()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Commit newCommit = new Commit(getCommit("head"), msg);
        pointer.put(pointer.get("head"), newCommit.getHash());
        clearStage();
    }

    /**
     * If the file is staged for add, removes it from staging area.
     * If the file is tracked in current commit, deletes it from CWD
     * and stages it for rm.
     */
    public static void rm(String fileName) throws IOException {
        boolean staged = containsStage(fileName);
        boolean commited = getCommit("head").containFile(fileName);
        Path rmFile = CWD.resolve(fileName);
        if (!staged && !commited) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        if (staged) {
            rmStage(fileName);
        }
        if (commited && Files.exists(rmFile)) {
            restrictedDelete(fileName);
            stageRm.add(fileName);
        }
    }

    /** Log from head commit. */
    public static void log(String cid) {
        Commit node = getCommit(cid);
        node.display();
        if (cid.equals(pointer.get("initCommit"))) return;
        log(node.getParent());
    }

    /** Global-log. */
    public static void globalLog() throws IOException {
        try (Stream<Path> stream = Files.walk(COMMIT_DIR)) {
            stream.filter(Files::isRegularFile).forEach(cPath -> {
                Commit commit = readObject(cPath.toFile(), Commit.class);
                commit.display();
            });
        }
    }

    /** Find. */
    public static void find(String cMsg) throws IOException {
        AtomicInteger msgNum = new AtomicInteger(0);
        try (Stream<Path> stream = Files.walk(COMMIT_DIR)) {
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
        for (String file : stageRm) {
            System.out.println(file);
        }
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        Commit currentCommit = getCommit("head");
        for (Map.Entry<String, String> entry : currentCommit.blobEntrySet()) {
            String file = entry.getKey();
            Path cwd = CWD.resolve(file);
            if (Files.exists(cwd)) {
                String cwdHash = sha1((Object) readContents(cwd));
                if (!entry.getValue().equals(cwdHash) && !containsStage(file, cwdHash)) {
                    System.out.println(file + " (modified)");
                }
            } else if (stageRm.contains(file)) {
                System.out.println(file + " (deleted)");
            }
        }
        for (String file : stgFiles) {
            String stgHash = sha1((Object) readContents(STAGE_DIR.resolve(file)));
            Path cwd = CWD.resolve(file);
            if (Files.exists(cwd)) {
                String cwdHash = sha1((Object) readContents(cwd));
                if (!stgHash.equals(cwdHash)) {
                    System.out.println(file + " (modified)");
                }
            } else {
                System.out.println(file + " (deleted)");
            }
        }
        System.out.println("\n=== Untracked Files ===");
        List<String> cwdFiles = plainFilenamesIn(CWD);
        assert cwdFiles != null;
        cwdFiles.remove(".gitlet");
        for (String file : cwdFiles) {
            if (!currentCommit.containFile(file) && !containsStage(file)) {
                System.out.println(file);
            }
        }

    }

    /** Recover file status from commit id. */
    private static void recover(String cid) throws IOException {
        Commit commit = getCommit(cid); // Failure case at Commit.readCommit
        List<String> cwdFiles = plainFilenamesIn(CWD);
        assert cwdFiles != null;
        cwdFiles.remove(".gitlet");
        for (String file : cwdFiles) {
            String hash = sha1((Object) readContents(CWD.resolve(file)));
            if (!commit.containFile(file) || !commit.containFile(file, hash)) {
                System.out.println("There is an untracked file in the way;" +
                        " delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        writeCWD(commit);
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
        clearStage();
    }
    /** Checkout [commit id] -- [file name]. */
    public static void checkout(String cid, String file) throws IOException {
        Commit commit = getCommit(cid); // Failure case at Commit.readCommit
        if (!commit.containFile(file)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        writeCWD(commit, file);
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
        clearStage();
    }

    /** Merge. */
    public static void merge(String branch) {
    }

    /** Returns the commit instance of head or certain branch or commit id. */
    private static Commit getCommit(String p) {
        // if p is a name(head or branch name), cid = get(p), else(p is hash) cid = p
        if (p.equals("head")) p = pointer.get(p);
        return readCommit(pointer.getOrDefault(p, p));
    }

    /** Adds a file from CWD to STAGE_DIR.
     * There should not be a file in STAGE_DIR with same name and version.
     */
    public static void addStage(String fileName) throws IOException {
        Files.copy(CWD.resolve(fileName), STAGE_DIR.resolve(fileName), REPLACE_EXISTING);
    }

    /** Remove a file from STAGE_DIR. */
    public static void rmStage(String fileName) throws IOException {
        Files.delete(STAGE_DIR.resolve(fileName));
    }

    /** Returns if stage area contains a file. */
    public static boolean containsStage(String file) {
        List<String> allFiles = plainFilenamesIn(STAGE_DIR);
        assert allFiles != null;
        return allFiles.contains(file);
    }
    /** Returns if stage area contains a file with that exact hash. */
    public static boolean containsStage(String file, String hash) {
        List<String> allFiles = plainFilenamesIn(STAGE_DIR);
        assert allFiles != null;
        return (allFiles.contains(file) && hash.equals(sha1((Object) readContents(STAGE_DIR.resolve(file)))));
    }

    /** Returns if stage area is empty. */
    public static boolean isEmptyStage() {
        return plainFilenamesIn(STAGE_DIR) == null;
    }

    /** Clear the STAGE_DIR. */
    public static void clearStage() throws IOException {
        List<String> allFiles = plainFilenamesIn(STAGE_DIR);
        if (allFiles == null) {
            return;
        }
        for (String file : allFiles) {
            rmStage(file);
        }
    }


    /** Returns the commit instance according to hash. */
    public static Commit readCommit(String commitHash) {
        Path folder = COMMIT_DIR.resolve(commitHash.substring(0, 2));
        String restCommit = commitHash.substring(2);
        int sameID = 0;
        String commitFile = "";
        if (Files.exists(folder)) {
            List<String> allFiles = plainFilenamesIn(folder);
            assert allFiles != null;
            for (String file : allFiles) {
                if (file.startsWith(restCommit)) {
                    commitFile = file;
                    sameID++;
                }
            }
        }
        if (sameID == 0) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        } else if (sameID > 1) {
            System.out.println("Ambiguous commit id.");
            System.exit(0);
        }
        return readObject(folder.resolve(commitFile), Commit.class);
    }

    /** Serialize a commit into COMMIT_DIR. */
    public static void writeCommit(Commit c) throws IOException {
        Path subHash = COMMIT_DIR.resolve(c.getHash().substring(0, 2));
        Files.createDirectory(subHash);
        Utils.writeObject(subHash.resolve(c.getHash().substring(2)), c);
    }

    /** Serialize all files from stage area into blob folder.
     * Adds them to the newCommit blobs. */
    public static void commitFromStage(Commit newCommit) throws IOException {
        List<String> allFiles = plainFilenamesIn(STAGE_DIR);
        assert allFiles != null;
        if (allFiles.isEmpty()) {
            return;
        }
        for (String file : allFiles) {
            String hash = sha1((Object) readContents(STAGE_DIR.resolve(file)));
            newCommit.linkBlob(file, hash);
            Path Folder = BLOB_DIR.resolve(hash.substring(0, 2));
            Files.createDirectory(Folder);
            Files.copy(STAGE_DIR.resolve(file), Folder.resolve(hash.substring(2)));
        }
    }

    /** Overwrite a file from commit to CWD. */
    public static void writeCWD(Commit c, String fileName) throws IOException {
        String fileHash = c.getFileHash(fileName);
        Files.copy(
                BLOB_DIR.resolve(fileHash.substring(0,2)).resolve(fileHash.substring(2)),
                CWD.resolve(fileName), REPLACE_EXISTING);
    }

    /** Delete all files in CWD.
     * Overwrite all files from commit to CWD. */
    public static void writeCWD(Commit c) throws IOException {
        List<String> cwdFile = plainFilenamesIn(Repository.CWD);
        if (cwdFile != null) {
            cwdFile.remove(".gitlet");
            for (String file : cwdFile) {
                restrictedDelete(file);
            }
        }
        for (Map.Entry<String, String> entry : c.blobEntrySet()) {
            writeCWD(c, entry.getKey());
        }
    }


}
