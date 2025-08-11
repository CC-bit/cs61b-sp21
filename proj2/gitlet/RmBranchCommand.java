package gitlet;

import java.io.IOException;

public class RmBranchCommand extends AbstractCommand {

    public RmBranchCommand(Repository repo) {
        super(repo);
    }

    @Override
    public void execute(String... args) throws IOException {
        if (args.length != 2) {
            throw new GitletException("Incorrect operands.");
        }
        String branchName = args[1];
        if (!branchManager.containsBranch(branchName)) {
            throw new GitletException("A branchMan with that name does not exist.");
        }
        if (branchManager.getBrCommitID("head").equals(branchName)) {
            throw new GitletException("Cannot remove the current branchMan.");
        }
        branchManager.deleteBranch(branchName);
        branchManager.save();
    }
}
