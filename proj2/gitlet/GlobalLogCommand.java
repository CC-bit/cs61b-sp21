package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

        List<Commit> commits = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(COMMIT_DIR)) {
            stream.filter(Files::isRegularFile).forEach(cPath -> {
                commits.add(readObject(cPath.toFile(), Commit.class));
            });
        }

        commits.sort(Comparator.comparing(Commit::getTime).reversed());

        for (Commit commit : commits) {
            repo.displayCommit(commit);
            System.out.println();
        }
    }
}
