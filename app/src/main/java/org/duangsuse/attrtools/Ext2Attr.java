package org.duangsuse.attrtools;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

/** Ext2 like filesystem file attribute querying library
 *
 * @author duangsuse
 * @see "https://github.com/duangsuse/attrtools"
 * @see "e2imutable.c"
 * @since 1.0
 * @version 1.2
 */
public class Ext2Attr implements Closeable {
    private static final String command_fmt = "%1$s %2$s %3$s;printf $?_";
    private static final String parse_error_msg = "Cannot parse status";
    /** Error string field */
    @SuppressWarnings("WeakerAccess")
    public static String error = "";

    /** E2IM executable path */
    @SuppressWarnings("WeakerAccess")
    public String lib_path = "libe2im.so";

    /** Shell executable path */
    @SuppressWarnings("WeakerAccess")
    public String su_path = "su";

    /** Shell process instance */
    @SuppressWarnings("WeakerAccess")
    public Process shell = null;

    /** Shell stdin */
    @SuppressWarnings("WeakerAccess")
    public PrintStream stdin = null;

    /** Shell stdout */
    @SuppressWarnings("WeakerAccess")
    public Scanner stdout = null;

    /** Shell stderr */
    @SuppressWarnings("WeakerAccess")
    public Scanner stderr = null;

    /** Default constructor
     *
     * @param su_path Super User binary path
     * @param lib_path e2im program path
     */
    @SuppressWarnings("unused")
    public Ext2Attr(String su_path, String lib_path) {
        this.su_path = su_path;
        this.lib_path = lib_path;
        new Thread(this::connect).start();
    }

    /** Construct a new instance using custom executable
     *
     * @param lib_path e2im executable library path
     */
    @SuppressWarnings("WeakerAccess")
    public Ext2Attr(String lib_path) {
        this.lib_path = lib_path;
        new Thread(this::connect).start();
    }

    /** Use this constructor to avoid connect() when start
     */
    @SuppressWarnings("unused")
    public Ext2Attr() {}

    /** Disconnect form shell */
    @Override
    public void close() {
        stdin.println("exit");
        stdin.flush();
        try {
            shell.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /** Connect with shell */
    @SuppressWarnings("WeakerAccess")
    public void connect() {
        try {
            shell = Runtime.getRuntime().exec(this.su_path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (shell == null)
            return;

        stdin = new PrintStream(shell.getOutputStream());
        stdout = new Scanner(new DataInputStream(shell.getInputStream()));
        stderr = new Scanner(new DataInputStream(shell.getErrorStream()));
        stdout.useDelimiter("_");
    }

    /** Not connected with shell? */
    public boolean not_connected() {
        return shell == null || stdin == null || !isAlive(shell);
    }

    /** Is a process alive?
     *
     * @param p target process
     * @return true if alive
     */
    private static boolean isAlive(Process p) {
        boolean ret = false;
        try {
            p.exitValue();
        } catch (IllegalThreadStateException ignored) {
            ret = true;
        }
        return ret;
    }

    /** Query file attribute
     *
     * @return 0 for no attribute<p>
     *     1 for +i<p>
     *     2 for +a<p>
     *     3 for +i+a<p>
     *     -1 for no file
     *
     * @throws RuntimeException reading attr fails
     */
    public byte query(String path) throws RuntimeException {
        String command = String.format(command_fmt, lib_path, '@', path);
        stdin.println(command);
        stdin.flush();

        switch (stdout.nextInt()) {
            case 0:
                return 0;
            case 255:
                return -1;
            case 254:
                error = stderr.nextLine();
                throw new RuntimeException(error);
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            default:
                throw new RuntimeException(parse_error_msg);
        }
    }

    /** File attribute +i
     *
     * @return 0 for changed<p>
     *     1 for unchanged<p>
     *     -1 for no file
     *
     * @throws RuntimeException change attr fails
     */
    public byte addi(String path) throws RuntimeException {
        String command = String.format(command_fmt, lib_path, '+', path);
        stdin.println(command);
        stdin.flush();

        switch (stdout.nextInt()) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 255:
                return -1;
            case 254:
                error = stderr.nextLine();
                throw new RuntimeException(error);
            default:
                throw new RuntimeException(parse_error_msg);
        }
    }

    /** File attribute -i
     *
     * @return 0 for changed<p>
     *     1 for unchanged<p>
     *     -1 for no file
     *
     * @throws RuntimeException change attr fails
     */
    public byte subi(String path) throws RuntimeException {
        String command = String.format(command_fmt, lib_path, '-', path);
        stdin.println(command);
        stdin.flush();

        switch (stdout.nextInt()) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 255:
                return -1;
            case 254:
                error = stderr.nextLine();
                throw new RuntimeException(error);
            default:
                throw new RuntimeException(parse_error_msg);
        }
    }
}
