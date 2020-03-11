package io.archetypal.spark;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import com.google.common.io.Files;

/**
 * Interpret command line arguments and call Spark.init().
 */
public class Main {

  private static final int DEFAULT_PORT = 8787;

  public static void fatal(String message, int status) throws IOException {
    System.err.println("fatal: " + message);
    System.err.println();
    help(System.err);
    System.exit(status);
  }

  public static void help(PrintStream out) throws IOException {
    out.println("usage: java -jar git-api.jar [<options>] <repo> [<dir>]");
    out.println();

    out.println("  -r    override api root path");
    out.println("  -p    override listener port " + DEFAULT_PORT);
    out.println("  -b    checkout <branch> instead of the remote's HEAD");
    out.println("  -h    help text");
  }

  public static void main(String[] args) throws IOException {
    int port = DEFAULT_PORT;
    URL repo = null;
    File dir = null;
    String branch = null;
    String root= null;

    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      switch (arg) {
        case "-h":
          help(System.out);
          System.exit(0);
        case "-b":
          i++;
          if (i == args.length || args[i].startsWith("-")) {
            fatal("You must specify a branch after -b switch.", 2);
          }

          branch = args[i];
          continue;
        case "-p":
          i++;
          if (i == args.length || args[i].startsWith("-")) {
            fatal("You must specify a port after -p switch.", 2);
          }

          try {
            port = Integer.parseInt(args[i]);
          } catch (NumberFormatException e) {
            fatal("You must specify a numeric port: " + args[i], 3);
          }
          continue;
        default:
          if (repo == null) {
            try {
              repo = new URL(arg);
            } catch (Exception ex) {
              fatal(ex.getMessage(), 4);
            }
            continue;
          }
          if (dir == null) {
            try {
              if (arg.equals(".")) {
                dir = new File("");
              } else {
                dir = new File(arg);
              }
            } catch (Exception ex) {
              fatal(ex.getMessage(), 5);
            }
            continue;
          }
          fatal("Too many arguments", 6);
      }
    }

    if (repo == null) {
      fatal("You must specify a repository to clone.", 1);
    }

    if (dir == null) {
      dir = new File(Files.getNameWithoutExtension(repo.getPath()));
    }

    if (root == null) {
      root = "/v1/" + Files.getNameWithoutExtension(repo.getPath());
    }

    Spark.init(port, root, repo, branch, dir);
  }

}
