// v0.7.0: Tests for CollectionProgressView equality and hash code behavior.
package com.vincevscode.cointracker.view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CollectionProgressViewTest {

    @Test
    void equals_shouldReturnTrueWhenViewsHaveSameFieldValues() {
        CollectionProgressView firstView = new CollectionProgressView(1, 6, 2, 4, 33.3);
        CollectionProgressView secondView = new CollectionProgressView(1, 6, 2, 4, 33.3);

        assertEquals(firstView, secondView);
    }

    @Test
    void hashCode_shouldBeEqualWhenViewsHaveSameFieldValues() {
        CollectionProgressView firstView = new CollectionProgressView(1, 6, 2, 4, 33.3);
        CollectionProgressView secondView = new CollectionProgressView(1, 6, 2, 4, 33.3);

        assertEquals(firstView.hashCode(), secondView.hashCode());
    }

    @Test
    void equals_shouldReturnFalseWhenViewsHaveDifferentFieldValues() {
        CollectionProgressView firstView = new CollectionProgressView(1, 6, 2, 4, 33.3);
        CollectionProgressView secondView = new CollectionProgressView(1, 6, 3, 3, 50.0);

        assertNotEquals(firstView, secondView);
    }
}
