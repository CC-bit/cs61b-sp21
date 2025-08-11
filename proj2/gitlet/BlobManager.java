package gitlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class BlobManager {
    /** The blob directory. */
    private final Path BLOB_DIR;

    public BlobManager(Path gitletPath) {
        this.BLOB_DIR = gitletPath.resolve("blobs");
    }

    Path getBlobPath() {
        return BLOB_DIR;
    }

    void writeBlob(Map<Path, String> fileMap) throws IOException {
        for (Map.Entry<Path, String> entry : fileMap.entrySet()) {
            Path sourceFile = entry.getKey();
            String fileHash = entry.getValue();
            Path folder = BLOB_DIR.resolve(fileHash.substring(0, 2));
            Path target = folder.resolve(fileHash.substring(2));
            if (!Files.isDirectory(folder)) {
                Files.createDirectory(folder);
            }
            Files.copy(sourceFile, target);
        }
    }

    /** Read from File hash to String. */
    String readBlobToString(String fileID) throws IOException {
        String fileString;
        if (fileID == null) {
            fileString = "";
        } else {
            fileString = Files.readString(BLOB_DIR.resolve(fileID.substring(0, 2))
                    .resolve(fileID.substring(2)));
        }
        return fileString;
    }

}
