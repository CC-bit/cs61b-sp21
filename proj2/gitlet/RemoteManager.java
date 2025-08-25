package gitlet;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeMap;

public class RemoteManager {
    private final Path CWD;
    /** Store remote repo information into a file. */
    private final Path REPOS;
    /** Read information into a map. */
    private TreeMap<String, String> repos;

    RemoteManager(Path gitletPath) {
        REPOS = gitletPath.resolve("repos");
        CWD = gitletPath.getParent();
        load();
    }

    boolean containsRemote(String remoteName) {
        return repos.containsKey(remoteName);
    }

    /** Returns absolute remote repo path. */
    Path getRemoteAbsolutePath(String remoteName) {
        return CWD.resolve(repos.get(remoteName)).getParent().toAbsolutePath().normalize();
    }

    /** Adds remote .gitlet path string to repo map. */
    void addRemote(String name, String path) {
        repos.put(name, path);
    }

    void rmRemote(String name) {
        repos.remove(name);
    }

    void save() {
        Utils.writeObject(REPOS, repos);
    }

    private void load() {
        if (Files.exists(REPOS)) {
            @SuppressWarnings("unchecked")
            TreeMap<String, String> repoTree = Utils.readObject(REPOS, TreeMap.class);
            this.repos = repoTree;
        } else {
            this.repos = new TreeMap<>();
        }
    }

}
