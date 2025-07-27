package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import static gitlet.Utils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author dhzp
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
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
                break;
            case "global-log":
                validateNumArgs(args, 1);
                break;
            case "find":
                validateNumArgs(args, 2);
                break;
            case "status":
                validateNumArgs(args, 1);
            case "checkout":
                break;
            case "branch":
                break;
            case "rm-branch":
                break;
            case "reset":
                break;
            case "merge":
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
     * @param n Number of expected arguments, treat -1 as invalid command name.
     */
    private static void validateNumArgs(String[] args, int n) {
        int l = args.length;
        if (l == 0) {
            System.out.println("Please enter a command.");
        } else if (n == -1) {
            System.out.println("No command with that name exists.");
        } else if (l != n) {
            System.out.println("Incorrect operands.");
        } else {
            return;
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
        if (Stage.STAGEAD.exists()) {
            @SuppressWarnings("unchecked")
            TreeMap<String, String> loadedPointer1 = readObject(Stage.STAGEAD, TreeMap.class);
            Stage.stageAd = loadedPointer1;
        } else {
            Stage.stageAd = new TreeMap<>();
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
        writeObject(Stage.STAGEAD, Stage.stageAd);
        writeObject(Stage.STAGERM, Stage.stageRm);
    }

}
