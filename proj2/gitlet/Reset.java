package gitlet;

import java.io.IOException;
import java.util.TreeMap;

public class Reset implements Command{
    private final WorkSpace workSpace;
    private final Repository repo;
    private final Stage stage;
    TreeMap<String, String> branches;

    public Reset(Repository repo) {
        this.repo = repo;
        stage = repo.getStage();
        branches = repo.getBranchMan().branches;
        workSpace = repo.getWorkSpace();
    }

    @Override
    public void execute(String... args) throws IOException {
        if (args.length != 2) {
            throw new GitletException("Incorrect operands.");
        }
        String cid = args[1];
        CommitInstance br = repo.getCommit(args[1]);
        workSpace.trackCheck(repo.getCommit("head"), br);
        workSpace.writeCWD(br);
        branches.put(branches.get("head"), cid);
        stage.clearStage();
    }
}
