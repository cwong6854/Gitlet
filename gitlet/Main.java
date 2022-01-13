package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Curtis Wong
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> ....
     *  java gitlet.Main add hello.txt */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else {
            Repo r = new Repo();
            if (args[0].equals("init")) {
                r.init();
            } else if (args[0].equals("add")) {
                r.add(args);
            } else if (args[0].equals("commit")) {
                r.commit(args[1]);
            } else if (args[0].equals("log")) {
                r.log();
            } else if (args[0].equals("global-log")) {
                r.gl();
            } else if (args[0].equals("checkout")) {
                r.checkout(args);
            } else if (args[0].equals("status")) {
                r.status();
            } else if (args[0].equals("reset")) {
                r.reset(args[1]);
            } else if (args[0].equals("rm")) {
                r.rm(args[1]);
            } else if (args[0].equals("find")) {
                r.find(args[1]);
            } else if (args[0].equals("branch")) {
                r.branch(args[1]);
            } else if (args[0].equals("rm-branch")) {
                r.rmbranch(args[1]);
            } else if (args[0].equals("merge")) {
                r.merge(args[1]);
            } else {
                System.out.println("No command with that name exists.");
            }
        }
    }


}
