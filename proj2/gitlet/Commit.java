package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.*;
import java.util.TreeMap;

import static gitlet.Utils.*;

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
    private Commit parent;
    /** The second parent. */
    private Commit mergeParent;
    /** The sha1 of all fields expect sha1 itself. */
    private String hash;

    /** Creates a commit node. */
    public Commit(Commit firstParent, String msg) throws IOException {
        timeStamp = Instant.now();
        blobs = new TreeMap<>(firstParent.blobs);
        blobs.putAll(Stage.stageAd);
        for (String k : Stage.stageRm) {
            blobs.remove(k);
        }
        message = msg;
        parent = firstParent;
        hash = calHash();
        saveCommit();
    }

    /** Creates the initial commit node. */
    public Commit() throws IOException {
        message = "initial commit";
        timeStamp = LocalDateTime
                .of(1970, 1, 1, 0, 0, 0)
                .toInstant(ZoneOffset.UTC);
        hash = calHash();
        saveCommit();
    }


    /** Returns the commit instance from file. */
    public static Commit readCommit(String commitHash) {
        File f = join(
                Repository.COMMIT_DIR,
                commitHash.substring(0, 2),
                commitHash.substring(2)
        );
        return readObject(f, Commit.class);
    }

    public String getFileHash(String key) {
        return blobs.get(key);
    }

    public String getHash() {
        return hash;
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

    /** Serialize a commit to COMMIT_DIR. */
    public void saveCommit() throws IOException {
        File subHash = join(Repository.COMMIT_DIR, hash.substring(0, 2));
        subHash.mkdir();
        File h = join(subHash, hash.substring(2));
        Utils.writeObject(h, this);
        // maybe No such file error
    }

    /** Returns if this commit contains a file. */
    public boolean containFile(String fileName) {
        return blobs.containsKey(fileName);
    }
}
