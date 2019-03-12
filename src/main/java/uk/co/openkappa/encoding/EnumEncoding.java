package uk.co.openkappa.encoding;

public class EnumEncoding<E extends Enum<E>> implements Encoding<E> {

  private final E[] values;

  EnumEncoding(Class<E> type) {
    this.values = type.getEnumConstants();
  }

  @Override
  public int encode(E value) {
    return value.ordinal();
  }

  @Override
  public E decode(int encoding) {
    return values[encoding];
  }
}
