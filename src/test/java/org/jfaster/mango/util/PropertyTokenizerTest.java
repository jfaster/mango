package org.jfaster.mango.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class PropertyTokenizerTest {

    @Test
    public void test() throws Exception {
        PropertyTokenizer prop = new PropertyTokenizer("a.b.c");
        assertThat(prop.getName(), equalTo("a"));
        assertThat(prop.getChildren(), equalTo("b.c"));

        PropertyTokenizer prop2 = new PropertyTokenizer("a");
        assertThat(prop2.getName(), equalTo("a"));
        assertThat(prop2.getChildren(), nullValue());
    }

}
