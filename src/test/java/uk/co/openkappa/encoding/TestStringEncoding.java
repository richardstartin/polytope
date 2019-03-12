package uk.co.openkappa.encoding;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.openkappa.Misc;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.BitSet;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class TestStringEncoding {


  public static Stream<Arguments> stringEncodings() {
    return Stream.of(
            new SimpleHashEncoding<>(String::hashCode),
            new SimpleHashEncoding<>(String::hashCode, 2),
            new SimpleHashEncoding<>(String::hashCode, 1),
            new SimpleHashEncoding<>(String::hashCode, 0),
            new BufferHashEncoding<>(String::hashCode, 1024, ByteBuffer::allocateDirect, Misc::utf8, Misc::utf8FromBuffer),
            new BufferHashEncoding<>(String::hashCode, 2, 1024, ByteBuffer::allocateDirect, Misc::utf8, Misc::utf8FromBuffer),
            new BufferHashEncoding<>(String::hashCode, 1, 1024, ByteBuffer::allocateDirect, Misc::utf8, Misc::utf8FromBuffer),
            new BufferHashEncoding<>(String::hashCode, 0, 1024, ByteBuffer::allocateDirect, Misc::utf8, Misc::utf8FromBuffer),
            new BufferHashEncoding<>(String::hashCode, 64, ByteBuffer::allocateDirect, Misc::utf8, Misc::utf8FromBuffer),
            new BufferHashEncoding<>(String::hashCode, 2, 64, ByteBuffer::allocateDirect, Misc::utf8, Misc::utf8FromBuffer),
            new BufferHashEncoding<>(String::hashCode, 1, 64, ByteBuffer::allocateDirect, Misc::utf8, Misc::utf8FromBuffer),
            new BufferHashEncoding<>(String::hashCode, 0, 64, ByteBuffer::allocateDirect, Misc::utf8, Misc::utf8FromBuffer),
            new CharSequenceEncoding(Misc::hashCode, 1024, CharBuffer::allocate),
            new CharSequenceEncoding(Misc::hashCode, 2, 1024, CharBuffer::allocate),
            new CharSequenceEncoding(Misc::hashCode, 1, 1024, CharBuffer::allocate),
            new CharSequenceEncoding(Misc::hashCode, 0,1024, CharBuffer::allocate),
            new CharSequenceEncoding(Misc::hashCode, 1024, size -> ByteBuffer.allocateDirect(size * 2).asCharBuffer()),
            new CharSequenceEncoding(Misc::hashCode, 2, 1024, size -> ByteBuffer.allocateDirect(size * 2).asCharBuffer()),
            new CharSequenceEncoding(Misc::hashCode, 1, 1024, size -> ByteBuffer.allocateDirect(size * 2).asCharBuffer()),
            new CharSequenceEncoding(Misc::hashCode, 0,1024, size -> ByteBuffer.allocateDirect(size * 2).asCharBuffer())
    ).map(Arguments::of);
  }


  @ParameterizedTest
  @MethodSource("stringEncodings")
  void idempotent(Encoding<CharSequence> encoding) {
    int k1 = encoding.encode("foo");
    int k2 = encoding.encode("foo");
    assertEquals(k1, k2);
  }

  @ParameterizedTest
  @MethodSource("stringEncodings")
  void distinct(Encoding<CharSequence> encoding) {
    int k1 = encoding.encode("cosi fan tutte");
    int k2 = encoding.encode("fidelio");
    assertNotSame(k1, k2);
    assertEquals("cosi fan tutte", encoding.decode(k1).toString());
    assertEquals("fidelio", encoding.decode(k2).toString());
  }

  @ParameterizedTest
  @MethodSource("stringEncodings")
  void distinctWithHashCodeCollisionSameLength(Encoding<CharSequence> encoding) {
    int k1 = encoding.encode("\"~");
    int k2 = encoding.encode("#_");
    assertNotSame(k1, k2);
    assertEquals("\"~", encoding.decode(k1).toString());
    assertEquals("#_", encoding.decode(k2).toString());
  }

  @ParameterizedTest
  @MethodSource("stringEncodings")
  void multiPages(Encoding<CharSequence> encoding) {
    int[] codes = new int[2000];
    String[] values = new String[2000];
    BitSet bits = new BitSet();
    for (int i = 0; i < 2000; ++i) {
      values[i] = UUID.randomUUID().toString();
      codes[i] = encoding.encode(values[i]);
      bits.set(codes[i]);
    }
    assertEquals(2000, bits.cardinality());
    for (int i = 0; i < 2000; ++i) {
      assertEquals(values[i], encoding.decode(codes[i]).toString());
    }
  }

}
