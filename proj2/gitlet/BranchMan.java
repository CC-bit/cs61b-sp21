package gitlet;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeMap;

public class BranchMan {
    /** The branches file. */
    private final Path branchesFile;
    /** A map of branches names like "master" to commit hash. */
    TreeMap<String, String> branches;

    public BranchMan(Path gitletPath) {
        this.branchesFile = gitletPath.resolve("branches");
        load();
    }

    private void load() {
        if (Files.exists(branchesFile)) {
            @SuppressWarnings("unchecked")
            TreeMap<String, String> branches = Utils.readObject(branchesFile, TreeMap.class);
            this.branches = branches;
        } else {
            this.branches = new TreeMap<>();
        }
    }
    public void save() {
        Utils.writeObject(branchesFile, branches);
    }
}
