package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static gitlet.Utils.*;

public class Status implements Command{
    private final Repository repo;
    TreeMap<String, String> branches;
    private final Stage stage;
    private final Path STAGE_DIR;
    private final Path CWD;

    public Status(Repository repo) {
        this.repo = repo;
        branches = repo.getBranchMan().branches;
        stage = repo.getStage();
        CWD = repo.getWorkSpace().getCWDPath();
        STAGE_DIR = stage.getStagePath();
    }

    @Override
    public void execute(String... args) throws IOException {
        if (args.length != 1) {
            throw new GitletException("Incorrect operands.");
        }
        List<String> stgFiles = plainFilenamesIn(stage.getStagePath());
        assert stgFiles != null;
        System.out.println("=== Branches ===");
        System.out.println("*" + branches.get(branches.get("head")));
        for (Map.Entry<String, String> entry : branches.entrySet()) {
            if (entry.getKey().equals("head")) continue;
            System.out.println(entry.getValue());
        }
        System.out.println("\n=== Staged Files ===");
        for (String file : stgFiles) {
            System.out.println(file);
        }
        System.out.println("\nRemoved Files");
        for (String file : stage.rmArea) {
            System.out.println(file);
        }
        System.out.println("\n=== Modifications Not Staged For CommitInstance ===");
        CommitInstance currentCommit = repo.getCommit("head");
        for (Map.Entry<String, String> entry : currentCommit.blobEntrySet()) {
            String file = entry.getKey();
            Path cwd = CWD.resolve(file);
            if (Files.exists(cwd)) {
                String cwdHash = sha1((Object) readContents(cwd));
                if (!entry.getValue().equals(cwdHash) && !stage.containsFile(file, cwdHash)) {
                    System.out.println(file + " (modified)");
                }
            } else if (stage.rmArea.contains(file)) {
                System.out.println(file + " (deleted)");
            }
        }
        for (String file : stgFiles) {
            String stgHash = sha1((Object) readContents(STAGE_DIR.resolve(file)));
            Path cwd = CWD.resolve(file);
            if (Files.exists(cwd)) {
                String cwdHash = sha1((Object) readContents(cwd));
                if (!stgHash.equals(cwdHash)) {
                    System.out.println(file + " (modified)");
                }
            } else {
                System.out.println(file + " (deleted)");
            }
        }
        System.out.println("\n=== Untracked Files ===");
        List<String> cwdFiles = plainFilenamesIn(CWD);
        assert cwdFiles != null;
        cwdFiles.remove(".gitlet");
        for (String file : cwdFiles) {
            if (currentCommit.isFileMissing(file) && !stage.containsFile(file)) {
                System.out.println(file);
            }
        }
    }
}
