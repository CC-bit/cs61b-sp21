package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author dhzp
 */
public class Main {

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                throw new GitletException("Please enter a command.");
            }
            String commandName = args[0];
            Repository repo = new Repository(Path.of(System.getProperty("user.dir")));
            Command command = getCommand(commandName, repo);
            if (!commandName.equals("init") && !Files.exists(Paths.get(".gitlet"))) {
                throw new GitletException("Not in an initialized Gitlet directory.");
            }
            command.execute(args);
            save(repo);
        } catch (GitletException | IOException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    private static Command getCommand(String name, Repository repo) {
        switch (name) {
            case "init": return new Init(repo);
            case "add": return new Add(repo);
            case "commit": return new Commit(repo);
            case "rm": return new Rm(repo);
            case "log": return new Log(repo);
            case "global-log": return new GlobalLog(repo);
            case "find": return new Find(repo);
            case "status": return new Status(repo);
            case "checkout": return new CheckOut(repo);
            case "branch": return new Branch(repo);
            case "rm-branch": return new RmBranch(repo);
            case "reset": return new Reset(repo);
            case "merge": return new Merge(repo);
            default: throw new GitletException("No command with that name exists.");
        }
    }

    /** Save app state to file. */
    private static void save(Repository repo) {
        repo.getBranchMan().save();
        repo.getStage().save();
    }
}
