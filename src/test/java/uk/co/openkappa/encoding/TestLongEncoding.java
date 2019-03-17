package uk.co.openkappa.encoding;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestLongEncoding {

  public static Stream<Arguments> encodings() {
    return Stream.of(
            Arguments.of(LongStream.range(0, 1_000_000), new ArrayLongEncoding(Object::hashCode, 2)),
            Arguments.of(LongStream.range(0, 1_000_000), new ArrayLongEncoding(Object::hashCode, 1)),
            Arguments.of(LongStream.range(0, 1_000_000), new ArrayLongEncoding(Object::hashCode, 0)),
            Arguments.of(LongStream.range(0, 1_000_000), new BufferLongEncoding(Object::hashCode, 2, 1024, ByteBuffer::allocateDirect)),
            Arguments.of(LongStream.range(0, 1_000_000), new BufferLongEncoding(Object::hashCode, 1, 1024, ByteBuffer::allocateDirect)),
            Arguments.of(LongStream.range(0, 1_000_000), new BufferLongEncoding(Object::hashCode, 0, 1024, ByteBuffer::allocateDirect)),
            Arguments.of(LongStream.range(0, 1_000_000), new BufferLongEncoding(Object::hashCode, 2, 8, ByteBuffer::allocateDirect)),
            Arguments.of(LongStream.range(0, 1_000_000), new BufferLongEncoding(Object::hashCode, 1, 8, ByteBuffer::allocateDirect)),
            Arguments.of(LongStream.range(0, 1_000_000), new BufferLongEncoding(Object::hashCode, 0, 8, ByteBuffer::allocateDirect))
    );
  }

  @ParameterizedTest
  @MethodSource("encodings")
  void idempotent(LongStream values, Encoding<Long> encoding) {
    values.forEach(i -> {
      int k1 = encoding.encode(i);
      int k2 = encoding.encode(i);
      assertEquals(k1, k2);
    });
  }

  @ParameterizedTest
  @MethodSource("encodings")
  void testAgainstHashMap(LongStream values, Encoding<Long> encoding) {
    Map<Long, Integer> reference = new HashMap<>();
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
