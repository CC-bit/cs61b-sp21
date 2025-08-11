package gitlet;

import java.io.IOException;

public class ResetCommand extends AbstractCommand {

    public ResetCommand(Repository repo) {
        super(repo);
    }

    @Override
    public void execute(String... args) throws IOException {
        if (args.length != 2) {
            throw new GitletException("Incorrect operands.");
        }
        String cid = args[1];
        Commit br = repo.getCommit(args[1]);
        workSpaceManager.trackCheck(repo.getCommit("head"), br);
        workSpaceManager.writeCWD(br);
        branchManager.createBranch(branchManager.getCurBranchName(), cid);
        stageManager.clearStageAdd();
    }
}
