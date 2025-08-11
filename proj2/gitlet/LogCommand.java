package gitlet;

import java.io.IOException;

public class LogCommand extends AbstractCommand {

    public LogCommand(Repository repo) {
        super(repo);
    }

    /** LogCommand from head commit. */
    @Override
    public void execute(String... args) throws IOException {
        if (args.length != 1) {
            throw new GitletException("Incorrect operands.");
        }

        log(repo.getCommit("head"));
    }

    private void log(Commit commit) {
        repo.displayCommit(commit);
        if (commit.getID().equals(branchManager.getBrCommitID("init"))) {
            return;
        }
        log(repo.getCommit(commit.getParentID()));
    }
}
