package uk.co.openkappa.encoding;

import java.util.Arrays;
import java.util.function.ToIntFunction;

public class ArrayIntEncoding extends HashEncoding<Integer> implements IntEncoding {

  private int[] values;

  ArrayIntEncoding(ToIntFunction<Integer> hasher, int bits) {
    super(hasher, bits);
    this.values = new int[1 << bits];
  }

  @Override
  protected void resize(int newSize) {
    this.values = Arrays.copyOf(values, newSize);
  }

  @Override
  protected void insertValue(int mark, Integer value) {
    values[mark] = value;
  }

  @Override
  public Integer decode(int encoding) {
    return decodeAsInt(encoding);
  }

  @Override
  public int decodeAsInt(int encoding) {
    return values[encoding];
  }
}
