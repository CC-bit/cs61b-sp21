package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static gitlet.Utils.readObject;

public class GlobalLog implements Command{
    private final Path COMMIT_DIR;
    private final CommitMan commitMan;

    public GlobalLog(Repository repo) {
        COMMIT_DIR = repo.getCommitMan().getCommitsPath();
        commitMan = repo.getCommitMan();
    }

    @Override
    public void execute(String... args) throws IOException {
        if (args.length != 1) {
            throw new GitletException("Incorrect operands.");
        }
        try (Stream<Path> stream = Files.walk(COMMIT_DIR)) {
            stream.filter(Files::isRegularFile).forEach(cPath -> {
                CommitInstance commit = readObject(cPath.toFile(), CommitInstance.class);
                commitMan.display(commit);
            });
        }
    }
}
