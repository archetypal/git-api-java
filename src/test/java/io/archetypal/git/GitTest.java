package io.archetypal.git;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eclipse.jgit.lib.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class GitTest {

  static final Gson gson = new GsonBuilder().serializeNulls().create();

  private static GitObjectStore git;

  @BeforeAll
  public static void setUp() throws IOException {
    git = new GitObjectStore(new File("/tmp/repo/.git"));
  }

  @AfterAll
  public static void tearDown() {
    git.close();
  }

  @Test
  public void testFetch() throws IOException {
    git.fetch();
  }

  @Test
  @Disabled
  public void test() throws IOException {
    System.out.println("Starting test");
    // takes a while to load the repo


    String id = git.put("Hello World!".getBytes());
    System.out.println(id);

    git.put("Hello World!".getBytes());


    String hw = new String(git.get(id));

    assertEquals("Hello World!", hw);

    Instant lm = Instant.now();
    git.putEntry("folder/hello.txt", lm, 100, id);
  }

  @Test
  public void testObjectId() throws IOException {
    ObjectId id = GitObjectStore.fromString("0bfee169bb98de8c57e9c583ee30cc96b7bf829d");
    String base64 = GitObjectStore.toBase64(id);

    assertEquals("C_7habuY3oxX6cWD7jDMlre_gp0", base64);

    ObjectId id2 = GitObjectStore.fromString(base64);
    assertEquals(id, id2);
  }


  @Test
  public void testSha1() throws IOException {
    //64f831d5ea8b215522bdc2510f4bdcef35f2562f

    byte[] bytes = git.get("ZPgx1eqLIVUivcJRD0vc7zXyVi8");
    byte[] byte2 = git.get("64f831d5ea8b215522bdc2510f4bdcef35f2562f");
    assertEquals("64f831d5ea8b215522bdc2510f4bdcef35f2562f",GitUtil.gitHash(bytes));
    
    assertArrayEquals(bytes, byte2);
  }

}