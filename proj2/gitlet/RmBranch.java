package gitlet;

import java.io.IOException;
import java.util.TreeMap;

public class RmBranch implements Command{
    TreeMap<String, String> branches;

    public RmBranch(Repository repo) {
        branches = repo.getBranchMan().branches;
    }

    @Override
    public void execute(String... args) throws IOException {
        if (args.length != 2) {
            throw new GitletException("Incorrect operands.");
        }
        String branchName = args[1];
        if (!branches.containsKey(branchName)) {
            throw new GitletException("A branchMan with that name does not exist.");
        }
        if (branches.get(branches.get("head")).equals(branchName)) {
            throw new GitletException("Cannot remove the current branchMan.");
        }
        branches.remove(branchName);
    }
}
