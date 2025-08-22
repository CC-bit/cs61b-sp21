package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author dhzp
 */
public class Repository {

    /** The current working directory. */
    private final BlobManager blobManager;
    private final BranchManager branchManager;
    private final CommitManager commitManager;
    private final StageManager stageManager;
    private final WorkSpaceManager workSpaceManager;
    private final Path GITLET_DIR;
    private final Path CWD;
    private final Path STAGE_DIR;
    private final Path BLOB_DIR;

    public Repository(Path cwd) {
        CWD = cwd;
        this.GITLET_DIR = CWD.resolve(".gitlet");
        this.workSpaceManager = new WorkSpaceManager(cwd);
        this.blobManager = new BlobManager(GITLET_DIR);
        BLOB_DIR = blobManager.getBlobPath();
        this.commitManager = new CommitManager(GITLET_DIR);
        this.stageManager = new StageManager(GITLET_DIR);
        STAGE_DIR = stageManager.getStagePath();
        this.branchManager = new BranchManager(GITLET_DIR);
    }
    Path getGitletPath() {
        return GITLET_DIR;
    }
    WorkSpaceManager getWorkSpaceManager() {
        return workSpaceManager;
    }
    BlobManager getBlobManager() {
        return blobManager;
    }
    CommitManager getCommitManager() {
        return commitManager;
    }
    StageManager getStageManager() {
        return stageManager;
    }
    BranchManager getBranchManager() {
        return branchManager;
    }

    void switchBranch(String branchName, Commit br) throws IOException {
        recoverFile(br);
        branchManager.createBranch("head", branchName);
        stageManager.clearStageAdd();
    }

    /** Handling conflict file, add it to stageManager. **/
    void conflictHandle(String fileName, String curHash, String brHash)
            throws IOException {
        String newFile = "<<<<<<< HEAD\n"
                + blobManager.readBlobToString(curHash)
                + "=======\n"
                + blobManager.readBlobToString(brHash)
                + ">>>>>>>\n";
        Files.writeString(CWD.resolve(fileName), newFile);
        // add fileName
        stageManager.stageAdd(fileName);
        stageManager.save();
    }

    /** Returns the commit instance of head or certain branchManager or commit id. */
    Commit getCommit(String id) {
        if (id == null) {
            return null;
        }
        // if id is a name(head or branchManager name), cid = get(id), else(id is hash) cid = id
        if (id.equals("head")) {
            id = branchManager.getCurBranchName();
        }
        if (branchManager.containsBranch(id)) {
            id = branchManager.getBrCommitID(id);
        }
        return commitManager.readCommit(id);
    }

    void recoverFile(Commit commit, String fileName) throws IOException {
        // get hash from commit, get filePath from hash
        String fileHash = commit.getFileHash(fileName);
        Path source = BLOB_DIR.resolve(fileHash.substring(0, 2))
                .resolve(fileHash.substring(2));
        workSpaceManager.writeCWD(source, fileName);
    }

    void recoverFile(Commit commit) throws IOException {
        workSpaceManager.clearCWD();
        for (Map.Entry<String, String> entry : commit.blobEntrySet()) {
            recoverFile(commit, entry.getKey());
        }
    }

    /** Check if CWD file tracked. */
    void trackCheck(Commit branch) {
        List<String> cwdFiles = workSpaceManager.cwdFileList();
        Commit cur = getCommit("head");
        for (String file : cwdFiles) {
            String cwdHash = sha1(file, readContents(CWD.resolve(file)));
            if (!branch.isFileMissing(file) && cur.isFileMissing(file, cwdHash)) {
                throw new GitletException("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
            }
        }
    }

    /** Display information. */
    public void displayCommit(Commit commit) {
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
        System.out.println(commit.getMessage());
    }

    Commit newCommit(Commit parent, Commit secParent, String msg)
            throws IOException {
        TreeMap<String, String> blobTree = parent.getBlobs();
        // get all files in stageAddArea into a map
        List<String> stagedFiles = plainFilenamesIn(STAGE_DIR);
        Map<Path, String> stagedFileMap = new TreeMap<>();
        for (String fileName : stagedFiles) {
            Path source = STAGE_DIR.resolve(fileName);
            String fileHash = sha1(fileName, readContents(source));
            stagedFileMap.put(source, fileHash);
            // put stage area file in blobTree
            blobTree.put(fileName, fileHash);
        }
        // write files to blob area
        blobManager.writeBlob(stagedFileMap);
        // stage rm area
        for (String k : stageManager.rmSet()) {
            blobTree.remove(k);
        }
        if (secParent == null) {
            return new Commit(parent, msg, blobTree);
        }
        return new Commit(parent, secParent, msg, blobTree);
    }

    Commit newCommit(Commit parent, String msg) throws IOException {
        return newCommit(parent, null, msg);
    }

    /** Add the given commit to gitlet commit graph and write it to file. */
    void commitGraph(Commit newCommit) throws IOException {
        String newID = newCommit.getID();
        commitManager.cacheCommit(newID, newCommit);
        branchManager.createBranch(branchManager.getCurBranchName(), newID);
        stageManager.clearStageAdd();
        stageManager.clearStageRm();
        commitManager.writeCommit(newCommit);
    }
}
