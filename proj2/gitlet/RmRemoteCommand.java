package gitlet;

import java.io.IOException;

public class RmRemoteCommand extends AbstractCommand {

    public RmRemoteCommand(Repository repo) {
        super(repo);
    }

    @Override
    public void execute(String... args) throws IOException {
        if (args.length != 2) {
            throw new GitletException("Incorrect operands.");
        }
        String name = args[1];
        if (!remoteManager.containsRemote(name)) {
            throw new GitletException("A remote with that name does not exist.");
        }
        remoteManager.rmRemote(name);
        remoteManager.save();
    }
}
