package gitlet;

import java.io.IOException;
import java.util.TreeMap;

public class Branch implements Command{
    private final TreeMap<String, String> branches;

    public Branch(Repository repo) {
        branches = repo.getBranchMan().branches;
    }

    @Override
    public void execute(String... args) throws IOException {
        if (args.length != 2) {
            throw new GitletException("Incorrect operands.");
        }
        String branchName = args[1];
        if (branches.containsKey(branchName)) {
            throw new GitletException("A branchMan with that name already exists.");
        }
        branches.put(branchName, branches.get(branches.get("head")));
    }
}
