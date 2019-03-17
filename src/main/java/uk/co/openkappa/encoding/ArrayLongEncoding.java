package uk.co.openkappa.encoding;

import java.util.Arrays;
import java.util.function.ToIntFunction;

public class ArrayLongEncoding extends HashEncoding<Long> implements LongEncoding {

  private long[] values;

  ArrayLongEncoding(ToIntFunction<Long> hasher, int bits) {
    super(hasher, bits);
    this.values = new long[1 << bits];
  }

  @Override
  protected void resize(int newSize) {
    this.values = Arrays.copyOfRange(values, 0, newSize);
  }

  @Override
  protected void insertValue(int mark, Long value) {
    values[mark] = value;
  }

  @Override
  public Long decode(int encoding) {
    return decodeAsLong(encoding);
  }

  @Override
  public long decodeAsLong(int encoding) {
    return values[encoding];
  }
}
