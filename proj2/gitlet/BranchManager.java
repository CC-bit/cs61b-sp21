package gitlet;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class BranchManager {
    /** The branches file. */
    private final Path BRANCHES;
    /** A map of branches names like "master" to commit hash. */
    TreeMap<String, String> branches;

    public BranchManager(Path gitletPath) {
        this.BRANCHES = gitletPath.resolve("branches");
        load();
    }

    boolean containsBranch(String branchName) {
        return branches.containsKey(branchName);
    }
    void createBranch(String branchName, String cid) {
        branches.put(branchName, cid);
    }
    void deleteBranch(String branchName) {
        branches.remove(branchName);
    }
    String getCurBranchName() {
        return branches.get("head");
    }
    Set<Map.Entry<String, String>> entrySet() {
        return branches.entrySet();
    }

    String getBrCommitID(String branchName) {
        if (branchName.equals("head")) {
            branchName = branches.get(branchName);
        }
        return branches.get(branchName);
    }


    private void load() {
        if (Files.exists(BRANCHES)) {
            @SuppressWarnings("unchecked")
            TreeMap<String, String> brTree = Utils.readObject(BRANCHES, TreeMap.class);
            this.branches = brTree;
        } else {
            this.branches = new TreeMap<>();
        }
    }
    public void save() {
        Utils.writeObject(BRANCHES, branches);
    }
}
