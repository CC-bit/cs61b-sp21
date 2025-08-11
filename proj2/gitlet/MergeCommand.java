package gitlet;

import java.io.IOException;
import java.util.*;

import static gitlet.Utils.plainFilenamesIn;
import static gitlet.Utils.restrictedDelete;

public class MergeCommand extends AbstractCommand {

    public MergeCommand(Repository repo) {
        super(repo);
    }

    @Override
    public void execute(String... args) throws IOException {
        if (args.length != 2) {
            throw new GitletException("Incorrect operands.");
        }
        String branchName = args[1];
        // Failure cases
        List<String> allFiles = plainFilenamesIn(STAGE_DIR);
        assert allFiles != null;
        if (!allFiles.isEmpty() || !stageManager.isRmEmpty()) {
            throw new GitletException("You have uncommitted changes.");
        }
        if (!branchManager.containsBranch(branchName)) {
            throw new GitletException("A branch with that name does not exist.");
        }
        String curBranchName = branchManager.getCurBranchName();
        if (curBranchName.equals(branchName)) {
            throw new GitletException("Cannot merge a branch with itself.");
        }
        // FindCommand split point
        Commit curCommit = repo.getCommit("head");
        Commit brCommit = repo.getCommit(branchName);
        repo.trackCheck(brCommit); // Failure case
        Commit splitPoint = repo.getCommit("init");
        Set<Commit> ancestors = repo.ancestorSet(curCommit);
        Queue<Commit> queue = new ArrayDeque<>();
        queue.offer(brCommit);
        while (queue.peek() != null) {
            Commit commit = queue.poll();
            if (ancestors.contains(commit)) {
                splitPoint = commit;
                break;
            }
            Commit parent = repo.getCommit(commit.getParentID());
            Commit secParent = repo.getCommit(commit.getSecondParentID());
            if (parent == null || secParent == null) {
                break;
            }
            queue.offer(parent);
            queue.offer(secParent);
        }
        if (splitPoint.getID().equals(branchName)) {
            throw new GitletException("Given branch is an ancestor of the current branch.");
        }
        if (splitPoint.getID().equals(curCommit.getID())) {
            repo.switchBranch(branchName, brCommit);
            throw new GitletException("Current branch fast-forwarded.");
        }
        // file handle
        for (Map.Entry<String, String> entry : brCommit.blobEntrySet()) {
            String brFile = entry.getKey();
            String brHash = entry.getValue();
            String splitHash = splitPoint.getFileHash(brFile);
            String curHash = curCommit.getFileHash(brFile);
            boolean curNotContains = curCommit.isFileMissing(brFile);
            boolean splitNotContains = splitPoint.isFileMissing(brFile);
            if (!splitNotContains && !brHash.equals(splitHash)) {
                if (!curNotContains) {
                    if (curHash.equals(splitHash)) {
                        // case 1 file exits in 3 commit curCommit
                        // and split has same hash, brCommit diff
                        // checkout brCommit brFile
                        repo.recoverFile(brCommit, brFile);
                        // add fileName
                        stageManager.stageAdd(brFile);
                    } else if (!brHash.equals(curHash)) {
                        // case 8.1: in split, curCommit and brCommit has different hash
                        repo.conflictHandle(brFile, curHash, brHash);
                    }
                } else {
                    // case 8.2: file absent in curCommit
                    repo.conflictHandle(brFile, null, brHash);
                }
            } else if (curNotContains) {
                // case 5: file is in brCommit, not in split, not in curCommit
                // checkout brCommit brFile
                repo.recoverFile(brCommit, brFile);
                // add brFile
                stageManager.stageAdd(brFile);
            } else if (!brHash.equals(curHash)) {
                // case 8.3: absent in split, and diff hash between curCommit and brCommit
                repo.conflictHandle(brFile, curHash, brHash);
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
                    if (brNotContains) {
                        // case 6: rm curFile
                        stageManager.rmAddedFile(curFile);
                        restrictedDelete(curFile);
                        stageManager.stageRm(curFile);
                    }
                } else if (brNotContains) {
                    // case 8.2: file absent in brCommit
                    repo.conflictHandle(curFile, curHash, brHash);
                }
            } else if (!brNotContains && !curHash.equals(brHash)) {
                // case 8.3: absent in split, and diff hash between curCommit and brCommit
                repo.conflictHandle(curFile, curHash, brHash);
            }
        }
        // Create merge commit
        String msg = "Merged " + branchName + " into " + curBranchName + ".";
        repo.newCommit(curCommit, brCommit, msg);
        stageManager.save();
    }
}
