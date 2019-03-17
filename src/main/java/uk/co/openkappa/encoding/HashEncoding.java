package uk.co.openkappa.encoding;

import java.util.Arrays;
import java.util.function.ToIntFunction;

public abstract class HashEncoding<T> implements Encoding<T> {

  static final int MISSING = -1; //0xC0000000;

  private final ToIntFunction<T> hasher;
  private int[] hashes;
  private int[] buckets;
  private int mark = 0;

  HashEncoding(ToIntFunction<T> hasher, int bits) {
    assert bits < 31;
    this.hasher = hasher;
    this.hashes = new int[1 << bits];
    this.buckets = new int[1 << bits];
    Arrays.fill(buckets, MISSING);
  }

  @Override
  public int encode(T value) {
    int hash = hasher.applyAsInt(value);
    int bucket = hash & (buckets.length - 1);
    int encoding = encode(hash, bucket, value);
    while (encoding == MISSING) {
      bucket = (bucket + 1) & (buckets.length - 1);
      encoding = encode(hash, bucket, value);
    }
    return encoding;
  }

  protected boolean isMapped(T value, int encoding) {
    return value.equals(decode(encoding));
  }

  protected void resize(int newSize) { }

  protected abstract void insertValue(int mark, T value);

  private int encode(int hash, int bucket, T value) {
    int encoding = buckets[bucket];
    if (encoding == MISSING) {
      ensureCapacity();
      insertValue(mark, value);
      hashes[mark] = hash;
      buckets[bucket] = mark;
      return mark++;
    } else if (hashes[encoding] == hash && isMapped(value, encoding)) {
      return encoding;
    }
    if (isFull()) {
      rehash();
      return encode(hash, hash & (buckets.length - 1), value);
    }
    return MISSING;
  }

  private void ensureCapacity() {
    if (isFull()) {
      rehash();
    }
  }

  private boolean isFull() {
    return mark == buckets.length;
  }

  private void rehash() {
    int newSize = buckets.length << 1;
    resize(newSize);
    int[] rebucketed = new int[newSize];
    Arrays.fill(rebucketed, MISSING);
    int newMask = rebucketed.length - 1;
    for (int i = 0; i < buckets.length; ++i) {
      int hash = hashes[i];
      int encoding = buckets[i];
      int pos = hash & newMask;
      while (true) {
        if (rebucketed[pos] == MISSING) {
          rebucketed[pos] = encoding;
          break;
        }
        pos = (pos + 1) & newMask;
      }
    }
    buckets = rebucketed;
    hashes = Arrays.copyOf(hashes, newSize);
  }
}
