package uk.co.openkappa.encoding;

import java.util.Arrays;
import java.util.function.ToIntFunction;

public class SimpleHashEncoding<T> extends HashEncoding<T> {

  private T[] values;

  public SimpleHashEncoding(ToIntFunction<T> hasher) {
    this(hasher, 6);
  }

  public SimpleHashEncoding(ToIntFunction<T> hasher, int bits) {
    super(hasher, bits);
    this.values = (T[])new Object[1 << bits];
  }

  @Override
  protected void resize(int newSize) {
    values = Arrays.copyOfRange(values, 0, newSize);
  }

  @Override
  protected void insertValue(int mark, T value) {
    assert mark < values.length;
    values[mark] = value;
  }

  @Override
  public T decode(int encoding) {
    return values[encoding];
  }
}
