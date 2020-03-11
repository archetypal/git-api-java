package io.archetypal.spark;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.net.URL;

import com.google.common.io.Files;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


public class URLTest {

  @Test
  public void testURL() throws Exception {
    URL u = new URL("https://github.com/dockcmd/aws-cli.git");
    assertEquals("/dockcmd/aws-cli.git", u.getPath());

    String fileNameWithOutExt = Files.getNameWithoutExtension(u.getPath());
    assertEquals("aws-cli", fileNameWithOutExt);
  }

  @Test
  @Disabled
  public void testFile() throws Exception {
    File f = new File("..");
    assertEquals("", f.getAbsolutePath());
  }

}