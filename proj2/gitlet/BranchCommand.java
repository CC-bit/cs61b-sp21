package gitlet;

import java.io.IOException;

public class BranchCommand extends AbstractCommand {

    public BranchCommand(Repository repo) {
        super(repo);
    }

    @Override
    public void execute(String... args) throws IOException {
        if (args.length != 2) {
            throw new GitletException("Incorrect operands.");
        }
        String branchName = args[1];
        if (branchManager.containsBranch(branchName)) {
            throw new GitletException("A branchMan with that name already exists.");
        }
        branchManager.createBranch(branchName, branchManager.getBrCommitID("head"));
        branchManager.save();
    }
}
