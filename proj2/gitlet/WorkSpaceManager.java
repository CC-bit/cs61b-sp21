package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static gitlet.Utils.plainFilenamesIn;
import static gitlet.Utils.restrictedDelete;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class WorkSpaceManager {
    private final Path CWD;

    public WorkSpaceManager(Path cwd) {
        this.CWD = cwd;
    }

    Path getCWDPath() {
        return CWD;
    }

    List<String> cwdFileList() {
        // cwd plain file list
        List<String> cwdFiles = plainFilenamesIn(CWD);
        assert cwdFiles != null;
        cwdFiles.remove(".gitlet");
        return cwdFiles;
    }

    /** Overwrite a file to CWD. */
    void writeCWD(Path source, String fileName) throws IOException {
        // get hash from commit, get filePath from hash
        Path target = CWD.resolve(fileName);
        if (!Files.exists(source)) {
            throw new GitletException("Can't find blob file: " + fileName);
        }
        if (Files.isDirectory(target)) {
            throw new GitletException("Target file is a directory.");
        }
        Files.copy(source, target, REPLACE_EXISTING);
    }

    void clearCWD() {
        List<String> cwdFile = plainFilenamesIn(CWD);
        if (cwdFile != null) {
            cwdFile.remove(".gitlet");
            for (String file : cwdFile) {
                restrictedDelete(file);
            }
        }
    }
}
