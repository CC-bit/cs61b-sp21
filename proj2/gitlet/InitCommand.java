package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class InitCommand extends AbstractCommand {

    public InitCommand(Repository repo) {
        super(repo);
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
        Files.createDirectory(repo.getStageManager().getStagePath());
        Files.createDirectory(repo.getBlobManager().getBlobPath());
        Files.createDirectory(repo.getCommitManager().getCommitsPath());
        Commit commit = new Commit();
        repo.getCommitManager().writeCommit(commit);
        String initHash = commit.getID();
        branchManager.createBranch("init", initHash);
        branchManager.createBranch("master", initHash);
        branchManager.createBranch("head", "master");
        branchManager.save();
    }
}
