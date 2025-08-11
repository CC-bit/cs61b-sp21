package gitlet;

import java.io.IOException;

public class CheckOutCommand extends AbstractCommand {
    public CheckOutCommand(Repository repo) {
        super(repo);
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
        if (!branchManager.containsBranch(branchName)) {
            throw new GitletException("No such branch exists.");
        }
        if (branchManager.getCurBranchName().equals(branchName)) {
            throw new GitletException("No need to checkout the current branch.");
        }
        Commit br = repo.getCommit(branchName);
        workSpaceManager.trackCheck(repo.getCommit("head"), br); // failure case
        repo.switchBranch(branchName, br); // switch branch
    }

    /** Checkout [commit id] -- [file name]. */
    public void checkout(String cid, String file) throws IOException {
        Commit commit = repo.getCommit(cid); // Failure case at Commit.readCommit
        if (commit.isFileMissing(file)) {
            throw new GitletException("File does not exist in that commit.");
        }
        workSpaceManager.writeCWD(commit, file);
    }

}
