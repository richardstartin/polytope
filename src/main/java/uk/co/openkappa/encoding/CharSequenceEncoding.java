package uk.co.openkappa.encoding;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import static java.lang.Long.numberOfTrailingZeros;

public class CharSequenceEncoding extends HashEncoding<CharSequence> {

  private static final int POSITIVE = 0x7FFFFFFF;

  private int[] marks;
  private final int pageSize;
  private final List<CharBuffer> pages;
  private final IntFunction<CharBuffer> newBuffer;

  CharSequenceEncoding(ToIntFunction<CharSequence> hasher, int bits, int pageSize, IntFunction<CharBuffer> newBuffer) {
    super(hasher, bits);
    this.pageSize = pageSize;
    this.pages = new ArrayList<>();
    this.newBuffer = newBuffer;
    this.marks = new int[1 << bits];
  }

  CharSequenceEncoding(ToIntFunction<CharSequence> hasher, int pageSize, IntFunction<CharBuffer> newBuffer) {
    this (hasher,6, pageSize, newBuffer);
  }

  @Override
  protected void resize(int newSize) {
    this.marks = Arrays.copyOfRange(marks, 0, newSize);
  }

  @Override
  protected void insertValue(int mark, CharSequence value) {
    int requiredCapacity = value.length();
    assert requiredCapacity < pageSize;
    int hwm = mark == 0 ? 0 : marks[mark - 1] & POSITIVE;
    int pageIndex = (hwm + requiredCapacity) >>> numberOfTrailingZeros(pageSize);
    if (pageIndex >= pages.size()) {
      pages.add(newBuffer.apply(pageSize));
      marks[mark] = (pageIndex * pageSize + requiredCapacity) | ~POSITIVE;
    } else {
      marks[mark] = (hwm + requiredCapacity);
    }
    CharBuffer page = pages.get(pageIndex);
    for (int i = 0; i < value.length(); ++i) {
      page.put(value.charAt(i));
    }
  }

  @Override
  public CharSequence decode(int encoding) {
    return slice(encoding);
  }

  @Override
  protected boolean isMapped(CharSequence value, int encoding) {
    CharSequence seq = decode(encoding);
    if (seq.length() != value.length()) {
      return  false;
    }
    for (int i = 0; i < seq.length(); ++i) {
      if (value.charAt(i) != seq.charAt(i)) {
        return false;
      }
    }
    return true;
  }

  private CharSequence slice(int address) {
    if (address == 0) {
      return pages.get(0).asReadOnlyBuffer().position(0).limit(marks[0] & POSITIVE);
    } else if (marks[address] < 0) {
      int decodedMark = (marks[address] & POSITIVE);
      int pageIndex = decodedMark >>> numberOfTrailingZeros(pageSize);
      return pages.get(pageIndex).asReadOnlyBuffer().position(0).limit(decodedMark - (pageIndex * pageSize));
    } else {
      int pageIndex = marks[address] >>> numberOfTrailingZeros(pageSize);
      return pages.get(pageIndex)
              .asReadOnlyBuffer()
              .position(marks[address - 1] & POSITIVE - (pageIndex * pageSize))
              .limit(marks[address] - (pageIndex * pageSize));
    }
  }
}
