package uk.co.openkappa.encoding;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEnumEncoding {

  public enum TestEnum {
    FOO, BAR
  }

  @Test
  public void testEnumEncoding() {
    Encoding<TestEnum> encoding = new EnumEncoding<>(TestEnum.class);
    for (TestEnum testEnum : TestEnum.values()) {
      assertEquals(testEnum.ordinal(), encoding.encode(testEnum));
      assertEquals(testEnum, encoding.decode(testEnum.ordinal()));
    }
  }

}
