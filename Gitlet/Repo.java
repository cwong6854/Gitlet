package gitlet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** The Repo class, where everything happens in Gitlet.
 * @author Curtis Wong
 */
public class Repo {
    /**
     * Current Working Directory.
     */
    static final File CWD = new File(".");

    /**
     * Main metadata folder.
     */
    static final File GITLET_FOLDER = Utils.join(CWD, ".gitlet");

    /**
     * Staging Area.
     */
    private static StagingArea _stage;

    /** Ordered added files in Staging Area. */
    private static ArrayList<String> _orderedstage = new ArrayList<>();

    /**
     * Stage tracker for file name and contents.
     */
    private static HashMap<String, Blobs> _trackedfiles;

    /**
     * HEAD pointer that points to the active branch.
     */
    private static String _head;

    /** POINTER that points to the current commit. */
    private static HashMap<String, ArrayList<Commit>> _branchpointer;

    /**
     * String of Commit's ID.
     */
    private static String _commitID;

    /**
     * HashMap branch to hold all commits and their ID's.
     */
    private static HashMap<String, Commit> _allCommits = new HashMap<>();

    /**
     * HashMap that holds the branches names and their branch history.
     */
    private static HashMap<String, ArrayList<Commit>> _branch;

    /** Helper method to create a new branch. */
    private static ArrayList<Commit> _newBranch = new ArrayList<>();


    /** Initialize Gitlet version-control. */
    public static void init() {
        if (!GITLET_FOLDER.exists()) {
            GITLET_FOLDER.mkdirs();
            Commit.COMMIT_DIR.mkdirs();
            StagingArea.STAGING_DIR.mkdir();
            Commit initial = new Commit(
                    "initial commit", null, new HashMap<>());
            initial.setTimestamp("Thu Jan 1 00:00:00 1970 -0800");
            _allCommits.put(initial.getID(), initial);
            File commitPath = Utils.join(Commit.COMMIT_DIR, "allcommits");
            Utils.writeObject(commitPath, _allCommits);

            _branch = new HashMap<>();
            ArrayList<Commit> branchhistory = new ArrayList<>();
            branchhistory.add(initial);
            _branch.put("master", branchhistory);
            File pathBranchesname = Utils.join(Commit.COMMIT_DIR, "branches");
            Utils.writeObject(pathBranchesname, _branch);

            _head = "master";
            File pathHead = Utils.join(Commit.COMMIT_DIR, "HEAD");
            Utils.writeObject(pathHead, _head);

            _commitID = initial.getID();
            File pathcommitID = Utils.join(Commit.COMMIT_DIR, "ID");
            Utils.writeObject(pathcommitID, _commitID);

            _stage = new StagingArea();
            File pathStage = Utils.join(StagingArea.STAGING_DIR, "stage");
            Utils.writeObject(pathStage, _stage);

            _trackedfiles = new HashMap<>();
            File pathTrackedFiles = Utils.join(Commit.COMMIT_DIR, "commits");
            Utils.writeObject(pathTrackedFiles, _trackedfiles);

            File pathOrder = Utils.join(StagingArea.STAGING_DIR, "order");
            Utils.writeObject(pathOrder, _orderedstage);

            File pathnewBranch = Utils.join(Commit.COMMIT_DIR, "newbranch");
            Utils.writeObject(pathnewBranch, _newBranch);

        } else {
            System.out.println("A Gitlet version-control system"
                    + " already exists in the current directory.");
        }
    }

    /** Add command.
     @param args arguments for add.
     */
    public static void add(String[] args) {
        File adding = Utils.join(CWD, args[1]);
        if (!adding.exists()) {
            System.out.println("File does not exist.");
        } else {
            File cwvFile = Utils.join(Commit.COMMIT_DIR, "allcommits");
            _allCommits = Utils.readObject(cwvFile, HashMap.class);
            File pathHead = Utils.join(Commit.COMMIT_DIR, "HEAD");
            _head = Utils.readObject(pathHead, String.class);
            File pathStage = Utils.join(StagingArea.STAGING_DIR, "stage");
            _stage = Utils.readObject(pathStage, StagingArea.class);
            File pathOrder = Utils.join(StagingArea.STAGING_DIR, "order");
            _orderedstage = Utils.readObject(pathOrder, ArrayList.class);
            File pathID = Utils.join(Commit.COMMIT_DIR, "ID");
            _commitID = Utils.readObject(pathID, String.class);
            File pathBranch = Utils.join(Commit.COMMIT_DIR, "branches");
            _branch = Utils.readObject(pathBranch, HashMap.class);
            ArrayList<Commit> branchhistory = _branch.get(_head);
            Commit currentcommit = branchhistory.get(branchhistory.size() - 1);
            String contents = Utils.readContentsAsString(adding);
            String id = Utils.sha1(contents);
            Blobs addBlob = new Blobs(args[1], contents, id);
            List<String> cwdFiles = Utils.plainFilenamesIn(CWD);
            for (String file: cwdFiles) {
                if (currentcommit.getTrackedFiles().containsKey(file)) {
                    File pathFile = Utils.join(CWD, file);
                    String cwdContent = Utils.readContentsAsString(pathFile);
                    String filecontent = currentcommit.getTrackedFiles()
                            .get(file).getfileContent();
                    if (cwdContent.equals(filecontent)) {
                        if (_stage.getaddedFiles().containsKey(file)) {
                            _stage.removestagefilename(file);
                        }
                    }
                }
            }

            if (_stage.getremovedFiles().contains(args[1])) {
                _stage.unremove(args[1]);

            } else {
                _stage.put(args[1], addBlob);
            }
            if (!_orderedstage.contains(args[1])) {
                _orderedstage.add(args[1]);
            }

            File stagePath = Utils.join(StagingArea.STAGING_DIR, "stage");
            Utils.writeObject(stagePath, _stage);
            File orderPath = Utils.join(StagingArea.STAGING_DIR, "order");
            Utils.writeObject(orderPath, _orderedstage);
        }
    }

    /** Commit command.
     @param commitMsg message input for new Commit.
     */
    public static void commit(String commitMsg) {
        File stagePath = Utils.join(StagingArea.STAGING_DIR, "stage");
        _stage = Utils.readObject(stagePath, StagingArea.class);
        File pathHead = Utils.join(Commit.COMMIT_DIR, "HEAD");
        _head = Utils.readObject(pathHead, String.class);
        File commitPath = Utils.join(Commit.COMMIT_DIR, "allcommits");
        _allCommits = Utils.readObject(commitPath, HashMap.class);

        File pathID = Utils.join(Commit.COMMIT_DIR, "ID");
        _commitID = Utils.readObject(pathID, String.class);
        File pathBranchesnames = Utils.join(Commit.COMMIT_DIR, "branches");
        _branch = Utils.readObject(pathBranchesnames, HashMap.class);

        if (commitMsg.length() == 0) {
            System.out.println("Please enter a commit message.");
        } else if (_stage.getaddedFiles().isEmpty()
                && _stage.getremovedFiles().isEmpty()) {
            _stage.clearstage();
            System.out.println("No changes added to the commit.");
        } else {
            Commit copyCommit = new Commit(
                    commitMsg, _allCommits.get(_commitID).getID(),
                    _allCommits.get(_commitID).getTrackedFiles());
            _commitID = copyCommit.getID();
            File pathsID = Utils.join(Commit.COMMIT_DIR, "ID");
            File copyPathFile = Utils.join(Commit.COMMIT_DIR, "copy");
            Utils.writeObject(copyPathFile, _commitID);
            Utils.writeObject(pathsID, _commitID);
            copyCommit.setTrackedFiles(_stage.getaddedFiles());
            _allCommits.put(copyCommit.getID(), copyCommit);
            Utils.writeObject(commitPath, _allCommits);

            String branchname = _head;
            ArrayList<Commit> branchhistory
                    = (ArrayList<Commit>) _branch.get(_head).clone();
            branchhistory.add(copyCommit);
            _branch.put(branchname, branchhistory);
            Utils.writeObject(pathBranchesnames, _branch);

            _stage.clearstage();
            Utils.writeObject(stagePath, _stage);
        }
    }

    /** Log command. */
    public static void log() {
        File commitPath = Utils.join(Commit.COMMIT_DIR, "allcommits");
        _allCommits = Utils.readObject(commitPath, HashMap.class);
        File pathbranchNames = Utils.join(Commit.COMMIT_DIR, "branches");
        _branch = Utils.readObject(pathbranchNames, HashMap.class);
        File pathHead = Utils.join(Commit.COMMIT_DIR, "HEAD");
        _head = Utils.readObject(pathHead, String.class);
        ArrayList<Commit> branchhistory = _branch.get(_head);
        for (int i = branchhistory.size() - 1; i >= 0; i -= 1) {
            Commit commits = branchhistory.get(i);
            System.out.println("===");
            System.out.println("commit " + commits.getID());
            System.out.println("Date: " + commits.getTimeStamp());
            System.out.println(commits.getMessage());
            System.out.println();
        }
    }

    /** Global Log command. */
    public static void gl() {
        File commitPath = Utils.join(Commit.COMMIT_DIR, "allcommits");
        _allCommits = Utils.readObject(commitPath, HashMap.class);
        for (Commit commits: _allCommits.values()) {
            System.out.println("===");
            System.out.println("commit " + commits.getID());
            System.out.println("Date: " + commits.getTimeStamp());
            System.out.println(commits.getMessage());
            System.out.println();
        }
    }

    /** Checkout command.
     * @param args arguments for checkout.
     */
    public static void checkout(String[] args) {
        File allcommitsPath = Utils.join(Commit.COMMIT_DIR, "allcommits");
        _allCommits = Utils.readObject(allcommitsPath, HashMap.class);
        File commitIDPath = Utils.join(Commit.COMMIT_DIR, "ID");
        _commitID = Utils.readObject(commitIDPath, String.class);
        File stagePath = Utils.join(StagingArea.STAGING_DIR, "stage");
        _stage = Utils.readObject(stagePath, StagingArea.class);
        File pathBranch = Utils.join(Commit.COMMIT_DIR, "branches");
        _branch = Utils.readObject(pathBranch, HashMap.class);
        File pathHEAD = Utils.join(Commit.COMMIT_DIR, "HEAD");
        _head = Utils.readObject(pathHEAD, String.class);

        if (args.length == 1) {
            throw new GitletException("Not enough commands given.");
        } else if (args.length == 3) {
            ArrayList<Commit> branchhistory = _branch.get(_head);
            Commit currentcommit = branchhistory.get(branchhistory.size() - 1);
            HashMap<String, Blobs> trackcurrent =
                    currentcommit.getTrackedFiles();
            Blobs currentblob = trackcurrent.get(args[2]);
            File checkoutPath = Utils.join(CWD, args[2]);
            Utils.writeContents(checkoutPath, currentblob.getfileContent());
        } else if (args.length == 4 && args[2].equals("--")) {
            checkoutCommitId(args);
        } else if (args.length == 2) {
            checkoutBranch(args);
        } else {
            System.out.println("Incorrect operands.");
        }
    }

    /** Checkout Helper function for commit ID and filename.
     * @param args arguments for checkout.
     */
    public static void checkoutCommitId(String[] args) {
        String checkoutID = args[1];
        String checkoutfileName = args[3];
        int printcountFileExist = 0;
        int printcountIDexist = 0;
        int filenotexist = 0;
        int idnotexist = 0;
        HashMap<String, Blobs> checkoutcurrent;
        Blobs checkoutblob;
        File checkoutPath;
        ArrayList<Commit> branchhistory = _branch.get(_head);
        for (int i = branchhistory.size() - 1; i >= 0; i -= 1) {
            Commit commits = branchhistory.get(i);
            Pattern p = Pattern.compile("^[\\w+\\d+]{8}");
            Matcher m = p.matcher(commits.getID());
            if (m.find()) {
                if (commits.getID().equals(checkoutID)
                        || m.group(0).equals(args[1])) {
                    printcountIDexist += 1;
                    for (String trackfile
                            : commits.getTrackedFiles().keySet()) {
                        if (trackfile.equals(checkoutfileName)) {
                            printcountFileExist += 1;
                            checkoutcurrent = commits.getTrackedFiles();
                            checkoutblob = checkoutcurrent.get(checkoutfileName);
                            checkoutPath = Utils.join(CWD, checkoutfileName);
                            Utils.writeContents(checkoutPath, checkoutblob.getfileContent());
                        } else {
                            filenotexist += 1;
                        }
                    }
                } else {
                    idnotexist += 1;
                }
            }
        }
        if (printcountFileExist - filenotexist != printcountFileExist) {
            System.out.println("File does not exist in that commit.");
        } else if (Math.abs(printcountIDexist - idnotexist)
                == branchhistory.size()) {
            System.out.println("No commit with that id exists.");
        } else {
            printcountFileExist = 0;
            printcountIDexist = 0;
        }
    }

    /** Checkout Helper method for branches.
     @param args arguments for checkout.
     */
    public static void checkoutBranch(String[] args) {
        File pathBranch = Utils.join(Commit.COMMIT_DIR, "branches");
        _branch = Utils.readObject(pathBranch, HashMap.class);
        File pathHEAD = Utils.join(Commit.COMMIT_DIR, "HEAD");
        _head = Utils.readObject(pathHEAD, String.class);
        File stagePath = Utils.join(StagingArea.STAGING_DIR, "stage");
        _stage = Utils.readObject(stagePath, StagingArea.class);
        String branchname = args[1];
        if (!_branch.containsKey(branchname)) {
            System.out.println("No such branch exists.");
        } else if (branchname.equals(_head)) {
            System.out.println("No need to checkout the current branch.");
        } else {
            ArrayList<Commit> currentBranch = _branch.get(_head);
            ArrayList<Commit> givenBranch = _branch.get(branchname);
            Commit currentCommit = currentBranch.get(
                    currentBranch.size() - 1);
            Commit givenCommit = givenBranch.get(givenBranch.size() - 1);
            List<String> cwdFiles = Utils.plainFilenamesIn(CWD);
            for (String filename : cwdFiles) {
                File pathFile = Utils.join(CWD, filename);
                String fileContents = Utils.readContentsAsString(pathFile);
                if (!currentCommit.getTrackedFiles().containsKey(filename)
                        && givenCommit.getTrackedFiles().containsKey(
                        filename)) {
                    if (!givenCommit.getTrackedFiles().get(filename)
                            .getfileContent().equals(fileContents)) {
                        System.out.println("There is an untracked file "
                                + "in the way; delete it, "
                                + "or add and commit it first.");
                        return;
                    }
                }
            }
            for (String giventracked
                    : givenCommit.getTrackedFiles().keySet()) {
                File pathTracked = Utils.join(CWD, giventracked);
                String trackedContent = givenCommit.getTrackedFiles().get(
                        giventracked).getfileContent();
                Utils.writeContents(pathTracked, trackedContent);
            }
            for (String filename : cwdFiles) {
                File pathFile = Utils.join(CWD, filename);
                if (!givenCommit.getTrackedFiles().containsKey(filename)) {
                    pathFile.delete();
                }
            }
            _head = branchname;
            _stage.clearstage();
            Utils.writeObject(pathHEAD, _head);
            Utils.writeObject(stagePath, _stage);
            Utils.writeObject(pathBranch, _branch);
        }
    }
    /** Status command. */
    public static void status() {
        if (!GITLET_FOLDER.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
        } else {
            System.out.println("=== Branches ===");
            File pathHead = Utils.join(Commit.COMMIT_DIR, "HEAD");
            _head = Utils.readObject(pathHead, String.class);
            File pathBranch = Utils.join(Commit.COMMIT_DIR, "branches");
            _branch = Utils.readObject(pathBranch, HashMap.class);
            File pathAllCommits = Utils.join(Commit.COMMIT_DIR, "allcommits");
            _allCommits = Utils.readObject(pathAllCommits, HashMap.class);
            SortedSet<String> branchkeys = new TreeSet<>(_branch.keySet());
            ArrayList<Commit> branchhistory = _branch.get(_head);
            Commit currcommit = branchhistory.get(branchhistory.size() - 1);
            for (String branchname : branchkeys) {
                if (branchname.equals(_head)) {
                    System.out.println("*" + branchname);
                } else {
                    System.out.println(branchname);
                }
            }
            System.out.println();
            System.out.println("=== Staged Files ===");
            statushelper();
            System.out.println();
            System.out.println("=== Removed Files ===");
            for (String removedFiles : _stage.getremovedFiles()) {
                System.out.println(removedFiles);
            }
            System.out.println();
            System.out.println("=== Modifications Not Staged For Commit ===");
            System.out.println();
            System.out.println("=== Untracked Files ===");
//            List<String> cwdFiles = Utils.plainFilenamesIn(CWD);
//            for (String file: cwdFiles) {
//                File pathFile = Utils.join(CWD, file);
//                String contents = Utils.readContentsAsString(pathFile);
//                if (pathFile.exists()) {
//                    if (!_stage.getaddedFiles().containsKey(file)
//                            && currcommit.getTrackedFiles().containsKey(file)) {
//                        if (!currcommit.getTrackedFiles().get(file)
//                                .getfileContent().equals(contents)) {
//                            System.out.println(file);
//                        }
//                    }
//                }
//            }
            System.out.println();
        }
    }

    /** Status helper method mainly for Staged Files. */
    public static void statushelper() {
        File pathHead = Utils.join(Commit.COMMIT_DIR, "HEAD");
        _head = Utils.readObject(pathHead, String.class);
        File pathBranch = Utils.join(Commit.COMMIT_DIR, "branches");
        _branch = Utils.readObject(pathBranch, HashMap.class);
        File pathOrder = Utils.join(StagingArea.STAGING_DIR, "order");
        _orderedstage = Utils.readObject(pathOrder, ArrayList.class);
        File pathStage = Utils.join(StagingArea.STAGING_DIR, "stage");
        _stage = Utils.readObject(pathStage, StagingArea.class);
        ArrayList<Commit> branchhistory = _branch.get(_head);
        Commit currentcommit = branchhistory.get(branchhistory.size() - 1);
        if (!_stage.getaddedFiles().isEmpty()) {
            for (int i = _orderedstage.size() - 1; i >= 0; i -= 1) {
                String filename = _orderedstage.get(i);
                if (_stage.getaddedFiles().containsKey(filename)) {
                    if (currentcommit.getTrackedFiles().containsKey(
                            filename)) {
                        String content = currentcommit.getTrackedFiles().get(
                                filename).getfileContent();
                        if (content.equals(_stage.getaddedFiles().get(
                                filename).getfileContent())) {
                            String donothing = filename;
                        } else {
                            System.out.println(filename);
                        }
                    } else if (!currentcommit.getTrackedFiles().containsKey(
                            filename)
                            && _stage.getaddedFiles().containsKey(
                            filename)) {
                        System.out.println(filename);
                    }
                }
            }
        }
    }
    /** Remove command.
     * @param filename file name.
     */
    public static void rm(String filename) {
        File pathStage = Utils.join(StagingArea.STAGING_DIR, "stage");
        _stage = Utils.readObject(pathStage, StagingArea.class);
        File pathBranch = Utils.join(Commit.COMMIT_DIR, "branches");
        _branch = Utils.readObject(pathBranch, HashMap.class);
        File pathHead = Utils.join(Commit.COMMIT_DIR, "HEAD");
        _head = Utils.readObject(pathHead, String.class);

        File cwdFile = Utils.join(CWD, filename);
        ArrayList<Commit> branchhistory = _branch.get(_head);
        Commit currcommit = branchhistory.get(branchhistory.size() - 1);

        if (currcommit.getTrackedFiles().isEmpty()
                && _stage.getaddedFiles().isEmpty()) {
            System.out.println("No reason to remove the file.");
        } else {
            Commit currentcommit = branchhistory.get(branchhistory.size() - 1);
            for (String stagefilename : _stage.getaddedFiles().keySet()) {
                if (stagefilename.equals(filename)) {
                    Blobs filecontent = _stage.getaddedFiles().get(
                            stagefilename);
                    _stage.removefromStage(filename, filecontent);
                }
            }
            if (currentcommit.getTrackedFiles().isEmpty()) {
                currentcommit.setTrackedFiles(new HashMap<>());
            } else {
                if (currentcommit.getTrackedFiles().containsKey(filename)) {
                    _stage.stageforRemoval(filename);
                    Blobs rmfile = currentcommit.getTrackedFiles().get(
                            filename);
                    currentcommit.removeTrackedFile(rmfile);
                    if (cwdFile.exists()) {
                        cwdFile.delete();
                    }
                }
            }
        }
        Utils.writeObject(pathBranch, _branch);
        Utils.writeObject(pathStage, _stage);
    }

    /** Rmbranch command.
     * @param branchname Branch name.
     */
    public static void rmbranch(String branchname) {
        File pathBranches = Utils.join(Commit.COMMIT_DIR, "branches");
        _branch = Utils.readObject(pathBranches, HashMap.class);
        File pathHead = Utils.join(Commit.COMMIT_DIR, "HEAD");
        _head = Utils.readObject(pathHead, String.class);
        if (!_branch.containsKey(branchname)) {
            System.out.println("A branch with that name does not exist.");
        } else {
            if (branchname.equals(_head)) {
                System.out.println("Cannot remove the current branch.");
            } else {
                _branch.remove(branchname);
                Utils.writeObject(pathBranches, _branch);
            }
        }
    }

    /** Find command.
     * @param commitMsg message of commit
     */
    public static void find(String commitMsg) {
        File pathBranch = Utils.join(Commit.COMMIT_DIR, "branches");
        _branch = Utils.readObject(pathBranch, HashMap.class);
        File pathHEAD = Utils.join(Commit.COMMIT_DIR, "HEAD");
        _head = Utils.readObject(pathHEAD, String.class);
        ArrayList<Commit> branchhistory = _branch.get(_head);
        File pathAllCommits = Utils.join(Commit.COMMIT_DIR, "allcommits");
        _allCommits = Utils.readObject(pathAllCommits, HashMap.class);

        int printcount = 0;
        int nocommitMsg = 0;
        for (int i = branchhistory.size() - 1; i >= 0; i -= 1) {
            Commit commits = branchhistory.get(i);
            if (commits.getMessage().equals(commitMsg)) {
                System.out.println(commits.getID());
                printcount += 1;
            }
        }
        if (printcount == 0) {
            for (Commit commits: _allCommits.values()) {
                if (commits.getMessage().equals(commitMsg)) {
                    nocommitMsg += 1;
                }
            }
            if (nocommitMsg > 0) {
                for (Commit commits: _allCommits.values()) {
                    if (commits.getMessage().equals(commitMsg)) {
                        System.out.println(commits.getID());
                    }
                }
            } else {
                System.out.println("Found no commit with that message.");
            }
        }
    }

    /** Branch command, creates pointer.
     * @param branchname branch name.
     */
    public static void branch(String branchname) {
        File pathBranch = Utils.join(Commit.COMMIT_DIR, "branches");
        _branch = Utils.readObject(pathBranch, HashMap.class);
        File pathHEAD = Utils.join(Commit.COMMIT_DIR, "HEAD");
        _head = Utils.readObject(pathHEAD, String.class);
        ArrayList<Commit> branchhistory = _branch.get(_head);

        if (_branch.containsKey(branchname)) {
            System.out.println("A branch with that name already exists.");
        } else {
            _branch.put(branchname, branchhistory);
            File pathbranchPointer = Utils.join(Commit.COMMIT_DIR, "branches");
            Utils.writeObject(pathbranchPointer, _branch);
        }
    }

    /** Reset command.
     * @param commitID commit ID.
     */
    public static void reset(String commitID) {
        File pathBranches = Utils.join(Commit.COMMIT_DIR, "branches");
        _branch = Utils.readObject(pathBranches, HashMap.class);
        File pathAllCommits = Utils.join(Commit.COMMIT_DIR, "allcommits");
        _allCommits = Utils.readObject(pathAllCommits, HashMap.class);
        File pathHead = Utils.join(Commit.COMMIT_DIR, "HEAD");
        _head = Utils.readObject(pathHead, String.class);
        File pathStage = Utils.join(StagingArea.STAGING_DIR, "stage");
        _stage = Utils.readObject(pathStage, StagingArea.class);
        if (!_allCommits.containsKey(commitID)) {
            System.out.println("No commit with that id exists.");
        } else {
            List<String> cwdFiles = Utils.plainFilenamesIn(CWD);
            ArrayList<Commit> currentbranch = _branch.get(_head);
            Commit currentcommit = currentbranch.get(currentbranch.size() - 1);
            Commit givencommit = _allCommits.get(commitID);
            for (String filename : cwdFiles) {
                File pathFile = Utils.join(CWD, filename);
                String fileContents =
                        Utils.readContentsAsString(pathFile);
                if (!currentcommit.getTrackedFiles().
                        containsKey(filename)
                        && givencommit.getTrackedFiles().
                        containsKey(filename)) {
                    if (!givencommit.getTrackedFiles().
                            get(filename).getfileContent()
                            .equals(fileContents)) {
                        System.out.println("There is an untracked file"
                                + "in the way; delete it, "
                                + "or add and commit it first.");
                    }
                }
            }
            for (String filename : cwdFiles) {
                File pathFile = Utils.join(CWD, filename);
                if (!givencommit.getTrackedFiles().containsKey(filename)
                        && currentcommit.
                        getTrackedFiles().containsKey(filename)) {
                    pathFile.delete();
                }
            }
            ArrayList<Commit> newBranch = reset2(commitID);
            reset3(newBranch, commitID);
            _branch.replace(_head, newBranch);
            _stage.clearstage();
            Utils.writeObject(pathBranches, _branch);
            Utils.writeObject(pathHead, _head);
            Utils.writeObject(pathStage, _stage);
            Utils.writeObject(pathAllCommits, _allCommits);
        }
    }

    /** @ return Reset helper method to add to
     * ArrayList to move current branch
     * to given commit id node.
     * @param commitID commit id.
     */
    public static ArrayList<Commit> reset2(String commitID) {
        File pathBranch = Utils.join(Commit.COMMIT_DIR, "branches");
        _branch = Utils.readObject(pathBranch, HashMap.class);
        File pathHead = Utils.join(Commit.COMMIT_DIR, "HEAD");
        _head = Utils.readObject(pathHead, String.class);
        ArrayList<Commit> currentbranch = _branch.get(_head);
        ArrayList<Commit> newBranch = new ArrayList<>();
        for (Commit commits : currentbranch) {
            if (!commits.getID().equals(commitID)) {
                newBranch.add(commits);
            } else {
                newBranch.add(commits);
                break;
            }
        } return newBranch;
    }

    /** Reset helper method to see if given id is not in active branch.
     @param newBranch new branch.
     @param id ID.
     */
    public static void reset3(ArrayList<Commit> newBranch, String id) {
        File pathAllCommits = Utils.join(Commit.COMMIT_DIR, "allcommits");
        _allCommits = Utils.readObject(pathAllCommits, HashMap.class);
        int identicalID = 0;
        for (Commit newBranchcommit : newBranch) {
            if (newBranchcommit.getID().equals(id)) {
                identicalID += 1;
            }
        }

        if (identicalID > 0) {
            identicalID = 0;
        } else {
            Commit missingCommit = _allCommits.get(id);
            newBranch.add(newBranch.size(), missingCommit);
        }
    }

    /** Merge command.
     @param branchname branch name.
     */
    public static void merge(String branchname) {
        File pathHead = Utils.join(Commit.COMMIT_DIR, "HEAD");
        _head = Utils.readObject(pathHead, String.class);
        File pathBranch = Utils.join(Commit.COMMIT_DIR, "branches");
        _branch = Utils.readObject(pathBranch, HashMap.class);
        File pathStage = Utils.join(StagingArea.STAGING_DIR, "stage");
        _stage = Utils.readObject(pathStage, StagingArea.class);
        File pathallCommits = Utils.join(Commit.COMMIT_DIR, "allcommits");
        _allCommits = Utils.readObject(pathallCommits, HashMap.class);
        if (!_branch.containsKey(branchname)) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (!_stage.getaddedFiles().isEmpty()
                || !_stage.getremovedFiles().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        } else if (_head.equals(branchname)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        ArrayList<Commit> currentbranch = _branch.get(_head);
        ArrayList<Commit> givenbranch = _branch.get(branchname);

        Commit currentcommit = currentbranch.get(currentbranch.size() - 1);
        Commit givencommit = givenbranch.get(givenbranch.size() - 1);
        List<String> cwdFiles = Utils.plainFilenamesIn(CWD);
        for (String filename : cwdFiles) {
            File pathFile = Utils.join(CWD, filename);
            String fileContents =
                    Utils.readContentsAsString(pathFile);
            if (!currentcommit.getTrackedFiles().
                    containsKey(filename)
                    && givencommit.getTrackedFiles().
                    containsKey(filename)) {
                if (!givencommit.getTrackedFiles().
                        get(filename).getfileContent()
                        .equals(fileContents)) {
                    System.out.println("There is an untracked file "
                            + "in the way; delete it, "
                            + "or add and commit it first.");
                    return;
                }
            }
        }
        // get split point / common ancestors
        Commit splitpoint = findsplitpoint(currentbranch, givenbranch);
        // check if files present and if modified
        if (splitpoint.getID().equals(givencommit)) {
            System.out.println("Given branch is an ancestor of the current branch.");
        }
        HashMap<String, Blobs> allFiles = new HashMap<>();
        allFiles.putAll(splitpoint.getTrackedFiles());
        allFiles.putAll(givencommit.getTrackedFiles());
        allFiles.putAll(currentcommit.getTrackedFiles());
        for (String files: allFiles.keySet()) {

            if (!splitpoint.getTrackedFiles().containsKey(files) && !givencommit.getTrackedFiles().containsKey(files)
                    && currentcommit.getTrackedFiles().containsKey(files)) {
                // do nothing
            } else if (!splitpoint.getTrackedFiles().containsKey(files) && !currentcommit.getTrackedFiles().containsKey(files)
                    && givencommit.getTrackedFiles().containsKey(files)) {

            }
        }
    }
    public static Commit findsplitpoint(ArrayList<Commit> current, ArrayList<Commit> given) {
        //FIXME
        // if equal branches are equal of length
        Commit splitpoint = null;
        List<String> master = new ArrayList<>();
        List<String> branch = new ArrayList<>();
        if (current.size() == given.size()) {
            for (int i  = 0; i < current.size(); i += 1) {
                if (current.get(i).getParent() == null) {
                    continue;
                } else {
                    master.add(current.get(i).getParent());
                }
            } for (int j = 0; j < given.size(); j += 1) {
                if (given.get(j).getParent() == null) {
                    continue;
                } else {
                    branch.add(given.get(j).getParent());
                }
            } for (int k = 0; k < master.size(); k += 1) {
                if (master.get(k).equals(branch.get(k))) {
                    splitpoint = _allCommits.get(master.get(k + 1));
                }
            }
        } else {
            // if current active branch isn't same size as given branch...
            // DSF or BSF?
        }
        return splitpoint;
    }
}
