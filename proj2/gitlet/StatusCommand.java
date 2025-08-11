package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static gitlet.Utils.*;

public class StatusCommand extends AbstractCommand {

    public StatusCommand(Repository repo) {
        super(repo);
    }

    @Override
    public void execute(String... args) throws IOException {
        if (args.length != 1) {
            throw new GitletException("Incorrect operands.");
        }
        List<String> stgFiles = plainFilenamesIn(STAGE_DIR);
        assert stgFiles != null;
        System.out.println("=== Branches ===");
        String curBranchName = branchManager.getCurBranchName();
        System.out.println("*" + curBranchName);
        for (Map.Entry<String, String> entry : branchManager.entrySet()) {
            String key = entry.getKey();
            if (key.equals("head") || key.equals("init") || key.equals(curBranchName)) {
                continue;
            }
            System.out.println(key);
        }
        System.out.println("\n=== Staged Files ===");
        for (String file : stgFiles) {
            System.out.println(file);
        }
        System.out.println("\n=== Removed Files ===");
        for (String file : stageManager.rmSet()) {
            System.out.println(file);
        }
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        Commit currentCommit = repo.getCommit("head");
        for (Map.Entry<String, String> entry : currentCommit.blobEntrySet()) {
            String file = entry.getKey();
            Path cwd = CWD.resolve(file);
            if (Files.exists(cwd)) {
                String cwdHash = sha1((Object) readContents(cwd));
                if (!entry.getValue().equals(cwdHash) && !stageManager.isStagedAdd(file, cwdHash)) {
                    System.out.println(file + " (modified)");
                }
            } else if (stageManager.isStagedRm(file)) {
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
            if (currentCommit.isFileMissing(file) && !stageManager.isStagedAdd(file)) {
                System.out.println(file);
            }
        }
    }
}
