package uk.co.openkappa.encoding;

import java.util.Arrays;
import java.util.function.ToIntFunction;

public abstract class HashEncoding<T> implements Encoding<T> {

  static final int MISSING = 0xC0000000;

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
    ensureCapacity();
    int mask = buckets.length - 1;
    int hash = hasher.applyAsInt(value);
    int bucket = hash & mask;
    int encoding = encode(hash, bucket, value);
    while (encoding == MISSING) {
      bucket = (bucket + 1) & mask;
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
      insertValue(mark, value);
      hashes[mark] = hash;
      buckets[bucket] = mark;
      return mark++;
    } else if (hashes[encoding] == hash && isMapped(value, encoding)) {
      return encoding;
    }
    return MISSING;
  }

  private void ensureCapacity() {
    if (mark == buckets.length) {
      int newSize = buckets.length << 1;
      resize(newSize);
      int[] rebucketed = new int[newSize];
      Arrays.fill(rebucketed, MISSING);
      int oldMask = buckets.length - 1;
      int newMask = rebucketed.length - 1;
      for (int hash : hashes) {
        int pos = hash & newMask;
        int encoding = buckets[hash & oldMask];
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
}
