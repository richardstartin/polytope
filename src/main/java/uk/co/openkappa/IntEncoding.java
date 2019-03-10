package uk.co.openkappa;

import java.util.Arrays;
import java.util.function.ToIntFunction;

public class IntEncoding extends HashEncoding<Integer> {

  private int[] values;

  IntEncoding(ToIntFunction<Integer> hasher, int bits) {
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
    return values[encoding];
  }
}
