package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static gitlet.Utils.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author dhzp
 */
public class Commit implements Serializable {
    /** The timestamp, date and time, of this Commit. */
    private Instant timeStamp;
    /** A mapping of origin file names to hash names. */
    private TreeMap<String, String> blobs;
    /** The message of this commit. */
    private String message;
    /** The default parent. */
    private String parent;
    /** The second parent. */
    private String mergeParent;
    /** The sha1 of all fields expect sha1 itself. */
    private String commitHash;

    /** Creates a commit node. */
    public Commit(String parentCid, String msg) throws IOException {
        Commit parentCommit = Commit.readCommit(parentCid);
        timeStamp = Instant.now();
        blobs = new TreeMap<>(parentCommit.blobs);
        saveBlobs(this);
        for (String k : Stage.stageRm) {
            blobs.remove(k);
        }
        message = msg;
        parent = parentCid;
        commitHash = calHash();
        saveCommit();
    }

    /** Creates the initial commit node. */
    public Commit() throws IOException {
        timeStamp = LocalDateTime
                .of(1970, 1, 1, 0, 0, 0)
                .toInstant(ZoneOffset.UTC);
        blobs = new TreeMap<>();
        message = "initial commit";
        commitHash = calHash();
        saveCommit();
    }


    /** Returns the commit instance according to hash. */
    public static Commit readCommit(String commitHash) {
        File folder = join(Repository.COMMIT_DIR, commitHash.substring(0, 2));
        String restCommit = commitHash.substring(2);
        int sameID = 0;
        String commitFile = "";
        if (folder.exists()) {
            List<String> allFiles = plainFilenamesIn(folder);
            assert allFiles != null;
            for (String file : allFiles) {
                if (file.startsWith(restCommit)) {
                    commitFile = file;
                    sameID++;
                }
            }
        }
        if (sameID == 0) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        } else if (sameID > 1) {
            System.out.println("Ambiguous commit id.");
            System.exit(0);
        }
        return readObject(join(folder, commitFile), Commit.class);
    }

    /** Returns the blob file hash. */
    public String getFileHash(String key) {
        return blobs.get(key);
    }

    public String getHash() {
        return commitHash;
    }

    /** Returns sha1 according to other instance fields. */
    private String calHash() {
        return Utils.sha1(
                "timeStamp", serialize(timeStamp),
                "blobs", serialize(blobs),
                "message", serialize(message),
                "parent", serialize(parent),
                "mergeParent", serialize(mergeParent)
        );
    }

    /** Overwritable add a key-value to the blobs map. */
    public void put(String key, String value) {
        blobs.put(key, value);
    }

    /** Serialize a commit into COMMIT_DIR. */
    public void saveCommit() throws IOException {
        File subHash = join(Repository.COMMIT_DIR, commitHash.substring(0, 2));
        subHash.mkdir();
        Utils.writeObject(join(subHash, commitHash.substring(2)), this);
    }

    /** Returns if contains the file. */
    public boolean containFile(String fileName) {
        return blobs.containsKey(fileName);
    }
    /** Returns if contains the file with the hash. */
    public boolean containFile(String fileName, String fileHash) {
        return blobs.containsKey(fileName) && blobs.get(fileName).equals(fileHash);
    }

    /** Serialize all files in stage area into blob folder.
     * Adds them to the newCommit blobs. */
    public static void saveBlobs(Commit newCommit) {
        List<String> allFiles = plainFilenamesIn(Repository.STAGE_DIR);
        assert allFiles != null;
        if (allFiles.isEmpty()) {
            return;
        }
        for (String file : allFiles) {
            byte[] fileContent = readContents(join(Repository.STAGE_DIR, file));
            String hash = sha1((Object) fileContent);
            newCommit.put(file, hash);
            File subFolder = join(Repository.BLOB_DIR, hash.substring(0, 2));
            subFolder.mkdir();
            writeContents(join(subFolder, hash.substring(2)), (Object) fileContent);
        }
    }

    /** Overwrite a file from commit to CWD. */
    public void writeCWD(String fileName) throws IOException {
        String fileHash = blobs.get(fileName);
        Path filePath = Path.of(".gitlet", "blob",
                fileHash.substring(0,2), fileHash.substring(2));
        Files.copy(filePath, Path.of(fileName), REPLACE_EXISTING);
    }

    /** Delete all files in CWD.
     * Overwrite all files from commit to CWD. */
    public void writeCWD() throws IOException {
        List<String> cwdFile = plainFilenamesIn(Repository.CWD);
        if (cwdFile != null) {
            cwdFile.remove(".gitlet");
            for (String file : cwdFile) {
                restrictedDelete(file);
            }
        }
        for (String file : blobs.keySet()) {
            writeCWD(file);
        }
    }

    /** Returns parent commit hash. */
    public String getParent() {
        return parent;
    }

    /** Display information. */
    public void display() {
        System.out.println("===");
        System.out.println("commit " + commitHash);
        if (mergeParent != null) {
            System.out.println(
                    "Merge: "
                    + parent.substring(0, 7) + " "
                    + mergeParent.substring(0, 7)
            );
        }
        ZonedDateTime zonedDateTime = timeStamp.atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E MMM d HH:mm:ss yyyy Z");
        System.out.println("Date: " + zonedDateTime.format(formatter));
        System.out.println(message + "\n");
    }

    /** Returns the commit message. */
    public String getMessage() {
        return message;
    }

    /** Returns entry set of blobs. */
    public Set<Map.Entry<String, String>> entrySet() {
        return blobs.entrySet();
    }

}
