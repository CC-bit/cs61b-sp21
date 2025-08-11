package gitlet;

import java.io.IOException;
import java.util.TreeMap;

public class CheckOut implements Command{
    TreeMap<String, String> branches;
    private final WorkSpace workSpace;
    private final Repository repo;

    public CheckOut(Repository repo) {
        this.repo = repo;
        branches = repo.getBranchMan().branches;
        workSpace = repo.getWorkSpace();
    }

    @Override
    public void execute(String... args) throws IOException {
        int l = args.length;
        if (l == 2) {
            checkout(args[1]);
        } else if (l == 3) {
            checkout("head", args[2]);
        } else if (l == 4) {
            checkout(args[1], args[3]);
        } else {
            throw new GitletException("Incorrect operands.");
        }
    }

    /** Checkout [branchMan name]. */
    public void checkout(String branchName) throws IOException {
        if (!branches.containsKey(branchName)) {
            throw new GitletException("No such branch exists.");
        }
        if (branches.get("head").equals(branchName)) {
            throw new GitletException("No need to checkout the current branch.");
        }
        CommitInstance br = repo.getCommit(branchName);
        workSpace.trackCheck(repo.getCommit("head"), br); // failure case
        repo.switchBranch(branchName, br); // switch branch
    }

    /** Checkout [commit id] -- [file name]. */
    public void checkout(String cid, String file) throws IOException {
        CommitInstance commit = repo.getCommit(cid); // Failure case at CommitInstance.readCommit
        if (commit.isFileMissing(file)) {
            throw new GitletException("File does not exist in that commit.");
        }
        workSpace.writeCWD(commit, file);
    }

}
