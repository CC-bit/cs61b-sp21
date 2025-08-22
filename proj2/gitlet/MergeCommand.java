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
        Commit curCommit = repo.getCommit("head");
        Commit brCommit = repo.getCommit(branchName);
        repo.trackCheck(brCommit); // Failure case
        // Find split point
        Commit splitPoint = findSplitPoint(curCommit, brCommit, branchName); // Failure case
        // file handle
        boolean conflict = fileHandle(curCommit, brCommit, splitPoint);
        // Merge commit
        String msg = "Merged " + branchName + " into " + curBranchName + ".";
        repo.commitGraph(repo.newCommit(curCommit, brCommit, msg));
        stageManager.save();
        branchManager.save();
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** Put all ancestors of the commit into a set and return it. */
    private Set<Commit> ancestorSet(Commit cur) {
        Set<Commit> ancestors = new HashSet<>();
        Queue<Commit> queue = new ArrayDeque<>();
        queue.offer(cur);
        while (queue.peek() != null) {
            Commit commit = queue.poll();
            ancestors.add(commit);
            Commit parent = repo.getCommit(commit.getParentID());
            Commit secParent = repo.getCommit(commit.getSecondParentID());
            if (parent != null && !ancestors.contains(parent)) {
                queue.offer(parent);
            }
            if (secParent != null && !ancestors.contains(secParent)) {
                queue.offer(secParent);
            }
        }
        return ancestors;
    }

    private Commit findSplitPoint(Commit curCommit, Commit brCommit, String branchName)
            throws IOException {
        Commit splitPoint = repo.getCommit("init");
        Set<Commit> ancestors = ancestorSet(curCommit);
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
            repo.switchBranch(branchName, brCommit);
            throw new GitletException("Current branch fast-forwarded.");
        }
        return splitPoint;
    }

    /** File handle,
     *  return true if conflict exists, false otherwise. */
    private boolean fileHandle(Commit curCommit, Commit brCommit, Commit splitPoint)
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
                        repo.recoverFile(brCommit, brFile);
                        stageManager.stageAdd(brFile);
                    } else if (!brHash.equals(curHash)) { // case 8.1
                        conflict = true;
                        repo.conflictHandle(brFile, curHash, brHash);
                    }
                } else { // case 8.2: file absent in curCommit
                    conflict = true;
                    repo.conflictHandle(brFile, null, brHash);
                }
            } else if (!splitNotContains) { // case 7 and case 2
                continue;
            } else if (curNotContains) { // case 5
                repo.recoverFile(brCommit, brFile);
                stageManager.stageAdd(brFile);
            } else if (!brHash.equals(curHash)) { // case 8.3
                conflict = true;
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
                    if (brNotContains) { // case 6: rm curFile
                        stageManager.rmAddedFile(curFile);
                        restrictedDelete(CWD.resolve(curFile));
                        stageManager.stageRm(curFile);
                    }
                } else if (brNotContains) { // case 8.2: file absent in brCommit
                    conflict = true;
                    repo.conflictHandle(curFile, curHash, brHash);
                }
            } else if (!brNotContains && !curHash.equals(brHash)) { // case 8.3
                conflict = true;
                repo.conflictHandle(curFile, curHash, brHash);
            }
        }
        return conflict;
    }

}
