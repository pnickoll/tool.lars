/*******************************************************************************
 * Copyright (c) 2017 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.ibm.ws.lars.rest;

import static com.ibm.ws.lars.rest.Condition.Operation.EQUALS;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ibm.ws.lars.rest.Condition.Operation;
import com.ibm.ws.lars.rest.memorybackend.MemoryPersistor;

/**
 *
 */
public class MemoryPersistorTest {

    @Test
    public void testGetValue() {
        Map<?, ?> testMap =
                map(
                    "foo", "bar",
                    "extra", map(
                                 "abc", "xyz",
                                 "foo", "wibble"
                    ),
                    "baz", "foobar"
                );

        assertThat(getValue(testMap, "foo"), equalTo((Object) "bar"));
        assertThat(getValue(testMap, "extra.abc"), equalTo((Object) "xyz"));
        assertThat(getValue(testMap, "baz"), equalTo((Object) "foobar"));
        assertThat(getValue(testMap, "nothing"), nullValue());
    }

    @Test
    public void testSearchFinds() {
        Map<?, ?> asset1 =
                map(
                    "name", "wibble",
                    "description", "I am cool"
                );
        Map<?, ?> asset2 =
                map(
                    "name", "helper",
                    "description", "I help with the wibble"
                );
        Map<?, ?> asset3 =
                map(
                    "name", "baz",
                    "description", "I am baz"
                );

        assertThat(searchFinds(asset1, "wibble"), is(true));
        assertThat(searchFinds(asset2, "wibble"), is(true));
        assertThat(searchFinds(asset3, "wibble"), is(false));

        assertThat(searchFinds(asset1, "Wibble"), is(true)); // Case insensitive

        assertThat(searchFinds(asset1, "Wibble Baz"), is(true)); //Match any word
        assertThat(searchFinds(asset2, "Wibble Baz"), is(true));
        assertThat(searchFinds(asset3, "Wibble Baz"), is(true));

        assertThat(searchFinds(asset1, "\"the Wibble\""), is(false)); // Match quoted phrase
        assertThat(searchFinds(asset2, "\"the Wibble\""), is(true));

        assertThat(searchFinds(asset1, "wibble -help"), is(true)); // Negation
        assertThat(searchFinds(asset2, "wibble -help"), is(false));
    }

    @Test
    public void testFilterMatches() {
        Map<?, ?> testMap =
                map(
                    "foo", "bar",
                    "extra", map(
                                 "abc", "xyz",
                                 "foo", "wibble"
                    ),
                    "baz", "foobar"
                );

        assertThat(matches(testMap, filter("foo", equals("bar"))), is(true));
        assertThat(matches(testMap, filter("foo", equals("baz"))), is(false));
        assertThat(matches(testMap, filter("wibble", equals("foo"))), is(false));
        assertThat(matches(testMap, filter("extra.abc", equals("xyz"))), is(true));

    }

    private boolean matches(Map<?, ?> object, AssetFilter filter) {
        try {
            Method m = MemoryPersistor.class.getDeclaredMethod("matches", Map.class, AssetFilter.class);
            m.setAccessible(true);
            return (boolean) m.invoke(null, object, filter);
        } catch (Exception e) {
            throw new RuntimeException("Invocation failed", e);
        }
    }

    private boolean searchFinds(Map<?, ?> object, String searchString) {
        try {
            Method m = MemoryPersistor.class.getDeclaredMethod("searchFinds", Map.class, String.class);
            m.setAccessible(true);
            return (boolean) m.invoke(null, object, searchString);
        } catch (Exception e) {
            throw new RuntimeException("Invocation failed", e);
        }
    }

    private Object getValue(Map<?, ?> object, String key) {
        try {
            Method m = MemoryPersistor.class.getDeclaredMethod("getValue", Map.class, String.class);
            m.setAccessible(true);
            return m.invoke(null, object, key);
        } catch (Exception e) {
            throw new RuntimeException("Invocation failed", e);
        }
    }

    private static Map<String, Object> map(Object... content) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (content.length % 2 != 0) {
            throw new RuntimeException("Keys and values don't match up in map");
        }

        for (int i = 0; i < content.length; i += 2) {
            if (!(content[i] instanceof String)) {
                throw new RuntimeException("Key " + i + " is not a string. Value: " + content[i]);
            }
            result.put((String) content[i], content[i + 1]);
        }

        return result;
    }

    private static List<Object> list(Object... content) {
        return new ArrayList<Object>(Arrays.asList(content));
    }

    private static AssetFilter filter(String key, Condition... conditions) {
        return new AssetFilter(key, Arrays.asList(conditions));
    }

    private static Condition equals(String value) {
        return new Condition(EQUALS, value);
    }

    private static Condition notEquals(String value) {
        return new Condition(Operation.NOT_EQUALS, value);
    }

}
