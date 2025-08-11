package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static gitlet.Utils.readObject;

public class FindCommand extends AbstractCommand {

    public FindCommand(Repository repo) {
        super(repo);
    }

    @Override
    public void execute(String... args) throws IOException {
        if (args.length != 2) {
            throw new GitletException("Incorrect operands.");
        }
        String cMsg = args[1];
        AtomicInteger msgNum = new AtomicInteger(0);
        try (Stream<Path> stream = Files.walk(COMMIT_DIR)) {
            stream.filter(Files::isRegularFile).forEach(cPath -> {
                Commit commit = readObject(cPath.toFile(), Commit.class);
                if (cMsg.equals(commit.getMessage())) {
                    msgNum.incrementAndGet();
                    System.out.println(commit.getID());
                }
            });
        }
        if (msgNum.get() == 0) {
            throw new GitletException("Found no commit with that message.");
        }
    }
}
