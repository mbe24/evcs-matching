package org.beyene.webapp.common;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class DataTest {

    private final List<Integer> objects = Arrays.asList(0, 1, 2, 3, 8, 6);

    @Test
    public void testGetNextKeyNotFound() throws Exception {
        List<Integer> result = Data.getNext(objects, 4, x -> x);
        Assert.assertThat(result, is(equalTo(Arrays.asList(0, 1, 2, 3, 8, 6))));
    }

    @Test
    public void testGetNext() throws Exception {
        List<Integer> result = Data.getNext(objects, 3, x -> x);
        Assert.assertThat(result, is(equalTo(Arrays.asList(8, 6))));
    }

    @Test
    public void testGetNextSingle() throws Exception {
        List<Integer> result = Data.getNext(Arrays.asList(5), 5, x -> x);
        Assert.assertThat(result, is(equalTo(Collections.emptyList())));
    }

    @Test
    public void testGetNextSingleAll() throws Exception {
        List<Integer> result = Data.getNext(Arrays.asList(5), 0, x -> x);
        Assert.assertThat(result, is(equalTo(Arrays.asList(5))));
    }

    @Test
    public void testGetNextFirst() throws Exception {
        List<Integer> result = Data.getNext(objects, 0, x -> x);
        Assert.assertThat(result, is(equalTo(Arrays.asList(1, 2, 3, 8, 6))));
    }

    @Test
    public void testGetNextNoData() throws Exception {
        List<Integer> result = Data.getNext(Collections.emptyList(), 0, x -> x);
        Assert.assertThat(result, is(equalTo(Collections.emptyList())));
    }
}