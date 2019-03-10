package uk.co.openkappa;

import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Misc {

  public static String utf8FromBuffer(ByteBuffer buffer) {
    byte[] arr = new byte[buffer.limit() - buffer.position()];
    buffer.get(arr);
    return new String(arr, UTF_8);
  }

  public static byte[] utf8(String str) {
    return str.getBytes(UTF_8);
  }

  public static int hashCode(CharSequence seq) {
    if (seq instanceof String) {
      return seq.hashCode();
    }
    int hash = 0;
    for (int i = 0; i < seq.length(); ++i) {
      hash = hash * 31 + seq.charAt(i);
    }
    return hash;
  }
}
