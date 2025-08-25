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
    private final BlobManager blobManager;
    private final BranchManager branchManager;
    private final CommitManager commitManager;
    private final StageManager stageManager;
    private final WorkSpaceManager workSpaceManager;
    private final RemoteManager remoteManager;
    private final Path GITLET_DIR;
    private final Path CWD;
    private final Path STAGE_DIR;
    private final Path BLOB_DIR;

    public Repository(Path cwd) {
        CWD = cwd;
        GITLET_DIR = CWD.resolve(".gitlet");
        workSpaceManager = new WorkSpaceManager(cwd);
        blobManager = new BlobManager(GITLET_DIR);
        BLOB_DIR = blobManager.getBlobPath();
        commitManager = new CommitManager(GITLET_DIR);
        stageManager = new StageManager(GITLET_DIR);
        STAGE_DIR = stageManager.getStagePath();
        branchManager = new BranchManager(GITLET_DIR);
        remoteManager = new RemoteManager(GITLET_DIR);
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
    RemoteManager getRemoteManager() {
        return remoteManager;
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

    /** Returns the commit instance of head or certain branch or commit id. */
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

    /** Handles files in stage area, creates and returns a new commit. */
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

    /** Handles files in stage area, creates and returns a new commit. */
    Commit newCommit(Commit parent, String msg) throws IOException {
        return newCommit(parent, null, msg);
    }

    /** Copies blobs from remote commit, creates and returns a new commit. */
    Commit newCommit(Path remoteBlobDir, Commit remoteCommit, Commit parent)
            throws IOException {
        TreeMap<String, String> remoteBlob = remoteCommit.getBlobs();
        Map<Path, String> pathMap = new TreeMap<>();
        for (Map.Entry<String, String> entry : remoteBlob.entrySet()) {
            String remoteFileHash = entry.getValue();
            Path filePath = remoteBlobDir.toAbsolutePath()
                    .resolve(remoteFileHash.substring(0, 2))
                    .resolve(remoteFileHash.substring(2));
            pathMap.put(filePath, remoteFileHash);
        }
        blobManager.writeBlob(pathMap);
        return new Commit(remoteCommit, parent);
    }

    /** Add the given commit to gitlet commit graph and write it to file.
     * branch, stage... */
    void commitGraph(Commit newCommit) throws IOException {
        String newID = newCommit.getID();
        commitManager.cacheCommit(newID, newCommit);
        branchManager.createBranch(branchManager.getCurBranchName(), newID);
        stageManager.clearStageAdd();
        stageManager.clearStageRm();
        commitManager.writeCommit(newCommit);
    }

    /** Put all ancestor commits of the given commit into a set and return it. */
    Set<Commit> ancestorCommitSet(Commit cur) {
        Set<Commit> ancestors = new HashSet<>();
        Queue<Commit> queue = new ArrayDeque<>();
        queue.offer(cur);
        while (queue.peek() != null) {
            Commit commit = queue.poll();
            ancestors.add(commit);
            Commit parent = getCommit(commit.getParentID());
            Commit secParent = getCommit(commit.getSecondParentID());
            if (parent != null && !ancestors.contains(parent)) {
                queue.offer(parent);
            }
            if (secParent != null && !ancestors.contains(secParent)) {
                queue.offer(secParent);
            }
        }
        return ancestors;
    }
    /** Put all ancestor commit hashes of the given commit into a set and return it. */
    Set<String> ancestorHashSet(Commit curCommit) {
        Set<Commit> commits = ancestorCommitSet(curCommit);
        Set<String> hashes = new HashSet<>();
        for (Commit commit: commits) {
            hashes.add(commit.getID());
        }
        return hashes;
    }

    /** Merge given branch into current branch. */
    void mergeBranch(String branchName) throws IOException {
        List<String> allStagedFiles = plainFilenamesIn(STAGE_DIR);
        assert allStagedFiles != null;
        if (!allStagedFiles.isEmpty() || !stageManager.isRmEmpty()) {
            throw new GitletException("You have uncommitted changes.");
        }
        if (!branchManager.containsBranch(branchName)) {
            throw new GitletException("A branch with that name does not exist.");
        }
        String curBranchName = branchManager.getCurBranchName();
        if (curBranchName.equals(branchName)) {
            throw new GitletException("Cannot merge a branch with itself.");
        }
        Commit curCommit = getCommit("head");
        Commit brCommit = getCommit(branchName);
        trackCheck(brCommit); // Failure case
        // Find split point
        Commit splitPoint = mergeSplitPoint(curCommit, brCommit, branchName); // Failure case
        // file handle
        boolean conflict = mergeFileHandle(curCommit, brCommit, splitPoint);
        // Merge commit
        String msg = "Merged " + branchName + " into " + curBranchName + ".";
        commitGraph(newCommit(curCommit, brCommit, msg));
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    private Commit mergeSplitPoint(Commit curCommit, Commit brCommit, String branchName)
            throws IOException {
        Commit splitPoint = getCommit("init");
        Set<String> ancestors = ancestorHashSet(curCommit);
        Queue<Commit> queue = new ArrayDeque<>();
        queue.offer(brCommit);
        while (queue.peek() != null) {
            Commit commit = queue.poll();
            if (ancestors.contains(commit.getID())) {
                splitPoint = commit;
                break;
            }
            Commit parent = getCommit(commit.getParentID());
            Commit secParent = getCommit(commit.getSecondParentID());
            if (parent != null) {
                queue.offer(parent);
            }
            if (secParent != null) {
                queue.offer(secParent);
            }
        }
        if (splitPoint.getID().equals(brCommit.getID())) {
            throw new GitletException("Given branch is an ancestor of the current branch.");
        }
        if (splitPoint.getID().equals(curCommit.getID())) {
            switchBranch(branchName, brCommit);
            throw new GitletException("Current branch fast-forwarded.");
        }
        return splitPoint;
    }

    /** File handle,
     *  return true if conflict exists, false otherwise. */
    private boolean mergeFileHandle(Commit curCommit, Commit brCommit, Commit splitPoint)
            throws IOException {
        boolean conflict = false;
        for (Map.Entry<String, String> entry : brCommit.blobEntrySet()) {
            String brFile = entry.getKey();
            String brHash = entry.getValue();
            String splitHash = splitPoint.getFileHash(brFile);
            String curHash = curCommit.getFileHash(brFile);
            boolean curNotContains = curCommit.isFileMissing(brFile);
            boolean splitNotContains = splitPoint.isFileMissing(brFile);
            if (!splitNotContains && !brHash.equals(splitHash)) { // in split with diff hash
                if (!curNotContains) {
                    if (curHash.equals(splitHash)) { // case 1
                        recoverFile(brCommit, brFile);
                        stageManager.stageAdd(brFile);
                    } else if (!brHash.equals(curHash)) { // case 8.1
                        conflict = true;
                        conflictHandle(brFile, curHash, brHash);
                    }
                } else { // case 8.2: file absent in curCommit
                    conflict = true;
                    conflictHandle(brFile, null, brHash);
                }
            } else if (!splitNotContains) { // case 7 and case 2
                continue;
            } else if (curNotContains) { // case 5
                recoverFile(brCommit, brFile);
                stageManager.stageAdd(brFile);
            } else if (!brHash.equals(curHash)) { // case 8.3
                conflict = true;
                conflictHandle(brFile, curHash, brHash);
            }
        }
        for (Map.Entry<String, String> entry : curCommit.blobEntrySet()) {
            String curFile = entry.getKey();
            String curHash = entry.getValue();
            String splitHash = splitPoint.getFileHash(curFile);
            String brHash = brCommit.getFileHash(curFile);
            boolean brNotContains = brCommit.isFileMissing(curFile);
            boolean splitNotContains = splitPoint.isFileMissing(curFile);
            if (!splitNotContains) {
                if (curHash.equals(splitHash)) {
                    if (brNotContains) { // case 6: rm curFile
                        stageManager.rmAddedFile(curFile);
                        restrictedDelete(CWD.resolve(curFile));
                        stageManager.stageRm(curFile);
                    }
                } else if (brNotContains) { // case 8.2: file absent in brCommit
                    conflict = true;
                    conflictHandle(curFile, curHash, brHash);
                }
            } else if (!brNotContains && !curHash.equals(brHash)) { // case 8.3
                conflict = true;
                conflictHandle(curFile, curHash, brHash);
            }
        }
        return conflict;
    }

    /** Fetch all commits and blobs from remote. */
    String fetchRemote(String remoteName, String remoteBrName) throws IOException {
        String commitDirName = commitManager.getCommitDirName();
        String blobDirName = blobManager.getBlobDirName();
        Path remoteRepoPath = remoteManager.getRemoteAbsolutePath(remoteName);
        Repository remoteRepo = new Repository(remoteRepoPath);
        if (!Files.exists(remoteRepoPath.resolve(".gitlet"))) {
            throw new GitletException("Remote directory not found.");
        }
        if (!remoteRepo.getBranchManager().containsBranch(remoteBrName)) {
            throw new GitletException("That remote does not have that branch.");
        }
        // copies all commits and blobs
        Commit remoteBrHead = remoteRepo.getCommit(remoteBrName);
        Set<Commit> remoteAncestor = remoteRepo.ancestorCommitSet(remoteBrHead);
        for (Commit commit : remoteAncestor) {
            fetchFile(remoteRepo, commitDirName, commit.getID());
            for (String blobHash : commit.getBlobs().values()) {
                fetchFile(remoteRepo, blobDirName, blobHash);
            }
        }
        return remoteBrHead.getID();
    }

    /** Copies a file whose name is a hash value(commit or blob) from remote to local. */
    private void fetchFile(Repository remoteRepo, String dirName, String hash)
            throws IOException {
        Path localCommitPath = GITLET_DIR.resolve(dirName)
                .resolve(hash.substring(0, 2))
                .resolve(hash.substring(2));
        if (Files.exists(localCommitPath)) {
            return;
        }
        Path remoteCommitPath = remoteRepo.getGitletPath().resolve(dirName)
                .resolve(hash.substring(0, 2))
                .resolve(hash.substring(2));
        Files.createDirectories(localCommitPath.getParent());
        Files.copy(remoteCommitPath, localCommitPath);
    }


}
