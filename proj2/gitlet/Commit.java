package gitlet;

import java.io.IOException;
import java.io.Serializable;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static gitlet.Utils.serialize;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author dhzp
 */
public class Commit implements Serializable {
    /** The timestamp, date and time, of this Commit. */
    private final Instant timeStamp;
    /** A mapping of origin file names to hash names. */
    private final TreeMap<String, String> blobs;
    /** The message of this commit. */
    private final String message;
    /** The default parentID. */
    private final String parentID;
    /** The second parentID. */
    private String secondParentID;
    /** The sha1 of all fields expect sha1 itself. */
    private final String commitHash;
    /** The default parent. */
    private transient Commit parent;
    /** The second parent. */
    private transient Commit secondParent;

    /** Creates a commit node. */
    public Commit(Commit parentCommit, String msg) throws IOException {
        timeStamp = Instant.now();
        blobs = new TreeMap<>(parentCommit.blobs);
        Repository.commitFromStage(this);
        for (String k : Repository.stageRm) {
            blobs.remove(k);
        }
        message = msg;
        parentID = parentCommit.getHash();
        commitHash = calHash();
        Repository.writeCommit(this);
    }

    /** Creates the initial commit node. */
    public Commit() throws IOException {
        timeStamp = LocalDateTime
                .of(1970, 1, 1, 0, 0, 0)
                .toInstant(ZoneOffset.UTC);
        blobs = new TreeMap<>();
        message = "initial commit";
        parentID = null;
        commitHash = calHash();
        Repository.writeCommit(this);
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
                "parentID", serialize(parentID),
                "secondParentID", serialize(secondParentID)
        );
    }

    /** Overwritable add a key-value to the blobs map. */
    public void linkBlob(String key, String value) {
        blobs.put(key, value);
    }

    /** Returns if contains the file. */
    public boolean containFile(String fileName) {
        return blobs.containsKey(fileName);
    }

    /** Returns if contains the file with the hash. */
    public boolean containFile(String fileName, String fileHash) {
        return blobs.containsKey(fileName) && blobs.get(fileName).equals(fileHash);
    }

    /** Returns parent commit. */
    public Commit getParent() {
        if (parent == null) {
            parent = Repository.getCommit(parentID);
        }
        return parent;
    }

    /** Returns parentID. */
    public String getParentID() {
        return parentID;
    }

    /** Display information. */
    public void display() {
        System.out.println("===");
        System.out.println("commit " + commitHash);
        if (secondParentID != null) {
            System.out.println(
                    "Merge: "
                    + parentID.substring(0, 7) + " "
                    + secondParentID.substring(0, 7)
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
    public Set<Map.Entry<String, String>> blobEntrySet() {
        return blobs.entrySet();
    }

}
