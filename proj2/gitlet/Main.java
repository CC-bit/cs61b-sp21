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

    /** Save app state to file. */
    private static void save(Repository repo) {
        repo.getBranchManager().save();
        repo.getStageManager().save();
    }
}
