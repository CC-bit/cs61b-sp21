package gitlet;

import java.io.IOException;
import java.nio.file.Path;

public class AddRemoteCommand extends AbstractCommand {

    public AddRemoteCommand(Repository repo) {
        super(repo);
    }

    @Override
    public void execute(String... args) throws IOException {
        if (args.length != 3) {
            throw new GitletException("Incorrect operands.");
        }
        String name = args[1];
        if (remoteManager.containsRemote(name)) {
            throw new GitletException("A remote with that name already exists.");
        }
        remoteManager.addRemote(name, args[2]);
        remoteManager.save();
    }
}
