package uk.co.openkappa.encoding;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import static java.lang.Integer.numberOfTrailingZeros;

public class BufferLongEncoding extends HashEncoding<Long> implements LongEncoding {

  private final IntFunction<ByteBuffer> newBuffer;
  private final List<ByteBuffer> pages;
  private final int pageSize;

  BufferLongEncoding(ToIntFunction<Long> hasher, int bits, int pageSize, IntFunction<ByteBuffer> newBuffer) {
    super(hasher, bits);
    assert (pageSize & (pageSize - 1)) == 0;
    this.pageSize = pageSize;
    this.newBuffer = newBuffer;
    this.pages = new ArrayList<>();
  }

  @Override
  protected boolean isMapped(Long value, int encoding) {
    int valuesPerPage = pageSize >>> 3;
    int pageIndex = encoding >>> numberOfTrailingZeros(valuesPerPage);
    int valueIndex = (encoding & (valuesPerPage - 1)) << 3;
    return pages.get(pageIndex).getLong(valueIndex) == value;
  }

  @Override
  protected void insertValue(int mark, Long value) {
    int valuesPerPage = pageSize >>> 3;
    int pageIndex = mark >>> numberOfTrailingZeros(valuesPerPage);
    if (pageIndex >= pages.size()) {
      pages.add(newBuffer.apply(pageSize));
    }
    int valueIndex = (mark & (valuesPerPage - 1)) << 3;
    pages.get(pageIndex).putLong(valueIndex, value);
  }

  @Override
  public Long decode(int encoding) {
    return decodeAsLong(encoding);
  }

  @Override
  public long decodeAsLong(int encoding) {
    int valuesPerPage = pageSize >>> 3;
    int pageIndex = encoding >>> numberOfTrailingZeros(valuesPerPage);
    int valueIndex = (encoding & (valuesPerPage - 1)) << 3;
    return pages.get(pageIndex).getLong(valueIndex);
  }
}
