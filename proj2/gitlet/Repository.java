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
    /** The branches file. */
    public static final Path BRANCHES = GITLET_DIR.resolve("branch");
    /** A map of branches names like "master" to commit hash. */
    public static TreeMap<String, String> branches;
    private static final String init = "initCommit";
    private static final String head = "head";
    private static final String master = "master";
    /** A map of commit id to commit instance. */
    private static final TreeMap<String, Commit> commitCache = new TreeMap<>();

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
        failureCase(Files.exists(GITLET_DIR),
                "A Gitlet version-control system already exists in the current directory.");
        Files.createDirectory(GITLET_DIR);
        Files.createDirectory(STAGE_DIR);
        Files.createDirectory(BLOB_DIR);
        Files.createDirectory(COMMIT_DIR);
        Commit initCommit = new Commit();
        String initHash = initCommit.getID();
        branches.put(init, initHash);
        branches.put(master, initHash);
        branches.put(head, master);
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
        String fileHashCommit = getCommit(head).getFileHash(fileName);
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
     * Returns the new commit.
     */
    public static Commit commit(String msg) throws IOException {
        failureCase(isEmptyStage() && stageRm.isEmpty(),
                "No changes added to the commit.");
        failureCase(msg.isBlank(), "Please enter a commit message.");
        Commit newCommit = new Commit(getCommit(head), msg);
        String newID = newCommit.getID();
        commitCache.put(newID, newCommit);
        branches.put(branches.get(head), newID);
        clearStage();
        return newCommit;
    }

    /**
     * If the file is staged for add, removes it from staging area.
     * If the file is tracked in current commit, deletes it from CWD
     * and stages it for rm.
     */
    public static void rm(String fileName) throws IOException {
        boolean staged = containsStage(fileName);
        boolean notCommited = getCommit(head).isFileMissing(fileName);
        Path rmFile = CWD.resolve(fileName);
        failureCase(!staged && notCommited, "No reason to remove the file.");
        if (staged) {
            rmStage(fileName);
        }
        if (!notCommited && Files.exists(rmFile)) {
            restrictedDelete(fileName);
            stageRm.add(fileName);
        }
    }

    /** Log from head commit. */
    public static void log(Commit commit) {
        commit.display();
        if (commit.getID().equals(branches.get(init))) return;
        log(commit.getParent());
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
                    System.out.println(commit.getID());
                }
            });
        }
        failureCase(msgNum.get() == 0, "Found no commit with that message.");
    }

    /** Status. */
    public static void status() {
        List<String> stgFiles = plainFilenamesIn(STAGE_DIR);
        assert stgFiles != null;
        System.out.println("=== Branches ===");
        System.out.println("*" + branches.get(head));
        for (Map.Entry<String, String> entry : branches.entrySet()) {
            if (entry.getKey().equals(head)) continue;
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
        Commit currentCommit = getCommit(head);
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
            if (currentCommit.isFileMissing(file) && !containsStage(file)) {
                System.out.println(file);
            }
        }
    }

    /** Check if CWD file tracked. */
    private static void CWDTrackCheck(Commit branch) {
        Commit cur = getCommit(head);
        List<String> cwdFiles = plainFilenamesIn(CWD);
        assert cwdFiles != null;
        cwdFiles.remove(".gitlet");
        for (String file : cwdFiles) {
            String hash = sha1((Object) readContents(CWD.resolve(file)));
            failureCase(cur.isFileMissing(file, hash) && branch.isFileMissing(file, hash),
                    "There is an untracked file in the way;" +
                        " delete it, or add and commit it first.");
        }
    }
    /** Recover file status from commit id. */
    private static void recover(String branchID) throws IOException {
        Commit branch = getCommit(branchID); // Failure case at Commit.readCommit
        CWDTrackCheck(branch); // Failure case
        writeCWD(branch);
    }

    /** Checkout [branch name]. */
    public static void checkout(String branch) throws IOException {
        failureCase(!branches.containsKey(branch), "No such branch exists.");
        failureCase(branches.get(head).equals(branch),
                "No need to checkout the current branch.");
        recover(branch);
        branches.put(head, branch);
        clearStage();
    }

    /** Checkout [commit id] -- [file name]. */
    public static void checkout(String cid, String file) throws IOException {
        Commit commit = getCommit(cid); // Failure case at Commit.readCommit
        failureCase(commit.isFileMissing(file), "File does not exist in that commit.");
        writeCWD(commit, file);
    }

    /** Branch. */
    public static void branch(String branchName) {
        failureCase(branches.containsKey(branchName),
                "A branch with that name already exists.");
        branches.put(branchName, branches.get(branches.get(head)));
    }

    /** Rm-branch. */
    public static void rmBranch(String branchName) {
        failureCase(!branches.containsKey(branchName),
            "A branch with that name does not exist.");
        failureCase(branches.get(head).equals(branchName),
                "Cannot remove the current branch.");
        branches.remove(branchName);
    }

    /** Reset. */
    public static void reset(String cid) throws IOException {
        recover(cid);
        branches.put(branches.get(head), cid);
        clearStage();
    }

    /** Put all ancestors of the commit into a set and return it. */
    private static Set<Commit> ancestorSet(Commit cur) {
        Set<Commit> ancestors = new HashSet<>();
        Queue<Commit> queue = new ArrayDeque<>();
        queue.offer(cur);
        while (queue.peek() != null) {
            Commit commit = queue.poll();
            ancestors.add(commit);
            Commit parent = commit.getParent();
            Commit secParent = commit.getSecondParent();
            if (!ancestors.contains(parent)) {
                queue.offer(parent);
            }
            if (!ancestors.contains(secParent)) {
                queue.offer(secParent);
            }
        }
        return ancestors;
    }

    /** Read from File hash to String. */
    private static String readBlob(String fileID) throws IOException {
        String file;
        if (fileID == null) {
            file = "";
        } else {
            file = Files.readString(BLOB_DIR
                    .resolve(fileID.substring(0, 2))
                    .resolve(fileID.substring(2)));
        }
        return file;
    }
    /** Handling conflict file. **/
    private static void conflictHandle(String fileName, String curHash, String brHash)
            throws IOException {
        String newFile = "<<<<<<< HEAD\n" +
                readBlob(curHash) +
                "=======\n" +
                readBlob(brHash) +
                ">>>>>>>\n";
        Files.writeString(CWD.resolve(fileName), newFile);
        add(fileName);
    }

    /** Merge. */
    public static void merge(String branch) throws IOException {
        // Failure cases
        List<String> allFiles = plainFilenamesIn(STAGE_DIR);
        assert allFiles != null;
        failureCase(!allFiles.isEmpty() || !stageRm.isEmpty(),
                "You have uncommitted changes.");
        failureCase(!branches.containsKey(branch), "A branch with that name does not exist.");
        failureCase(branches.get(head).equals(branch), "Cannot merge a branch with itself.");
        // Find split point
        Commit br = getCommit(branch);
        CWDTrackCheck(br); // Failure case
        Commit cur = getCommit(head);
        Commit splitPoint = getCommit(init);
        Set<Commit> ancestors = ancestorSet(cur);
        Queue<Commit> queue = new ArrayDeque<>();
        queue.offer(br);
        while (queue.peek() != null) {
            Commit commit = queue.poll();
            if (ancestors.contains(commit)) {
                splitPoint = commit;
                break;
            }
            Commit parent = commit.getParent();
            Commit secParent = commit.getSecondParent();
            if (parent == null || secParent == null) {
                break;
            }
            queue.offer(parent);
            queue.offer(secParent);
        }
        failureCase(splitPoint.getID().equals(branch),
                "Given branch is an ancestor of the current branch.");
        if (splitPoint.getID().equals(cur.getID())) {
            checkout(branch);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        // file handle
        for (Map.Entry<String, String> entry : br.blobEntrySet()) {
            String brFile = entry.getKey();
            String brHash = entry.getValue();
            String splitHash = splitPoint.getFileHash(brFile);
            String curHash = cur.getFileHash(brFile);
            boolean curNotContains = cur.isFileMissing(brFile);
            boolean splitNotContains = splitPoint.isFileMissing(brFile);
            if (!splitNotContains && !brHash.equals(splitHash)) {
                if (!curNotContains) {
                    if (curHash.equals(splitHash)) {
                        // case 1
                        checkout(branch, brFile);
                        add(brFile);
                    } else if (!brHash.equals(curHash)) {
                        // case 8.1: in split, cur and br has different hash
                        conflictHandle(brFile, curHash, brHash);
                    }
                } else {
                    // case 8.2: file absent in cur
                    conflictHandle(brFile, null, brHash);
                }
            } else if (curNotContains) {
                // case 5: file is in br, not in split, not in cur
                checkout(branch, brFile);
                add(brFile);
            } else if (!brHash.equals(curHash)) {
                // case 8.3: absent in split, and diff hash between cur and br
                conflictHandle(brFile, curHash, brHash);
            }
        }
        for (Map.Entry<String, String> entry : cur.blobEntrySet()) {
            String curFile = entry.getKey();
            String curHash = entry.getValue();
            String splitHash = splitPoint.getFileHash(curFile);
            String brHash = br.getFileHash(curFile);
            boolean brNotContains = br.isFileMissing(curFile);
            boolean splitNotContains = splitPoint.isFileMissing(curFile);
            if (!splitNotContains) {
                if (curHash.equals(splitHash)) {
                    if (brNotContains) {
                        // case 6: rm
                        rm(curFile);
                    }
                } else if (brNotContains) {
                    // case 8.2: file absent in br
                    conflictHandle(curFile, curHash, brHash);
                }
            } else if (!brNotContains && !curHash.equals(brHash)) {
                // case 8.3: absent in split, and diff hash between cur and br
                conflictHandle(curFile, curHash, brHash);
            }
        }
        // Create merge commit
        Commit newCommit = commit("Merged " + branch + " into " + branches.get(head) + ".");
        newCommit.linkSecParent(br.getID());
    }

    /** Returns the commit instance of head or certain branch or commit id. */
    public static Commit getCommit(String id) {
        // if id is a name(head or branch name), cid = get(id), else(id is hash) cid = id
        if (id.equals(head)) id = branches.get(id);
        String cid = branches.getOrDefault(id, id);
        // check if commit is in cache
        if (commitCache.containsKey(cid)) {
            return commitCache.get(cid);
        }
        // find the target commit
        Path folder = COMMIT_DIR.resolve(cid.substring(0, 2));
        String restCid = cid.substring(2);
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
        failureCase(commitNum == 0, "No commit with that id exists.");
        failureCase(commitNum > 1, "Ambiguous commit id.");
        Commit target = readObject(folder.resolve(commit), Commit.class);
        commitCache.put(cid, target);
        return target;
    }

    /** Serialize a commit into COMMIT_DIR. */
    public static void writeCommit(Commit c) throws IOException {
        Path subHash = COMMIT_DIR.resolve(c.getID().substring(0, 2));
        Files.createDirectory(subHash);
        Utils.writeObject(subHash.resolve(c.getID().substring(2)), c);
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

    /** Adds a file from CWD to STAGE_DIR.
     * There should not be a file in STAGE_DIR with same name and version.
     */
    private static void addStage(String fileName) throws IOException {
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
    private static boolean containsStage(String file, String hash) {
        List<String> allFiles = plainFilenamesIn(STAGE_DIR);
        assert allFiles != null;
        return (allFiles.contains(file)
                && hash.equals(sha1((Object) readContents(STAGE_DIR.resolve(file)))));
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
}
