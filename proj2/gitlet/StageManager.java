package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static gitlet.Utils.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class StageManager {
    /** The staging area. */
    private final Path STAGE_DIR;
    private final Path CWD;
    private final Path STAGERM;
    private ArrayList<String> rmArea;

    public StageManager(Path gitletDir) {
        this.STAGE_DIR = gitletDir.resolve("stage");
        this.STAGERM = gitletDir.resolve("rmArea");
        this.CWD = gitletDir.getParent();
        load();
    }

    Path getStagePath() {
        return STAGE_DIR;
    }

    private void load() {
        if (Files.exists(STAGERM)) {
            @SuppressWarnings("unchecked")
            ArrayList<String> rmTree = Utils.readObject(STAGERM, ArrayList.class);
            this.rmArea = rmTree;
        } else {
            this.rmArea = new ArrayList<>();
        }
    }
    public void save() {
        Utils.writeObject(STAGERM, rmArea);
    }

    /** Adds a file from CWD to STAGE_DIR.
     * There should not be a file in STAGE_DIR with same name and version.
     */
    void stageAdd(String fileName) throws IOException {
        Files.copy(CWD.resolve(fileName), STAGE_DIR.resolve(fileName), REPLACE_EXISTING);
        rmArea.remove(fileName);
    }
    void stageRm(String fileName) {
        rmArea.add(fileName);
    }
    Set<String> rmSet() {
        return new HashSet<>(rmArea);
    }

    /** Remove a file from STAGE_DIR. */
    void rmAddedFile(String fileName) throws IOException {
        Path file = STAGE_DIR.resolve(fileName);
        if (Files.exists(file)) {
            Files.delete(file);
        }
    }
    /** Returns if stage area contains a file. */
    boolean isStagedAdd(String file) {
        List<String> allFiles = plainFilenamesIn(STAGE_DIR);
        assert allFiles != null;
        return allFiles.contains(file);
    }
    /** Returns if stage area contains a file with that exact hash. */
    boolean isStagedAdd(String file, String hash) {
        List<String> allFiles = plainFilenamesIn(STAGE_DIR);
        assert allFiles != null;
        return (allFiles.contains(file)
                && hash.equals(sha1((Object) readContents(STAGE_DIR.resolve(file)))));
    }
    boolean isStagedRm(String fileName) {
        return rmArea.contains(fileName);
    }
    /** Returns if stage area is empty. */
    boolean isAddEmpty() {
        return plainFilenamesIn(STAGE_DIR).isEmpty();
    }
    boolean isRmEmpty() {
        return rmArea.isEmpty();
    }
    /** Clear the STAGE_DIR. */
    void clearStageAdd() throws IOException {
        List<String> allFiles = plainFilenamesIn(STAGE_DIR);
        if (allFiles == null) {
            return;
        }
        for (String file : allFiles) {
            rmAddedFile(file);
        }
    }
}
