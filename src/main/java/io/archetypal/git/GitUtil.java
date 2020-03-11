package io.archetypal.git;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class GitUtil {

  private static final char[] hexDigits = "0123456789abcdef".toCharArray();

  public static String bytesToHex(byte[] bytes) {
    char[] chars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      chars[j * 2] = hexDigits[v >>> 4];
      chars[j * 2 + 1] = hexDigits[v & 0x0F];
    }
    return new String(chars);
  }

  public static String gitHash(byte[] bytes) {
    final MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
      throw new Error("JVM missing SHA-1");
    }
    digest.update(("blob " + bytes.length + '\u0000').getBytes());
    digest.update(bytes);
    return bytesToHex(digest.digest());
  }

  
}