package uk.co.openkappa.encoding;

public interface Encoding<T> {

  int encode(T value);

  T decode(int encoding);
}
