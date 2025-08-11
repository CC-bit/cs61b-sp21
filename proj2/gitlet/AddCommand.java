package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static gitlet.Utils.readContents;

public class AddCommand extends AbstractCommand {

    public AddCommand(Repository repo) {
        super(repo);
    }

    /** add [file name]
     * Makes a copy of the file in CWD to the STAGE_DIR.
     * It will overwrite the old version in the STAGE_DIR.
     * If the version in CWD is identical to that in current commit,
     * does not stageManager and removes it from STAGE_DIR(if there be).
     * If the file is staged for rm before add, cancel the stageManager for rm.
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
        Commit curCommit = repo.getCommit("head");
        if (cwdFileHash.equals(curCommit.getFileHash(fileName))) {
            stageManager.rmAddedFile(fileName);
            return;
        }
        // execute
        stageManager.stageAdd(fileName);
    }
}
