package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author dhzp
 */
public class Repository {

    /** The current working directory. */
    private final Blob blob;
    private final BranchMan branchMan;
    private final TreeMap<String, String> branches;
    private final CommitMan commitMan;
    private final Stage stage;
    private final WorkSpace workSpace;
    private final Path GITLET_DIR;
    private final Path CWD;

    public Repository(Path cwd) {
        CWD = cwd;
        this.workSpace = new WorkSpace(cwd);
        this.GITLET_DIR = CWD.resolve(".gitlet");
        this.blob = new Blob(GITLET_DIR);
        this.branchMan = new BranchMan(GITLET_DIR);
        branches = branchMan.branches;
        this.commitMan = new CommitMan(GITLET_DIR);
        this.stage = new Stage(GITLET_DIR);
    }
    Path getGitletPath() {
        return GITLET_DIR;
    }
    WorkSpace getWorkSpace() {
        return workSpace;
    }
    Blob getBlob() {
        return blob;
    }
    BranchMan getBranchMan() {
        return branchMan;
    }
    CommitMan getCommitMan() {
        return commitMan;
    }
    Stage getStage() {
        return stage;
    }

    void switchBranch(String branchName, CommitInstance br) throws IOException {
        workSpace.writeCWD(br);
        branches.put("head", branchName);
        stage.clearStage();
    }

    void switchBranch(String branchName) throws IOException {
        CommitInstance br = getCommit(branchName);
        switchBranch(branchName, br);
    }

    /** Handling conflict file, add it to stage. **/
    void conflictHandle(String fileName, String curHash, String brHash)
            throws IOException {
        String newFile = "<<<<<<< HEAD\n" +
                blob.readBlob(curHash) +
                "=======\n" +
                blob.readBlob(brHash) +
                ">>>>>>>\n";
        Files.writeString(CWD.resolve(fileName), newFile);
        // add fileName
        stage.addFile(fileName);
    }

    /** Returns the commit instance of head or certain branchMan or commit id. */
    CommitInstance getCommit(String id) {
        // if id is a name(head or branchMan name), cid = get(id), else(id is hash) cid = id
        if (id.equals("head")) {
            id = branches.get(id);
        }
        if (id == null) {
            return null;
        }
        String cid = branches.getOrDefault(id, id);
        return commitMan.readCommit(cid);
    }

    /** Put all ancestors of the commit into a set and return it. */
    Set<CommitInstance> ancestorSet(CommitInstance cur) {
        Set<CommitInstance> ancestors = new HashSet<>();
        Queue<CommitInstance> queue = new ArrayDeque<>();
        queue.offer(cur);
        while (queue.peek() != null) {
            CommitInstance commit = queue.poll();
            ancestors.add(commit);
            CommitInstance parent = getCommit(commit.getParentID());
            CommitInstance secParent = getCommit(commit.getSecondParentID());
            if (!ancestors.contains(parent)) {
                queue.offer(parent);
            }
            if (!ancestors.contains(secParent)) {
                queue.offer(secParent);
            }
        }
        return ancestors;
    }

    CommitInstance newCommit(CommitInstance parent, CommitInstance secParent, String msg)
            throws IOException {
        TreeMap<String, String> blobTree = parent.getBlobs();
        blobTree.putAll(blob.writeBlob());
        for (String k : stage.rmArea) {
            blobTree.remove(k);
        }
        if (secParent == null) {
            return new CommitInstance(parent, msg, blobTree);
        }
        return new CommitInstance(parent, secParent, msg, blobTree);
    }
    CommitInstance newCommit(CommitInstance parent, String msg) throws IOException {
        return newCommit(parent, null, msg);
    }

}
