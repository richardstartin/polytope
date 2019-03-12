package uk.co.openkappa.encoding;

import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUUIDEncoding {

  @Test
  public void testUUIDEncoding() {
    int[] codes = new int[2000];
    UUID[] uuids = new UUID[2000];
    Encoding<UUID> encoding = new UUIDEncoding(UUID::hashCode, 0);
    BitSet bitSet = new BitSet();
    for (int i = 0; i < 2000; ++i) {
      uuids[i] = UUID.randomUUID();
      codes[i] = encoding.encode(uuids[i]);
      bitSet.set(codes[i]);
    }
    assertEquals(2000, bitSet.cardinality());
    for (int i = 0; i < 2000; ++i) {
      assertEquals(uuids[i], encoding.decode(codes[i]));
    }
  }
}