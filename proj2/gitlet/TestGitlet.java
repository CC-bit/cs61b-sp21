package gitlet;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
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
            case "add-remote": return new AddRemoteCommand(repo);
            case "rm-remote": return new RmRemoteCommand(repo);
            case "push": return new PushCommand(repo);
            case "fetch": return new FetchCommand(repo);
            case "pull": return new PullCommand(repo);
            default: throw new GitletException("No command with that name exists.");
        }
    }

    private void callMain(Path cwd, String... args) throws IOException {
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
    public void mergeNoConflict() {
        try {
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
        } catch (GitletException | IOException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    @Test
    public void specialMergeCases() throws IOException {
        Path cwd = createCwd("specialMergeCases");
        try {
            setUp2(cwd);
            callMain(cwd, "branch", "b1");
            plus(cwd, "h.txt", "wug2.txt");
            callMain(cwd, "add", "h.txt");
            callMain(cwd, "commit", "Add h.txt");
            callMain(cwd, "branch", "b2");
            callMain(cwd, "rm", "f.txt");
            callMain(cwd, "commit", "remove f.txt");
        } catch (GitletException | IOException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        try {
            callMain(cwd, "merge", "b1");
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            assertEquals("Given branch is an ancestor of the current branch.",
                    e.getMessage());
        }
        callMain(cwd, "checkout", "b2");
        eq(cwd, "f.txt", "wug.txt");
        try {
            callMain(cwd, "merge", "master");
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            assertEquals("Current branch fast-forwarded.",
                    e.getMessage());
        }
        star(cwd, "f.txt");
    }

    private Path bigC(Path cwd, String dir) throws IOException {
        Path subDir = cwd.resolve(dir);
        if (!Files.isDirectory(subDir)) {
            Files.createDirectories(subDir);
        }
        return subDir;
    }

    private String captureLog(Path repoPath, int commitNum) throws IOException {
        // set capture
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos, true, StandardCharsets.UTF_8));
        // call "log"
        callMain(repoPath, "log");
        // process captured information
        System.setOut(originalOut);
        String capturedLog = baos.toString(StandardCharsets.UTF_8);

        String hash = "";
        int i = 1;
        // process information line by line
        String[] lines = capturedLog.split(System.lineSeparator());
        for (String line : lines) {
            if (line.contains("commit")) {
                if (i == commitNum) {
                    int index = line.indexOf(" ");
                    hash = line.substring(index + 1);
                }
                i++;
            }
        }
        return hash;
    }

    @Test
    public void remoteFetchPush() throws IOException {
        Path cwd = createCwd("remoteFetchPush");
        Path d1 = bigC(cwd, "D1");
        setUp2(d1);

        System.out.println("1st log:");
        callMain(d1, "log");
        /* 1st log
        ${COMMIT_HEAD}
        Two files

        ===
        ${COMMIT_HEAD}
        initial commit

        */
        String R1_TWO = captureLog(d1, 1); // sha1 of "two"
        String R1_INIT = captureLog(d1, 2); // sha1 of "init"

        Path d2 = bigC(cwd, "D2");
        callMain(d2, "init");
        plus(d2, "k.txt", "wug2.txt");
        callMain(d2, "add", "k.txt");
        callMain(d2, "commit", "Add k in repo 2");

        System.out.println("2nd log:");
        callMain(d2, "log");
        /* 2nd log
        ===
        ${COMMIT_HEAD}
        Add k in repo 2

        ===
        ${COMMIT_HEAD}
        initial commit

         */
        String R2_K = captureLog(d2, 1); // sha1 of "k"
        String R2_INIT = captureLog(d2, 2); // sha1 of "init"

        callMain(d2, "add-remote", "R1", "../D1/.gitlet");
        callMain(d2, "fetch", "R1", "master");
        callMain(d2, "checkout", "R1/master");

        System.out.println("3rd log:");
        callMain(d2, "log");
        /* 3rd log
        ===
        commit ${R1_TWO}
        ${DATE}
        Two files

        ===
        commit ${R1_INIT}
        ${DATE}
        initial commit

         */
        callMain(d2, "checkout", "master");
        callMain(d2, "reset", R1_TWO);
        plus(d2, "h.txt", "wug3.txt");
        callMain(d2, "add", "h.txt");
        callMain(d2, "commit", "Add h");

        System.out.println("4th log:");
        callMain(d2, "log");
        /* 4th log
        ===
        ${COMMIT_HEAD}
        Add h

        ===
        commit ${R1_TWO}
        ${DATE}
        Two files

        ===
        commit ${R1_INIT}
        ${DATE}
        initial commit
         */
        String R2_H = captureLog(d2, 1); // sha1 of "Add h"
        callMain(d2, "push", "R1", "master");
        // cd to D1

        System.out.println("last log:");
        callMain(d1, "log");
        /* last log
        ===
        commit ${R2_H}
        ${DATE}
        Add h

        ===
        commit ${R1_TWO}
        ${DATE}
        Two files

        ===
        commit ${R1_INIT}
        ${DATE}
        initial commit

         */
    }
}
