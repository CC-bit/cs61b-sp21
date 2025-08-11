package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static gitlet.Utils.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Stage {
    /** The staging area. */
    private final Path STAGE_DIR;
    private final Path CWD;
    private final Path RM_AREA;
    ArrayList<String> rmArea;

    public Stage(Path gitletDir) {
        this.STAGE_DIR = gitletDir.resolve("stage");
        this.RM_AREA = gitletDir.resolve("rmArea");
        this.CWD = gitletDir.getParent();
        load();
    }
    Path getStagePath() {
        return STAGE_DIR;
    }

    private void load() {
        if (Files.exists(RM_AREA)) {
            @SuppressWarnings("unchecked")
            ArrayList<String> rmArea = Utils.readObject(RM_AREA, ArrayList.class);
            this.rmArea = rmArea;
        } else {
            this.rmArea = new ArrayList<>();
        }
    }
    public void save() {
        Utils.writeObject(RM_AREA, rmArea);
    }

    /** Adds a file from CWD to STAGE_DIR.
     * There should not be a file in STAGE_DIR with same name and version.
     */
    void addFile(String fileName) throws IOException {
        Files.copy(CWD.resolve(fileName), STAGE_DIR.resolve(fileName), REPLACE_EXISTING);
        rmArea.remove(fileName);
    }

    /** Remove a file from STAGE_DIR. */
    void rmFile(String fileName) throws IOException {
        Path file = STAGE_DIR.resolve(fileName);
        if (Files.exists(file)) {
            Files.delete(file);
        }
    }
    /** Returns if stage area contains a file. */
    boolean containsFile(String file) {
        List<String> allFiles = plainFilenamesIn(STAGE_DIR);
        assert allFiles != null;
        return allFiles.contains(file);
    }
    /** Returns if stage area contains a file with that exact hash. */
    boolean containsFile(String file, String hash) {
        List<String> allFiles = plainFilenamesIn(STAGE_DIR);
        assert allFiles != null;
        return (allFiles.contains(file)
                && hash.equals(sha1((Object) readContents(STAGE_DIR.resolve(file)))));
    }
    /** Returns if stage area is empty. */
    boolean isEmpty() {
        return plainFilenamesIn(STAGE_DIR) == null;
    }
    boolean isRmEmpty() {
        return rmArea.isEmpty();
    }
    /** Clear the STAGE_DIR. */
    void clearStage() throws IOException {
        List<String> allFiles = plainFilenamesIn(STAGE_DIR);
        if (allFiles == null) {
            return;
        }
        for (String file : allFiles) {
            rmFile(file);
        }
    }
}
