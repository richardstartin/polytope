package uk.co.openkappa.encoding;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestIntEncoding {

  public static Stream<Arguments> encodings() {
    return Stream.of(
            Arguments.of(IntStream.range(0, 1_000_000), new ArrayIntEncoding(Object::hashCode, 2)),
            Arguments.of(IntStream.range(0, 1_000_000), new ArrayIntEncoding(Object::hashCode, 1)),
            Arguments.of(IntStream.range(0, 1_000_000), new ArrayIntEncoding(Object::hashCode, 0)),
            Arguments.of(IntStream.range(0, 1_000_000), new BufferIntEncoding(Object::hashCode, 2, 1024, ByteBuffer::allocateDirect)),
            Arguments.of(IntStream.range(0, 1_000_000), new BufferIntEncoding(Object::hashCode, 1, 1024, ByteBuffer::allocateDirect)),
            Arguments.of(IntStream.range(0, 1_000_000), new BufferIntEncoding(Object::hashCode, 0, 1024, ByteBuffer::allocateDirect)),
            Arguments.of(IntStream.range(0, 1_000_000), new BufferIntEncoding(Object::hashCode, 2, 8, ByteBuffer::allocateDirect)),
            Arguments.of(IntStream.range(0, 1_000_000), new BufferIntEncoding(Object::hashCode, 1, 8, ByteBuffer::allocateDirect)),
            Arguments.of(IntStream.range(0, 1_000_000), new BufferIntEncoding(Object::hashCode, 0, 8, ByteBuffer::allocateDirect)),
            Arguments.of(IntStream.range(0, 30).map(i -> 1 << i), new ArrayIntEncoding(Object::hashCode, 10)),
            Arguments.of(IntStream.range(0, 30).map(i -> 1 << i), new ArrayIntEncoding(Object::hashCode, 10)),
            Arguments.of(IntStream.range(0, 30).map(i -> 1 << i), new ArrayIntEncoding(Object::hashCode, 10)),
            Arguments.of(IntStream.range(0, 30).map(i -> 1 << i), new BufferIntEncoding(Object::hashCode, 10, 1024, ByteBuffer::allocateDirect)),
            Arguments.of(IntStream.range(0, 30).map(i -> 1 << i), new BufferIntEncoding(Object::hashCode, 10, 1024, ByteBuffer::allocateDirect)),
            Arguments.of(IntStream.range(0, 30).map(i -> 1 << i), new BufferIntEncoding(Object::hashCode, 10, 1024, ByteBuffer::allocateDirect)),
            Arguments.of(IntStream.range(0, 30).map(i -> 1 << i), new BufferIntEncoding(Object::hashCode, 10, 8, ByteBuffer::allocateDirect)),
            Arguments.of(IntStream.range(0, 30).map(i -> 1 << i), new BufferIntEncoding(Object::hashCode, 10, 8, ByteBuffer::allocateDirect)),
            Arguments.of(IntStream.range(0, 30).map(i -> 1 << i), new BufferIntEncoding(Object::hashCode, 10, 8, ByteBuffer::allocateDirect))
    );
  }

  @ParameterizedTest
  @MethodSource("encodings")
  void idempotent(IntStream values, Encoding<Integer> encoding) {
    values.forEach(i -> {
      int k1 = encoding.encode(i);
      int k2 = encoding.encode(i);
      assertEquals(k1, k2);
    });
  }

  @ParameterizedTest
  @MethodSource("encodings")
  void testAgainstHashMap(IntStream values, Encoding<Integer> encoding) {
    Map<Integer, Integer> reference = new HashMap<>();
    AtomicInteger mark = new AtomicInteger();
    values.forEach(i -> {
      encoding.encode(i);
      reference.put(i, mark.getAndIncrement());
    });

    reference.forEach((l, i) -> {
      assertEquals(l, encoding.decode(i));
      assertEquals(i.intValue(), encoding.encode(l));
    });

  }
}
