package uk.co.openkappa.encoding;

import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

class ConvertibleToIntEncoding<T, U extends Encoding<Integer> & IntEncoding> implements Encoding<T> {

  private final ToIntFunction<T> encoder;
  private final IntFunction<T> decoder;
  private final U delegate;

  ConvertibleToIntEncoding(ToIntFunction<T> encoder, IntFunction<T> decoder, U delegate) {
    this.encoder = encoder;
    this.decoder = decoder;
    this.delegate = delegate;
  }

  @Override
  public int encode(T value) {
    return delegate.encode(encoder.applyAsInt(value));
  }

  @Override
  public T decode(int encoding) {
    return decoder.apply(delegate.decodeAsInt(encoding));
  }
}
