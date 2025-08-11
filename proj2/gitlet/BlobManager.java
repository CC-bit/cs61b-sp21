package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;

import static gitlet.Utils.*;

public class BlobManager {

    /** The blob directory. */
    private final Path BLOB_DIR;
    private final Path STAGE_DIR;

    public BlobManager(Path gitletPath) {
        this.BLOB_DIR = gitletPath.resolve("blobs");
        this.STAGE_DIR = gitletPath.resolve("stage");
    }
    Path getBlobPath() {
        return BLOB_DIR;
    }

    /** Serialize all files from stage area into blob folder.
     * Adds them to the newCommit blobs. */
    public TreeMap<String, String> writeBlob() throws IOException {
        TreeMap<String, String> stagedFileTree = new TreeMap<>();
        List<String> stagedFiles = plainFilenamesIn(STAGE_DIR);
        for (String fileName : stagedFiles) {
            Path source = STAGE_DIR.resolve(fileName);
            String fileHash = sha1((Object) readContents(source));
            Path folder = BLOB_DIR.resolve(fileHash.substring(0, 2));
            Path target = folder.resolve(fileHash.substring(2));
            if (!Files.isDirectory(folder)) {
                Files.createDirectory(folder);
            }
            Files.copy(source, target);
            stagedFileTree.put(fileName, fileHash);
        }
        return stagedFileTree;
    }

    /** Read from File hash to String. */
    String readBlob(String fileID) throws IOException {
        String file;
        if (fileID == null) {
            file = "";
        } else {
            file = Files.readString(BLOB_DIR.resolve(fileID.substring(0, 2))
                    .resolve(fileID.substring(2)));
        }
        return file;
    }

}
