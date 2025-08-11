package gitlet;

import java.io.IOException;


public class CommitCommand extends AbstractCommand {

    public CommitCommand(Repository repo) {
        super(repo);
    }
    /** commit [message]
     * Copies the last commit as the new commit.
     * Updates the files in staging area.
     * Untracking files in rmArea area.
     * Returns the new commit.
     */
    @Override
    public void execute(String... args) throws IOException {
        if (args.length != 2) {
            throw new GitletException("Incorrect operands.");
        }
        if (stageManager.isAddEmpty() && stageManager.isRmEmpty()) {
            throw new GitletException("No changes added to the commit.");
        }
        String msg = args[1];
        if (msg.isBlank()) {
            throw new GitletException("Please enter a commit message.");
        }
        Commit parent = repo.getCommit("head");
        Commit newCommit = repo.newCommit(parent, msg);
        String newID = newCommit.getID();
        commitManager.cacheCommit(newID, newCommit);
        branchManager.createBranch(branchManager.getCurBranchName(), newID);
        stageManager.clearStageAdd();
        commitManager.writeCommit(newCommit);
        branchManager.save();
    }
}
