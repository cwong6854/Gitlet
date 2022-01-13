package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;

/** Gitlet Commit class, where class holds log messages, timestamp,
 * trackedfiles, reference to parent commits, and the ID of the
 * commit.
 * @author Curtis Wong
 */
public class Commit implements Serializable {

    /** Commit Directory. */
    static final File COMMIT_DIR = Utils.join(Repo.GITLET_FOLDER, "commits");
    /** Commit message. */
    private String _message;
    /** Timestamp that the commit was created. */
    private String _timestamp;
    /** Trackedfiles represented as Hashmap, to hold filename and
     * contents that the file holds. */
    private HashMap<String, Blobs> _trackedFiles;
    /** Reference to the parent commit. */
    private String _parent;
    /** Commit ID. */
    private String _id;

    /** Commit constructor holding message, parent and trackedfiles.
     * @param message message.
     * @param parent parent reference.
     * @param tracker trackedfiles.
     */
    public Commit(String message, String parent, HashMap<String,
            Blobs> tracker) {
        String currtime = new SimpleDateFormat(
                "EEE MMM dd HH:mm:ss yyyy Z").format(new Date());
        _message = message;
        _parent = parent;
        _timestamp = currtime;
        _trackedFiles = tracker;
        String id = Utils.sha1(message, currtime);
        _id = id;

    }

    /** @return Getter method to get the Commit's message. */
    public String getMessage() {
        return this._message;
    }

    /** @return Getter method to get the timestamp creation of Commit. */
    public String getTimeStamp() {
        return _timestamp;
    }

    /** @return Getter method to get the Comnmit's parent ID. */
    public String getParent() {
        return this._parent;
    }

    /** @return Getter method to get the Commit's ID. */
    public String getID() {
        return this._id;
    }

    /** Getter method to get the Commit's trackedfiles.
     * @return getTrackedFiles. */
    public HashMap<String, Blobs> getTrackedFiles() {
        return this._trackedFiles;
    }

    /** @param rmblob Helper method to remove specific trackedfile in Commit. */
    public void removeTrackedFile(Blobs rmblob) {
        _trackedFiles.remove(rmblob.getfileName());
    }

    /** Helper method to clear trackedfiles. */
    public void clearTrackedFile() {
        _trackedFiles = null;
    }

    /** @param set Helper method to set trackedfiles. */
    public void setTrackedFiles(HashMap<String, Blobs> set) {
        _trackedFiles = set;
    }

    /** Helper method to set the ID of the Commit. */
    public void setID() {
        _id = Utils.sha1(_message, _timestamp);
    }

    /** @param timestamp Helper method to set the timestamp of Commit. */
    public void setTimestamp(String timestamp) {
        _timestamp = timestamp;
    }

}
