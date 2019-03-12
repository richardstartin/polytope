package uk.co.openkappa.encoding;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.ToIntFunction;

public class UUIDEncoding extends HashEncoding<UUID> {

  private long[] values;

  UUIDEncoding(ToIntFunction<UUID> hasher, int bits) {
    super(hasher, bits);
    this.values = new long[2 * (1 << bits)];
  }

  @Override
  protected void resize(int newSize) {
    this.values = Arrays.copyOf(values, newSize * 2);
  }

  @Override
  protected void insertValue(int mark, UUID value) {
    values[2 * mark] = value.getMostSignificantBits();
    values[2 * mark + 1] = value.getLeastSignificantBits();
  }

  @Override
  public UUID decode(int encoding) {
    return new UUID(values[2 * encoding], values[2 * encoding + 1]);
  }
}
