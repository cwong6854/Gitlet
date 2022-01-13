package gitlet;
import java.io.Serializable;
/** Blobs that are the contents of a file.
 * @author Curtis Wong
 */
public class Blobs implements Serializable {

    /** The name of the file. */
    private String _fileName;

    /** The contents of the file. */
    private String _fileContent;

    /** The ID of the blob. **/
    private String _id;

    /** Blob constructor holding name, content and id.
     * @param name name.
     * @param content content.
     * @param id id.
     */
    public Blobs(String name, String content, String id) {
        _fileName = name;
        _fileContent = content;
        _id = id;

    }

    /** @return Getter method to get the Blob's filename. */
    public String getfileName() {
        return _fileName;
    }

    /** @return Getter method to get the Blob's filecontent. */
    public String getfileContent() {
        return _fileContent;
    }

    /** Getter method to set the Blob's filecontent.
     * @param content contents.
     */
    public void setfileContent(String content) {
        _fileContent = content;
    }

    /** @return Getter method to get the Blob's ID. */
    public String getfileID() {
        return _id;
    }

}
