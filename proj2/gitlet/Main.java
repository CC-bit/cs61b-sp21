package gitlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import static gitlet.Utils.readObject;
import static gitlet.Utils.writeObject;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author dhzp
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) throws IOException {
        int l = args.length;
        if (l == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        loadState();
        String firstArg = args[0];
        if (firstArg.equals("init")) {
            validateNumArgs(args, 1);
            Repository.init();
            saveState();
            return;
        }
        if (!Repository.GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        switch(firstArg) {
            case "add":
                validateNumArgs(args, 2);
                Repository.add(args[1]);
                break;
            case "commit":
                validateNumArgs(args, 2);
                Repository.commit(args[1]);
                break;
            case "rm":
                validateNumArgs(args, 2);
                Repository.rm(args[1]);
                break;
            case "log":
                validateNumArgs(args, 1);
                Repository.log();
                break;
            case "global-log":
                validateNumArgs(args, 1);
                Repository.globalLog();
                break;
            case "find":
                validateNumArgs(args, 2);
                Repository.find(args[1]);
                break;
            case "status":
                validateNumArgs(args, 1);
                Repository.status();
            case "checkout":
                validateNumArgs(args, 2, 3, 4);
                if (l == 2) {
                    Repository.checkout(args[1]);
                } else if (l == 3) {
                    Repository.checkout("--", args[2]);
                } else {
                    Repository.checkout(args[1], "--", args[3]);
                }
                break;
            case "branch":
                validateNumArgs(args, 2);
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                validateNumArgs(args, 2);
                Repository.rmBranch(args[1]);
                break;
            case "reset":
                validateNumArgs(args, 2);
                Repository.reset(args[1]);
                break;
            case "merge":
                validateNumArgs(args, 2);
                Repository.merge(args[1]);
                break;
            default:
                validateNumArgs(args, -1);
                break;
        }
        saveState();
    }

    /**
     * Checks the number and format of arguments versus the expected number and format,
     * print a message if they do not match.
     *
     * @param args Argument array from command line
     * @param numbs Number of expected arguments, treat -1 as invalid command name.
     */
    private static void validateNumArgs(String[] args, int... numbs) {
        int l = args.length;
        if (l == 0) {
            System.out.println("Please enter a command.");
        } else if (numbs[0] == -1) {
            System.out.println("No command with that name exists.");
        } else {
            for (int n : numbs) {
                if (l == n) {
                    return;
                }
            }
            System.out.println("Incorrect operands.");
        }
        System.exit(0);
    }

    /** Load Repo and Stage state from file. */
    private static void loadState() {
        if (Repository.POINTER.exists()) {
            @SuppressWarnings("unchecked")
            TreeMap<String, String> loadedPointer0 = readObject(Repository.POINTER, TreeMap.class);
            Repository.pointer = loadedPointer0;
        } else {
            Repository.pointer = new TreeMap<>();
        }
        if (Stage.STAGERM.exists()) {
            @SuppressWarnings("unchecked")
            ArrayList<String> loadedPointer2 = readObject(Stage.STAGERM, ArrayList.class);
            Stage.stageRm = loadedPointer2;
        } else {
            Stage.stageRm= new ArrayList<>();
        }
    }

    /** Save app state to file. */
    private static void saveState() {
        writeObject(Repository.POINTER, Repository.pointer);
        writeObject(Stage.STAGERM, Stage.stageRm);
    }
}
