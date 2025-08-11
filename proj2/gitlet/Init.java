package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeMap;

public class Init implements Command{
    private final Repository repo;
    private final TreeMap<String, String> branches;
    public Init(Repository repo) {
        this.repo = repo;
        branches = repo.getBranchMan().branches;
    }

    @Override
    public void execute(String... args) throws IOException {
        if (args.length != 1) {
            throw new GitletException("Incorrect operands.");
        }
        Path gitLetPath = repo.getGitletPath();
        if (Files.exists(gitLetPath)) {
            throw new GitletException(
                    "A Gitlet version-control system already exists in the current directory.");
        }
        Files.createDirectory(gitLetPath);
        Files.createDirectory(repo.getStage().getStagePath());
        Files.createDirectory(repo.getBlob().getBlobPath());
        Files.createDirectory(repo.getCommitMan().getCommitsPath());
        CommitInstance commit = new CommitInstance();
        repo.getCommitMan().writeCommit(commit);
        String initHash = commit.getID();
        branches.put("init", initHash);
        branches.put("master", initHash);
        branches.put("head", "master");
    }
}
