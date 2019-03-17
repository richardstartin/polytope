package uk.co.openkappa.encoding;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ConvertibleToLongEncodingTest {

  public static Stream<LongEncoding> encodings() {
    return Stream.of(
            new ArrayLongEncoding(Object::hashCode, 2),
            new ArrayLongEncoding(Object::hashCode, 1),
            new ArrayLongEncoding(Object::hashCode, 0),
            new BufferLongEncoding(Object::hashCode, 2, 1024, ByteBuffer::allocateDirect),
            new BufferLongEncoding(Object::hashCode, 1, 1024, ByteBuffer::allocateDirect),
            new BufferLongEncoding(Object::hashCode, 0, 1024, ByteBuffer::allocateDirect),
            new BufferLongEncoding(Object::hashCode, 2, 8, ByteBuffer::allocateDirect),
            new BufferLongEncoding(Object::hashCode, 1, 8, ByteBuffer::allocateDirect),
            new BufferLongEncoding(Object::hashCode, 0, 8, ByteBuffer::allocateDirect)
    );
  }

  public static Stream<Instant> seconds() {
    return LongStream.range(1_000_000, 1_000_010)
            .mapToObj(Instant::ofEpochSecond);
  }

  public static Stream<Arguments> flatMapped() {
    return seconds().flatMap(instant -> encodings().map(encoding -> Arguments.of(instant, encoding)));
  }

  public static Stream<Arguments> nested() {
    return encodings().map(encoding -> Arguments.of(seconds(), encoding));
  }

  @ParameterizedTest
  @MethodSource("flatMapped")
  public <T extends LongEncoding & Encoding<Long>> void invertible(Instant instant, T delegate) {
    ConvertibleToLongEncoding<Instant, T> encoding = new ConvertibleToLongEncoding<>(Instant::getEpochSecond, Instant::ofEpochSecond, delegate);
    int code = encoding.encode(instant);
    Instant recovered = encoding.decode(code);
    assertEquals(instant, recovered);
  }

  @ParameterizedTest
  @MethodSource("nested")
  public <T extends LongEncoding & Encoding<Long>> void invertible(Stream<Instant> instants, T delegate) {
    ConvertibleToLongEncoding<Instant, T> encoding = new ConvertibleToLongEncoding<>(Instant::getEpochSecond, Instant::ofEpochSecond, delegate);
    instants.forEach(instant -> {
      int code = encoding.encode(instant);
      Instant recovered = encoding.decode(code);
      assertEquals(instant, recovered);
    });
  }

  @ParameterizedTest
  @MethodSource("nested")
  public <T extends LongEncoding & Encoding<Long>> void idempotent(Stream<Instant> instants, T delegate) {
    ConvertibleToLongEncoding<Instant, T> encoding = new ConvertibleToLongEncoding<>(Instant::getEpochSecond, Instant::ofEpochSecond, delegate);
    instants.forEach(instant -> {
      int code1 = encoding.encode(instant);
      int code2 = encoding.encode(instant);
      assertEquals(code1, code2);
      assertEquals(instant, encoding.decode(code1));
    });
  }

}