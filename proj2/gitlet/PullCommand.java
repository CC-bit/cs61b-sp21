package gitlet;

import java.io.IOException;

public class PullCommand extends AbstractCommand {

    public PullCommand(Repository repo) {
        super(repo);
    }

    @Override
    public void execute(String... args) throws IOException {
        if (args.length != 3) {
            throw new GitletException("Incorrect operands.");
        }
        String remoteName = args[1];
        String remoteBrName = args[2];
        String remoteBrHeadID = repo.fetchRemote(remoteName, remoteBrName); // failure case
        String newBrName = remoteName + "/" + remoteBrName;
        // new branch remoteName/remoteBrName
        branchManager.createBranch(newBrName, remoteBrHeadID);
        // merge
        repo.mergeBranch(newBrName);
        stageManager.save();
        branchManager.save();
    }
}
