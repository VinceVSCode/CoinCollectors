// v0.3.0: Tests for CollectionEntry equality and hash code behavior.
package com.vincevscode.cointracker.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CollectionEntryTest {

    @Test
    void equals_shouldReturnTrueWhenEntriesHaveSameFieldValues() {
        CollectionEntry firstEntry = new CollectionEntry(1, 1, 10, 2);
        CollectionEntry secondEntry = new CollectionEntry(1, 1, 10, 2);

        assertEquals(firstEntry, secondEntry);
    }

    @Test
    void hashCode_shouldBeEqualWhenEntriesHaveSameFieldValues() {
        CollectionEntry firstEntry = new CollectionEntry(1, 1, 10, 2);
        CollectionEntry secondEntry = new CollectionEntry(1, 1, 10, 2);

        assertEquals(firstEntry.hashCode(), secondEntry.hashCode());
    }

    @Test
    void equals_shouldReturnFalseWhenEntriesHaveDifferentFieldValues() {
        CollectionEntry firstEntry = new CollectionEntry(1, 1, 10, 2);
        CollectionEntry secondEntry = new CollectionEntry(2, 1, 10, 0);

        assertNotEquals(firstEntry, secondEntry);
    }
}