/*
  The MIT License (MIT)

  Copyright (c) 2017 Giacomo Marciani and Michele Porretta

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:


  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.


  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
 */
package com.acmutv.moviedoop.common.util;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for {@link RecordParser}.
 *
 * @author Giacomo Marciani {@literal <gmarciani@acm.org>}
 * @author Michele Porretta {@literal <mporretta@acm.org>}
 * @since 1.0
 */
public class RecordParserTest {

  /**
   * Tests the record parsing.
   * Unquoted case.
   */
  @Test
  public void test_unquoted() {
    String attributes[] = {"key1", "key2", "key3"};
    String delimiter = RecordParser.DELIMITER;
    String line = "val1,val2,val3";

    Map<String,String> actual = RecordParser.parse(line, attributes, delimiter);
    Map<String,String> expected = new HashMap<>();
    expected.put("key1", "val1");
    expected.put("key2", "val2");
    expected.put("key3", "val3");

    Assert.assertEquals(expected, actual);
  }

  /**
   * Tests the record parsing.
   * Quoted case.
   */
  @Test
  public void test_quoted() {
    String attributes[] = {"key1", "key2", "key3"};
    String delimiter = RecordParser.ESCAPED_DELIMITER;
    String line = "val1,\"val2, val2bis, val2tris\",val3";

    Map<String,String> actual = RecordParser.parse(line, attributes, delimiter);
    Map<String,String> expected = new HashMap<>();
    expected.put("key1", "val1");
    expected.put("key2", "val2, val2bis, val2tris");
    expected.put("key3", "val3");

    Assert.assertEquals(expected, actual);
  }

  @Test
  @Ignore
  public void test_movies() throws IOException {
    BufferedReader br = Files.newBufferedReader(Paths.get("_ignore/data/movies.csv"));
    String attributes[] = {"id", "title", "genres"};
    String delimiter = RecordParser.ESCAPED_DELIMITER;
    String line;
    while ((line = br.readLine()) != null) {
      RecordParser.parse(line, attributes, delimiter);
    }
    br.close();
  }

}
