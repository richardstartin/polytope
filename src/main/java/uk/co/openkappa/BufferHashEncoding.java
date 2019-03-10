package uk.co.openkappa;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import static java.lang.Long.numberOfTrailingZeros;

public class BufferHashEncoding<T> extends HashEncoding<T> {

  private static final int POSITIVE = 0x7FFFFFFF;
  private int[] marks;
  private final Function<T, byte[]> serialiser;
  private final IntFunction<ByteBuffer> newBuffer;
  private final Function<ByteBuffer, T> deserialiser;
  private final int pageSize;
  private List<ByteBuffer> pages;

  public BufferHashEncoding(ToIntFunction<T> hasher,
                            int pageSize,
                            IntFunction<ByteBuffer> newBuffer,
                            Function<T, byte[]> serialiser,
                            Function<ByteBuffer, T> deserialiser) {
    this(hasher, 6, pageSize, newBuffer, serialiser, deserialiser);
  }

  public BufferHashEncoding(ToIntFunction<T> hasher,
                            int bits,
                            int pageSize,
                            IntFunction<ByteBuffer> newBuffer,
                            Function<T, byte[]> serialiser,
                            Function<ByteBuffer, T> deserialiser) {
    super(hasher, bits);
    assert (pageSize & (pageSize -1)) == 0;
    this.serialiser = serialiser;
    this.newBuffer = newBuffer;
    this.deserialiser = deserialiser;
    this.pageSize = pageSize;
    this.marks = new int[1 << bits];
    this.pages = new ArrayList<>();
  }

  @Override
  public T decode(int encoding) {
    return deserialiser.apply(slice(encoding));
  }

  @Override
  protected void resize(int newSize) {
    this.marks = Arrays.copyOfRange(marks, 0, newSize);
  }

  @Override
  protected void insertValue(int mark, T value) {
    byte[] data = serialiser.apply(value);
    int requiredCapacity = data.length;
    assert requiredCapacity <= pageSize;
    int hwm = mark == 0 ? 0 : marks[mark - 1] & POSITIVE;
    int pageIndex = (hwm + requiredCapacity) >>> numberOfTrailingZeros(pageSize);
    int tag = 0;
    if (pageIndex >= pages.size()) {
      pages.add(newBuffer.apply(pageSize));
      tag = ~POSITIVE;
    }
    marks[mark] = (hwm + requiredCapacity) | tag;
    pages.get(pageIndex).put(data);
  }

  private ByteBuffer slice(int address) {
    if (address == 0) {
      return pages.get(0).asReadOnlyBuffer().position(0).limit(marks[0] & POSITIVE);
    } else if (marks[address] < 0) {
      int decodedMark = (marks[address] & POSITIVE);
      int pageIndex = decodedMark >>> numberOfTrailingZeros(pageSize);
      return pages.get(pageIndex).asReadOnlyBuffer().position(0).limit(decodedMark - (marks[address -1] & POSITIVE));
    } else {
      int pageIndex = marks[address] >>> numberOfTrailingZeros(pageSize);
      return pages.get(pageIndex).asReadOnlyBuffer().position(marks[address - 1] & POSITIVE).limit(marks[address]);
    }
  }
}
