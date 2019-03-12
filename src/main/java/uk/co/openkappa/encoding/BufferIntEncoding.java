package uk.co.openkappa.encoding;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import static java.lang.Integer.numberOfTrailingZeros;

public class BufferIntEncoding extends HashEncoding<Integer> {

  private final IntFunction<ByteBuffer> newBuffer;
  private final List<ByteBuffer> pages;
  private final int pageSize;

  BufferIntEncoding(ToIntFunction<Integer> hasher, int bits, int pageSize, IntFunction<ByteBuffer> newBuffer) {
    super(hasher, bits);
    assert (pageSize & (pageSize - 1)) == 0;
    this.pageSize = pageSize;
    this.newBuffer = newBuffer;
    this.pages = new ArrayList<>();
  }

  @Override
  protected boolean isMapped(Integer value, int encoding) {
    int valuesPerPage = pageSize >>> 2;
    int pageIndex = encoding >>> numberOfTrailingZeros(valuesPerPage);
    int valueIndex = (encoding & (valuesPerPage - 1)) << 2;
    return pages.get(pageIndex).getInt(valueIndex) == value;
  }

  @Override
  protected void insertValue(int mark, Integer value) {
    int valuesPerPage = pageSize >>> 2;
    int pageIndex = mark >>> numberOfTrailingZeros(valuesPerPage);
    if (pageIndex >= pages.size()) {
      pages.add(newBuffer.apply(pageSize));
    }
    int valueIndex = (mark & (valuesPerPage - 1)) << 2;
    pages.get(pageIndex).putInt(valueIndex, value);
  }

  @Override
  public Integer decode(int encoding) {
    int valuesPerPage = pageSize >>> 2;
    int pageIndex = encoding >>> numberOfTrailingZeros(valuesPerPage);
    int valueIndex = (encoding & (valuesPerPage - 1)) << 2;
    return pages.get(pageIndex).getInt(valueIndex);
  }
}
