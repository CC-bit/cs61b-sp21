package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class PushCommand extends AbstractCommand {

    public PushCommand(Repository repo) {
        super(repo);
    }

    /** Push current commit to the remote branch's head. */
    @Override
    public void execute(String... args) throws IOException {
        if (args.length != 3) {
            throw new GitletException("Incorrect operands.");
        }
        Commit cur = repo.getCommit("head");
        Path remotePath = remoteManager.getRemoteAbsolutePath(args[1]);
        Repository remoteRepo = new Repository(remotePath);
        Commit remoteHead = remoteRepo.getCommit("head");
        Set<String> curAncestors = repo.ancestorHashSet(cur);
        if (!curAncestors.contains(remoteHead.getID())) {
            throw new GitletException("Please pull down remote changes before pushing.");
        }
        if (!Files.exists(remotePath.resolve(".gitlet"))) {
            throw new GitletException("Remote directory not found.");
        }
        remoteRepo.commitGraph(remoteRepo.newCommit(BLOB_DIR, cur, remoteRepo.getCommit(args[2])));
        remoteRepo.getStageManager().save();
        remoteRepo.getBranchManager().save();
    }
}
