package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static gitlet.Utils.restrictedDelete;

public class Rm implements Command{
    private final Repository repo;
    private final Stage stage;
    private final Path CWD;

    public Rm(Repository repo) {
        this.repo = repo;
        this.stage = repo.getStage();
        this.CWD = repo.getWorkSpace().getCWDPath();
    }
    /** rm [file name]
     * If the file is staged for add, removes it from staging area.
     * If the file is tracked in current commit, deletes it from CWD
     * and stages it for rm.
     */
    @Override
    public void execute(String... args) throws IOException {
        if (args.length != 2) {
            throw new GitletException("Incorrect operands.");
        }
        String fileName = args[1];
        boolean staged = stage.containsFile(fileName);
        boolean notCommited = repo.getCommit("head").isFileMissing(fileName);
        Path rmFile = CWD.resolve(fileName);
        if (!staged && notCommited) {
            throw new GitletException("No reason to remove the file.");
        }
        stage.rmFile(fileName);
        if (!notCommited && Files.exists(rmFile)) {
            restrictedDelete(fileName);
            stage.rmArea.add(fileName);
        }
    }
}
