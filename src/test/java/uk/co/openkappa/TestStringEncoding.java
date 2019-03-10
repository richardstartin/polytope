package uk.co.openkappa;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
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
            new BufferHashEncoding<>(String::hashCode, 16, ByteBuffer::allocateDirect, Misc::utf8, Misc::utf8FromBuffer),
            new BufferHashEncoding<>(String::hashCode, 2, 16, ByteBuffer::allocateDirect, Misc::utf8, Misc::utf8FromBuffer),
            new BufferHashEncoding<>(String::hashCode, 1, 16, ByteBuffer::allocateDirect, Misc::utf8, Misc::utf8FromBuffer),
            new BufferHashEncoding<>(String::hashCode, 0, 16, ByteBuffer::allocateDirect, Misc::utf8, Misc::utf8FromBuffer),
            new CharSequenceEncoding(Misc::hashCode, 1024, CharBuffer::allocate),
            new CharSequenceEncoding(Misc::hashCode, 2, 1024, CharBuffer::allocate),
            new CharSequenceEncoding(Misc::hashCode, 1, 1024, CharBuffer::allocate),
            new CharSequenceEncoding(Misc::hashCode, 0,1024, CharBuffer::allocate),
            new CharSequenceEncoding(Misc::hashCode, 1024, size -> ByteBuffer.allocateDirect(size).asCharBuffer()),
            new CharSequenceEncoding(Misc::hashCode, 2, 1024, size -> ByteBuffer.allocateDirect(size).asCharBuffer()),
            new CharSequenceEncoding(Misc::hashCode, 1, 1024, size -> ByteBuffer.allocateDirect(size).asCharBuffer()),
            new CharSequenceEncoding(Misc::hashCode, 0,1024, size -> ByteBuffer.allocateDirect(size).asCharBuffer())
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

}
