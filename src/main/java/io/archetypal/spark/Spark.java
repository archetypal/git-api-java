package io.archetypal.spark;

import static spark.Spark.awaitStop;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.notFound;
import static spark.Spark.path;
import static spark.Spark.port;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.jgit.dircache.DirCacheEntry;

import io.archetypal.git.GitObjectStore;

/**
 * Utilize spark to wrap GitDB
 */
public class Spark {

  public static void init(int port, String root, URL repo, String branch, File dir) throws IOException {

    GitObjectStore git = new GitObjectStore(new File(dir, ".git"));
    git.init(dir, repo, branch);
    port(port);

    System.out.println("root: " + root);

    //
    // Standard git dumb protocol
    //

    int pathStart = root.length() + 1;

    path(root + ".git", () -> {
      get("/HEAD", (req, res) -> {
        res.type("text/plain; charset=utf-8");
        return git.branch();
      });

      get("/objects/:objectId", (req, res) -> {
        byte[] bytes = git.get(req.params(":objectId"));
        if (bytes == null) {
          res.status(404);
          return "";
        }
        return bytes;
      });

      // Non-Standard RPC
      get("/fetch", (req, res) -> {
        res.type("application/json");
        git.fetch();
        res.status(204);
        return "";
      });

    });

    get(root + "/*", (req, res) -> {
      String path = req.pathInfo();
      if (path.length() == pathStart) {
        // path is /v1/<app>/ - no list of files supported
        res.status(404);
        return "";
      }

      System.out.println("GET " + path.substring(pathStart));

      DirCacheEntry entry = git.getPath(path.substring(pathStart));
      if (entry == null) {
        res.status(404);
        return "";
      }

      byte[] bytes = git.getByEntry(entry);
      if (bytes == null) {
        res.status(404);
        return "";
      }

      // TODO Last-Modified header
      res.header("Location", root + ".git/objects/" + entry.getObjectId().getName());
      return bytes;
    });

    notFound((req, res) -> "");

    get(root + ".health", (req, res) -> {
      res.status(204);
      return "";
    });

    exception(Exception.class, (exception, req, res) -> {
      res.type("application/json");
      res.status(400);
      res.body(exception.getMessage());
    });

    get("/*", (req, res) -> {
      // stopgap for rest of unmatched calls to return 404
      res.status(404);
      return "";
    });

    awaitStop();

    git.close();
  }

}
