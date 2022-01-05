package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/** Staging Area that holds the stage for addition
 * and stage for removal files.
 * @author Curtis Wong
 */

public class StagingArea implements Serializable {
    /** Folder that StagingArea lives in. */
    static final File STAGING_DIR = Utils.join(Commit.COMMIT_DIR, "staging");
    /** Stage for Addition Hashmap. */
    private HashMap<String, Blobs> _addedFiles = new HashMap<>();
    /** Stage for Removal ArrayList. */
    private ArrayList<String> _removedFiles = new ArrayList<>();

    /** Staging Area. */
    public StagingArea() {
        File addPath = Utils.join(STAGING_DIR, "stage");
        Utils.writeObject(addPath, _addedFiles);
        File removePath = Utils.join(STAGING_DIR, "remove");
        Utils.writeObject(removePath, _removedFiles);

    }

    /** @param filename  Save files for future use.
     * @param content contents. */
    public void put(String filename, Blobs content) {
        _addedFiles.put(filename, content);
        File addfilesPath = Utils.join(STAGING_DIR, "stage");
        Utils.writeObject(addfilesPath, _addedFiles);
    }

    /** @return Getter method to get Stage for Addition Files. */
    public HashMap<String, Blobs> getaddedFiles() {
        return this._addedFiles;
    }

    /**  @return Getter method to get Stage for Removal Files. */
    public ArrayList<String> getremovedFiles() {
        return this._removedFiles;
    }

    /** @param filename Helper method to get rid of Addition files.
     * @param content contents.
     */
    public void removefromStage(String filename, Blobs content) {
        _addedFiles.remove(filename, content);
        File addedfilesPath = Utils.join(STAGING_DIR, "stage");
        Utils.writeObject(addedfilesPath, _addedFiles);
    }

    /** @param filename Helper method to remove file based
     * on name in Addition. */
    public void removestagefilename(String filename) {
        _addedFiles.remove(filename);
        File addedfilesPath = Utils.join(STAGING_DIR, "stage");
        Utils.writeObject(addedfilesPath, _addedFiles);
    }

    /** @param filename  Helper method to Stage for Removal. */
    public void stageforRemoval(String filename) {
        _removedFiles.add(filename);
        File addedfilesPath = Utils.join(STAGING_DIR, "stage");
        Utils.writeObject(addedfilesPath, _addedFiles);
        File removefilesPath = Utils.join(STAGING_DIR, "remove");
        Utils.writeObject(removefilesPath, _removedFiles);
    }

    /** @param filename Helper method to unremove
     * from Stage for Removal. */
    public void unremove(String filename) {
        _removedFiles.remove(filename);
        File removefilesPath = Utils.join(STAGING_DIR, "remove");
        Utils.writeObject(removefilesPath, _removedFiles);
    }

    /** Helper method to clear the Staging Area. */
    public void clearstage() {
        _addedFiles = new HashMap<>();
        _removedFiles = new ArrayList<>();
    }
}
