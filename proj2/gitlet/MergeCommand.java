package gitlet;

import java.io.IOException;

public class MergeCommand extends AbstractCommand {

    public MergeCommand(Repository repo) {
        super(repo);
    }

    @Override
    public void execute(String... args) throws IOException {
        if (args.length != 2) {
            throw new GitletException("Incorrect operands.");
        }
        repo.mergeBranch(args[1]); // failure case
        stageManager.save();
        branchManager.save();
    }
}
