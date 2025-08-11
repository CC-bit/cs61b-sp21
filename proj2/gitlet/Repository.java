package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


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
    private final TreeMap<String, String> branches;
    private final CommitManager commitManager;
    private final StageManager stageManager;
    private final WorkSpaceManager workSpaceManager;
    private final Path GITLET_DIR;
    private final Path CWD;

    public Repository(Path cwd) {
        CWD = cwd;
        this.GITLET_DIR = CWD.resolve(".gitlet");
        this.workSpaceManager = new WorkSpaceManager(cwd);
        this.blobManager = new BlobManager(GITLET_DIR);
        this.commitManager = new CommitManager(GITLET_DIR);
        this.stageManager = new StageManager(GITLET_DIR);
        this.branchManager = new BranchManager(GITLET_DIR);
        branches = branchManager.branches;
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
        workSpaceManager.writeCWD(br);
        branches.put("head", branchName);
        stageManager.clearStageAdd();
    }

    void switchBranch(String branchName) throws IOException {
        Commit br = getCommit(branchName);
        switchBranch(branchName, br);
    }

    /** Handling conflict file, add it to stageManager. **/
    void conflictHandle(String fileName, String curHash, String brHash)
            throws IOException {
        String newFile = "<<<<<<< HEAD\n"
                + blobManager.readBlob(curHash)
                + "=======\n"
                + blobManager.readBlob(brHash)
                + ">>>>>>>\n";
        Files.writeString(CWD.resolve(fileName), newFile);
        // add fileName
        stageManager.stageAdd(fileName);
    }

    /** Returns the commit instance of head or certain branchManager or commit id. */
    Commit getCommit(String id) {
        // if id is a name(head or branchManager name), cid = get(id), else(id is hash) cid = id
        if (id.equals("head")) {
            id = branches.get(id);
        }
        if (id == null) {
            return null;
        }
        String cid = branches.getOrDefault(id, id);
        return commitManager.readCommit(cid);
    }

    /** Put all ancestors of the commit into a set and return it. */
    Set<Commit> ancestorSet(Commit cur) {
        Set<Commit> ancestors = new HashSet<>();
        Queue<Commit> queue = new ArrayDeque<>();
        queue.offer(cur);
        while (queue.peek() != null) {
            Commit commit = queue.poll();
            ancestors.add(commit);
            Commit parent = getCommit(commit.getParentID());
            Commit secParent = getCommit(commit.getSecondParentID());
            if (!ancestors.contains(parent)) {
                queue.offer(parent);
            }
            if (!ancestors.contains(secParent)) {
                queue.offer(secParent);
            }
        }
        return ancestors;
    }

    Commit newCommit(Commit parent, Commit secParent, String msg)
            throws IOException {
        TreeMap<String, String> blobTree = parent.getBlobs();
        blobTree.putAll(blobManager.writeBlob());
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

}
