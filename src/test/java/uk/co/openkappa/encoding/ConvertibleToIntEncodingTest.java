package uk.co.openkappa.encoding;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.ByteBuffer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConvertibleToIntEncodingTest {

  public static Stream<IntEncoding> encodings() {
    return Stream.of(
            new ArrayIntEncoding(Object::hashCode, 2),
            new ArrayIntEncoding(Object::hashCode, 1),
            new ArrayIntEncoding(Object::hashCode, 0),
            new BufferIntEncoding(Object::hashCode, 2, 1024, ByteBuffer::allocateDirect),
            new BufferIntEncoding(Object::hashCode, 1, 1024, ByteBuffer::allocateDirect),
            new BufferIntEncoding(Object::hashCode, 0, 1024, ByteBuffer::allocateDirect),
            new BufferIntEncoding(Object::hashCode, 2, 8, ByteBuffer::allocateDirect),
            new BufferIntEncoding(Object::hashCode, 1, 8, ByteBuffer::allocateDirect),
            new BufferIntEncoding(Object::hashCode, 0, 8, ByteBuffer::allocateDirect)
    );
  }

  public static Stream<byte[]> bytes() {
    return IntStream.range(0, 30)
            .map(i -> 1 << i)
            .mapToObj(ConvertibleToIntEncodingTest::unpack);
  }

  public static Stream<Arguments> flatMapped() {
    return bytes().flatMap(arr -> encodings().map(encoding -> Arguments.of(arr, encoding)));
  }

  public static Stream<Arguments> nested() {
    return encodings().map(encoding -> Arguments.of(bytes(), encoding));
  }

  @ParameterizedTest
  @MethodSource("flatMapped")
  public <T extends IntEncoding & Encoding<Integer>> void invertible(byte[] arr, T delegate) {
    ConvertibleToIntEncoding<byte[], T> encoding = new ConvertibleToIntEncoding<>(ConvertibleToIntEncodingTest::pack,
            ConvertibleToIntEncodingTest::unpack, delegate);
    int code = encoding.encode(arr);
    byte[] recovered = encoding.decode(code);
    assertArrayEquals(arr, recovered);
  }

  @ParameterizedTest
  @MethodSource("nested")
  public <T extends IntEncoding & Encoding<Integer>> void invertible(Stream<byte[]> arrays, T delegate) {
    ConvertibleToIntEncoding<byte[], T> encoding = new ConvertibleToIntEncoding<>(ConvertibleToIntEncodingTest::pack,
            ConvertibleToIntEncodingTest::unpack, delegate);
    arrays.forEach(array -> {
      int code = encoding.encode(array);
      byte[] recovered = encoding.decode(code);
      assertArrayEquals(array, recovered);
    });
  }

  @ParameterizedTest
  @MethodSource("nested")
  public <T extends IntEncoding & Encoding<Integer>> void idempotent(Stream<byte[]> arrays, T delegate) {
    ConvertibleToIntEncoding<byte[], T> encoding = new ConvertibleToIntEncoding<>(ConvertibleToIntEncodingTest::pack,
            ConvertibleToIntEncodingTest::unpack, delegate);
    arrays.forEach(array -> {
      int code1 = encoding.encode(array);
      int code2 = encoding.encode(array);
      assertEquals(code1, code2);
      assertArrayEquals(array, encoding.decode(code1));
    });
  }
  static int pack(byte[] arr) {
    return ((arr[0] & 0xFF) << 24) | ((arr[1] & 0xFF) << 16) | ((arr[2] & 0xFF) << 8) | (arr[3] & 0xFF);
  }

  static byte[] unpack(int i) {
    return new byte[]{
            (byte) ((i >>> 24) & 0xFF),
            (byte) ((i >>> 16) & 0xFF),
            (byte) ((i >>> 8) & 0xFF),
            (byte) (i & 0xFF)
    };
  }

}
