package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static gitlet.Utils.readObject;

public class GlobalLogCommand extends AbstractCommand {

    public GlobalLogCommand(Repository repo) {
        super(repo);
    }

    @Override
    public void execute(String... args) throws IOException {
        if (args.length != 1) {
            throw new GitletException("Incorrect operands.");
        }
        try (Stream<Path> stream = Files.walk(COMMIT_DIR)) {
            stream.filter(Files::isRegularFile).forEach(cPath -> {
                Commit commit = readObject(cPath.toFile(), Commit.class);
                repo.displayCommit(commit);
                System.out.println();
            });
        }
    }
}
