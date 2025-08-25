package gitlet;

import java.io.IOException;

public class FetchCommand extends AbstractCommand {

    public FetchCommand(Repository repo) {
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
        // new branch remoteName/remoteBrName
        branchManager.createBranch(remoteName + "/" + remoteBrName,
                remoteBrHeadID);
        branchManager.save();
    }
}
