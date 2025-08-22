package gitlet;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.*;

public class TestGitlet {
    /** The testing dir(./testing/) */
    private final Path TEST_DIR = Path.of(System.getProperty("user.dir")).resolve("testing");

    private static Command getCommand(String name, Repository repo) {
        switch (name) {
            case "init": return new InitCommand(repo);
            case "add": return new AddCommand(repo);
            case "commit": return new CommitCommand(repo);
            case "rm": return new RmCommand(repo);
            case "log": return new LogCommand(repo);
            case "global-log": return new GlobalLogCommand(repo);
            case "find": return new FindCommand(repo);
            case "status": return new StatusCommand(repo);
            case "checkout": return new CheckOutCommand(repo);
            case "branch": return new BranchCommand(repo);
            case "rm-branch": return new RmBranchCommand(repo);
            case "reset": return new ResetCommand(repo);
            case "merge": return new MergeCommand(repo);
            default: throw new GitletException("No command with that name exists.");
        }
    }

    private void callMain(Path cwd, String... args) {
        try {
            if (args.length == 0) {
                throw new GitletException("Please enter a command.");
            }
            String commandName = args[0];
            Repository repo = new Repository(cwd);
            Command command = getCommand(commandName, repo);
            if (!commandName.equals("init") && !Files.exists(cwd.resolve(".gitlet"))) {
                throw new GitletException("Not in an initialized Gitlet directory.");
            }
            command.execute(args);
        } catch (GitletException | IOException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    private void plus(Path cwd, String tar, String src) throws IOException {
        Path target = cwd.resolve(tar);
        Path source = TEST_DIR.resolve("src").resolve(src);
        Files.copy(source, target, REPLACE_EXISTING);
    }

    private void star(Path cwd, String fileName) {
        Path file = cwd.resolve(fileName);
        assertFalse(Files.exists(file));
    }

    private void eq(Path cwd, String cwdFile, String srcFile) {
        Path cFile = cwd.resolve(cwdFile);
        Path rFile = TEST_DIR.resolve("src").resolve(srcFile);
        String cwdHash = Utils.sha1(Utils.readContents(cFile));
        String srcHash = Utils.sha1(Utils.readContents(rFile));
        assertEquals(cwdHash, srcHash);
    }

    private void setUp2(Path cwd) throws IOException {
        callMain(cwd, "init");
        plus(cwd, "f.txt", "wug.txt");
        plus(cwd, "g.txt", "notwug.txt");
        callMain(cwd, "add", "g.txt");
        callMain(cwd, "add", "f.txt");
        callMain(cwd, "commit", "Two files");
    }

    private Path createCwd(String dir) throws IOException {
        int i = 0;
        Path cwd = TEST_DIR.resolve(dir + "_" + i);
        while (Files.exists(cwd)) {
            i++;
            cwd = TEST_DIR.resolve(dir + "_" + i);
        }
        Files.createDirectory(cwd);
        return cwd;
    }

    @Test
    public void mergeNoConflict() throws IOException {
        Path cwd = createCwd("mergeNoConflict");
        setUp2(cwd);
        callMain(cwd, "branch", "other");
        plus(cwd, "h.txt", "wug2.txt");
        callMain(cwd, "add", "h.txt");
        callMain(cwd, "rm", "g.txt");
        callMain(cwd, "commit", "Add h.txt and remove g.txt");
        callMain(cwd, "checkout", "other");
        callMain(cwd, "rm", "f.txt");
        plus(cwd, "k.txt", "wug3.txt");
        callMain(cwd, "add", "k.txt");
        callMain(cwd, "commit", "Add k.txt and remove f.txt");
        callMain(cwd, "checkout", "master");
        callMain(cwd, "merge", "other");
        star(cwd, "f.txt");
        star(cwd, "g.txt");
        eq(cwd, "h.txt", "wug2.txt");
        eq(cwd, "k.txt", "wug3.txt");
        callMain(cwd, "log");
        callMain(cwd, "status"); // should be blank status
    }
}
