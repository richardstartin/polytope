package uk.co.openkappa.encoding;

import java.util.function.LongFunction;
import java.util.function.ToLongFunction;

class ConvertibleToLongEncoding<T, U extends LongEncoding & Encoding<Long>> implements Encoding<T> {

  private final ToLongFunction<T> encoder;
  private final LongFunction<T> decoder;
  private final U delegate;

  ConvertibleToLongEncoding(ToLongFunction<T> encoder, LongFunction<T> decoder, U delegate) {
    this.encoder = encoder;
    this.decoder = decoder;
    this.delegate = delegate;
  }

  @Override
  public int encode(T value) {
    return delegate.encode(encoder.applyAsLong(value));
  }

  @Override
  public T decode(int encoding) {
    return decoder.apply(delegate.decodeAsLong(encoding));
  }
}
