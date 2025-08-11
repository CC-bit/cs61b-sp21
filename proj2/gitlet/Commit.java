package gitlet;

import java.io.IOException;
import java.util.TreeMap;


public class Commit implements Command{
    private final Repository repo;
    private final Stage stage;
    private final CommitMan commitMan;
    private final TreeMap<String, String> branches;

    public Commit(Repository repo) {
        this.repo = repo;
        this.stage = repo.getStage();
        this.commitMan = repo.getCommitMan();
        branches = repo.getBranchMan().branches;
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
        if (stage.isEmpty() && stage.isRmEmpty()) {
            throw new GitletException("No changes added to the commit.");
        }
        String msg = args[1];
        if (msg.isBlank()) {
            throw new GitletException("Please enter a commit message.");
        }
        CommitInstance parent = repo.getCommit("head");
        CommitInstance newCommit = repo.newCommit(parent, msg);
        String newID = newCommit.getID();
        commitMan.cacheCommit(newID, newCommit);
        branches.put(branches.get("head"), newID);
        stage.clearStage();
        commitMan.writeCommit(newCommit);
    }
}
