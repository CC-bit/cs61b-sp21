package gitlet;

import java.io.IOException;
import java.util.TreeMap;

public class Log implements Command{
    private final Repository repo;
    private final CommitMan commitMan;
    private final TreeMap<String, String> branches;

    public Log(Repository repo) {
        this.repo = repo;
        this.commitMan = repo.getCommitMan();
        branches = repo.getBranchMan().branches;
    }

    /** Log from head commit. */
    @Override
    public void execute(String... args) throws IOException {
        if (args.length != 1) {
            throw new GitletException("Incorrect operands.");
        }

        log(repo.getCommit("head"));
    }

    private void log(CommitInstance commit) {
        commitMan.display(commit);
        if (commit.getID().equals(branches.get("init"))) {
            return;
        }
        log(repo.getCommit(commit.getParentID()));
    }
}
