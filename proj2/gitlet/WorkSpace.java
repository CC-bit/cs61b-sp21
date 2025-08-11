package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static gitlet.Utils.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class WorkSpace {
    private final Path CWD;
    private final Path BLOB_DIR;

    public WorkSpace(Path cwd) {
        this.CWD = cwd;
        this.BLOB_DIR = CWD.resolve(".gitlet").resolve("blobs");
    }

    Path getCWDPath() {
        return CWD;
    }

    /** Check if CWD file tracked. */
    void trackCheck(CommitInstance cur, CommitInstance branch) {
        List<String> cwdFiles = plainFilenamesIn(CWD);
        assert cwdFiles != null;
        cwdFiles.remove(".gitlet");
        for (String file : cwdFiles) {
            String hash = sha1((Object) readContents(CWD.resolve(file)));
            if (cur.isFileMissing(file, hash) && branch.isFileMissing(file, hash)) {
                throw new GitletException("There is an untracked file in the way;" +
                        " delete it, or add and commit it first.");
            }
        }
    }

    /** Overwrite a file from commit to CWD. */
    public void writeCWD(CommitInstance commit, String fileName) throws IOException {
        String fileHash = commit.getFileHash(fileName);
        Path source = BLOB_DIR.resolve(fileHash.substring(0,2))
                .resolve(fileHash.substring(2));
        Path target = CWD.resolve(fileName);
        if (!Files.exists(source)) {
            throw new GitletException("Can't find blob file with hash: " + fileHash);
        }
        if (Files.isDirectory(target)) {
            throw new GitletException("Target file is a directory.");
        }
        Files.copy(source, target, REPLACE_EXISTING);
    }

    /** Delete all files in CWD.
     * Overwrite all files from commit to CWD. */
    public void writeCWD(CommitInstance c) throws IOException {
        List<String> cwdFile = plainFilenamesIn(CWD);
        if (cwdFile != null) {
            cwdFile.remove(".gitlet");
            for (String file : cwdFile) {
                restrictedDelete(file);
            }
        }
        for (Map.Entry<String, String> entry : c.blobEntrySet()) {
            writeCWD(c, entry.getKey());
        }
    }
}
