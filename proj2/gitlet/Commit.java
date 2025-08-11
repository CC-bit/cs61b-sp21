package gitlet;

import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static gitlet.Utils.serialize;

/** Represents a gitlet commit object.
 *  It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author dhzp
 */
public class Commit implements Serializable {
    /** A mapping of origin file names to hash names. */
    private final TreeMap<String, String> blobs;
    /** The timestamp, date and time, of this Commit. */
    private final Instant timeStamp;
    /** The message of this commit. */
    private final String message;
    /** The default parentID. */
    private final String parentID;
    /** The second parentID. */
    private String secondParentID;
    /** The sha1 of all fields expect sha1 itself. */
    private String commitHash;

    /** Creates the initial commit node. */
    public Commit() {
        timeStamp = LocalDateTime
                .of(1970, 1, 1, 0, 0, 0)
                .toInstant(ZoneOffset.UTC);
        blobs = new TreeMap<>();
        message = "initial commit";
        parentID = null;
        commitHash = calHash();
    }
    /** Creates a commit node. */
    public Commit(Commit parent,
                  String msg, TreeMap<String, String> blobs) throws IOException {
        this.blobs = blobs;
        timeStamp = Instant.now();
        message = msg;
        parentID = parent.getID();
        commitHash = calHash();
    }
    /** Creates a commit node. */
    public Commit(Commit parent, Commit secParent,
                  String msg, TreeMap<String, String> blobs) throws IOException {
        this(parent, msg, blobs);
        secondParentID = secParent.getID();
        commitHash = calHash();
    }

    public String getID() {
        return commitHash;
    }
    public Instant getTime() {
        return timeStamp;
    }
    public String getMessage() {
        return message;
    }
    String getParentID() {
        return parentID;
    }
    String getSecondParentID() {
        return secondParentID;
    }

    TreeMap<String, String> getBlobs() {
        // Shallow copy from String, safe.
        return new TreeMap<>(blobs);
    }

    /** Returns entry set of blobs. */
    Set<Map.Entry<String, String>> blobEntrySet() {
        return blobs.entrySet();
    }
    /** Returns if contains the file. */
    boolean isFileMissing(String fileName) {
        return !blobs.containsKey(fileName);
    }
    /** Returns the blob file hash. */
    String getFileHash(String key) {
        return blobs.get(key);
    }

    /** Returns true if the commit contains the file with the hash.
     * Returns false if not contain file or contain with different hash. */
    boolean isFileMissing(String fileName, String fileHash) {
        return !blobs.containsKey(fileName) || !blobs.get(fileName).equals(fileHash);
    }

    /** Returns sha1 according to other instance fields. */
    private String calHash() {
        return Utils.sha1(
                "timeStamp", serialize(timeStamp),
                "blobs", serialize(blobs),
                "message", serialize(message),
                "parentID", serialize(parentID),
                "secondParentID", serialize(secondParentID)
        );
    }
}
