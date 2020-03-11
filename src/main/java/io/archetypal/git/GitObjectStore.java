package io.archetypal.git;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Base64;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEditor;
import org.eclipse.jgit.dircache.DirCacheEditor.DeletePath;
import org.eclipse.jgit.dircache.DirCacheEditor.PathEdit;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectDatabase;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TagBuilder;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class GitObjectStore implements AutoCloseable {

  // base 64 url encoder without padding
  private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder decoder = Base64.getUrlDecoder();

  public static String toBase64(ObjectId objectId) {
    ByteBuffer id = ByteBuffer.allocate(20);
    objectId.copyRawTo(id);
    return encoder.encodeToString(id.array());
  }

  public static ObjectId fromString(String id) {
    if (id == null) {
      throw new IllegalArgumentException("Object Id may not be null.");
    }
    switch (id.length()) {
      case 40:
        return ObjectId.fromString(id);
      case 41:
        return ObjectId.fromString(id.substring(0, 2) + id.substring(3));
      case 27:
        return ObjectId.fromRaw(decoder.decode(id));
    }
    throw new IllegalArgumentException("Id must be a 40 character hex, 41 character hex path, or 27 character base64URL with no padding.  Unknown id of length(" + id.length() + "): " + id);
  }

  public GitObjectStore(File gitDir) throws IOException {
    repo = FileRepositoryBuilder.create(gitDir);
    db = repo.getObjectDatabase().newCachedDatabase();
    git = new Git(repo);
  }

  public void init(File path, URL uri, String branch) throws IOException {
    if (path.exists()) {
      return;
    }
    CloneCommand clone = Git.cloneRepository();
    clone.setDirectory(path);
    clone.setBranch(branch);
    clone.setURI(uri.toString());
    try {
      clone.call();
    } catch (GitAPIException e) {
      throw new IOException(e);
    }

    System.out.println("Cloned to: " + repo.getDirectory());
  }

  public String branch() throws IOException {
    return repo.getBranch();
  }

  public void fetch() throws IOException {
    
    try {

      // doing Pull for now.  When switching back to Fetch, need to update HEAD to FETCH_HEAD
      PullCommand cmd = git.pull();
      cmd.call();

      // advance head to FETCH_HEAD
      

    } catch (GitAPIException e) {
      throw new IOException(e);
    }
  }

  public String put(byte[] bytes) throws IOException {
    try (final ObjectInserter ins = db.newInserter()) {
      ObjectId id = ins.insert(Constants.OBJ_BLOB, bytes);
      ins.flush();
      return toBase64(id);
    }
  }

  public String put(String path, byte[] bytes) throws IOException {
    String id = put(bytes);
    putEntry(path, Instant.now(), bytes.length, id);
    return id;
  }

  public byte[] get(String id) throws IOException {
    try (final ObjectReader reader = db.newReader()) {
      return reader.open(fromString(id)).getBytes();
    } catch (MissingObjectException e) {
      return null;
    }
  }

  public DirCacheEntry getPath(String path) throws IOException {
    return repo.readDirCache().getEntry(path);
  }

  public byte[] getByPath(String path) throws IOException {
    return getByEntry(getPath(path));
  }

  public byte[] getByEntry(DirCacheEntry entry) throws IOException {
    try (final ObjectReader reader = db.newReader()) {
      return reader.open(entry.getObjectId(), Constants.OBJ_BLOB).getBytes();
    } catch (MissingObjectException e) {
      return null;
    }
  }



  public void putEntry(String path, Instant lastModified, int length, String objectId) throws IOException {
    PathEdit pe = new PathEdit(path) {
      @Override
      public void apply(DirCacheEntry ent) {
        if (ent.getCreationTime() == 0) {
          ent.setCreationTime(lastModified.toEpochMilli());
        }
        ent.setFileMode(FileMode.REGULAR_FILE);
        ent.setLastModified(lastModified);
        ent.setObjectId(fromString(objectId));
      }
    };
    edit(pe);
  }

  public void deletePath(String path) throws IOException {
    edit(new DeletePath(path));
  }

  private void edit(PathEdit edit) throws IOException {
    DirCache dc = repo.lockDirCache();
    try {
      DirCacheEditor editor = dc.editor();
      editor.add(edit);
      editor.finish();
    } finally {
      dc.write();
      dc.commit();
    }
  }

  public String putTag(TagBuilder tag) throws IOException {
    try (final ObjectInserter ins = db.newInserter()) {
      ObjectId id = ins.insert(tag);
      ins.flush();
      return toBase64(id);
    }
  }


  @Override
  public void close() {
    repo.close();
    db.close();
  }

  private final Repository repo;
  private final ObjectDatabase db;
  private final Git git;
}
