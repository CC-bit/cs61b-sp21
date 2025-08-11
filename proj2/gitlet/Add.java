package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static gitlet.Utils.readContents;

public class Add implements Command{
    private final Repository repo;
    private final Stage stage;
    private final Path CWD;

    public Add(Repository repo) {
        this.repo = repo;
        stage = repo.getStage();
        CWD = repo.getWorkSpace().getCWDPath();
    }

    /** add [file name]
     * Makes a copy of the file in CWD to the STAGE_DIR.
     * It will overwrite the old version in the STAGE_DIR.
     * If the version in CWD is identical to that in current commit,
     * does not stage and removes it from STAGE_DIR(if there be).
     * If the file is staged for rm before add, cancel the stage for rm.
     */
    @Override
    public void execute(String... args) throws IOException {
        // check
        if (args.length != 2) {
            throw new GitletException("Incorrect operands.");
        }
        String fileName = args[1];
        Path filePath = CWD.resolve(fileName);
        if (!Files.exists(filePath)) {
            throw new GitletException("File does not exist.");
        }
        String cwdFileHash = Utils.sha1((Object) readContents(filePath));
        CommitInstance curCommit = repo.getCommit("head");
        if (cwdFileHash.equals(curCommit.getFileHash(fileName))) {
                stage.rmFile(fileName);
                return;
        }
        // execute
        stage.addFile(fileName);
    }
}
