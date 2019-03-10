package uk.co.openkappa;

public interface Encoding<T> {

  int encode(T value);

  T decode(int encoding);
}
