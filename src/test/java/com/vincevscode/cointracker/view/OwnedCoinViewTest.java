// v0.3.0: Tests for OwnedCoinView equality and hash code behavior.
package com.vincevscode.cointracker.view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class OwnedCoinViewTest {

    @Test
    void equals_shouldReturnTrueWhenViewsHaveSameFieldValues() {
        OwnedCoinView firstView = new OwnedCoinView(1, "Bulgaria", "1 Lev", 2002, 2);
        OwnedCoinView secondView = new OwnedCoinView(1, "Bulgaria", "1 Lev", 2002, 2);

        assertEquals(firstView, secondView);
    }

    @Test
    void hashCode_shouldBeEqualWhenViewsHaveSameFieldValues() {
        OwnedCoinView firstView = new OwnedCoinView(1, "Bulgaria", "1 Lev", 2002, 2);
        OwnedCoinView secondView = new OwnedCoinView(1, "Bulgaria", "1 Lev", 2002, 2);

        assertEquals(firstView.hashCode(), secondView.hashCode());
    }

    @Test
    void equals_shouldReturnFalseWhenViewsHaveDifferentFieldValues() {
        OwnedCoinView firstView = new OwnedCoinView(1, "Bulgaria", "1 Lev", 2002, 2);
        OwnedCoinView secondView = new OwnedCoinView(2, "Germany", "1 Euro", 2010, 1);

        assertNotEquals(firstView, secondView);
    }
}