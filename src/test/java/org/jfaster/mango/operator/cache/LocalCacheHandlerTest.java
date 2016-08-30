/*
 * Copyright 2014 mango.jfaster.org
 *
 * The Mango Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.jfaster.mango.operator.cache;

import com.google.common.collect.Sets;
import org.jfaster.mango.util.Ticker;
import org.junit.Test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

/**
 * @author ash
 */
public class LocalCacheHandlerTest {

  @Test
  public void testGet() throws Exception {
    Ticker4Test t = new Ticker4Test();
    LocalCacheHandler cache = new LocalCacheHandler(t);
    int seconds = 100;
    String key = "key";
    String value = "value";
    cache.set(key, value, seconds);
    assertThat((String) cache.get(key), equalTo(value));
    t.addSeconds(seconds + 1);
    assertThat(cache.get(key), nullValue());
  }

  @Test
  public void testGetBulk() throws Exception {
    Ticker4Test t = new Ticker4Test();
    LocalCacheHandler cache = new LocalCacheHandler(t);
    int seconds = 100;
    String key = "key";
    String value = "value";
    int seconds2 = 200;
    String key2 = "key2";
    String value2 = "value2";
    cache.set(key, value, seconds);
    cache.set(key2, value2, seconds2);

    Set<String> keys = Sets.newHashSet(key, key2);

    Map<String, Object> map = cache.getBulk(keys);
    assertThat(map.size(), equalTo(2));
    assertThat((String) map.get(key), equalTo(value));
    assertThat((String) map.get(key2), equalTo(value2));

    t.addSeconds(seconds + 1);

    map = cache.getBulk(keys);
    assertThat(map.size(), equalTo(1));
    assertThat((String) map.get(key2), equalTo(value2));

    t.reset();
    t.addSeconds(seconds2 + 1);

    map = cache.getBulk(keys);
    assertThat(map.size(), equalTo(0));
  }

  @Test
  public void testDelete() throws Exception {
    Ticker4Test t = new Ticker4Test();
    LocalCacheHandler cache = new LocalCacheHandler(t);
    int seconds = 100;
    String key = "key";
    String value = "value";
    cache.set(key, value, seconds);
    assertThat((String) cache.get(key), equalTo(value));
    cache.delete(key);
    assertThat(cache.get(key), nullValue());
  }

  @Test
  public void testAdd() throws Exception {
    Ticker4Test t = new Ticker4Test();
    LocalCacheHandler cache = new LocalCacheHandler(t);
    int seconds = 100;
    String key = "key";
    String value = "value";
    int seconds2 = 200;
    String key2 = "key2";
    String value2 = "value2";
    cache.set(key, value, seconds);
    cache.add(key, value2, seconds);
    cache.add(key2, value2, seconds2);

    assertThat((String) cache.get(key), equalTo(value));
    assertThat((String) cache.get(key2), equalTo(value2));
  }

  private static class Ticker4Test extends Ticker {

    private long now = System.nanoTime();

    @Override
    public long read() {
      return now;
    }

    public void reset() {
      this.now = System.nanoTime();
    }

    public void addSeconds(long seconds) {
      now += TimeUnit.SECONDS.toNanos(seconds);
    }

  }

}
