// v0.3.0: Tests for MissingCoinView equality and hash code behavior.
package com.vincevscode.cointracker.view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MissingCoinViewTest {

    @Test
    void equals_shouldReturnTrueWhenViewsHaveSameFieldValues() {
        MissingCoinView firstView = new MissingCoinView(1, "Bulgaria", "1 Lev", 2002);
        MissingCoinView secondView = new MissingCoinView(1, "Bulgaria", "1 Lev", 2002);

        assertEquals(firstView, secondView);
    }

    @Test
    void hashCode_shouldBeEqualWhenViewsHaveSameFieldValues() {
        MissingCoinView firstView = new MissingCoinView(1, "Bulgaria", "1 Lev", 2002);
        MissingCoinView secondView = new MissingCoinView(1, "Bulgaria", "1 Lev", 2002);

        assertEquals(firstView.hashCode(), secondView.hashCode());
    }

    @Test
    void equals_shouldReturnFalseWhenViewsHaveDifferentFieldValues() {
        MissingCoinView firstView = new MissingCoinView(1, "Bulgaria", "1 Lev", 2002);
        MissingCoinView secondView = new MissingCoinView(2, "Germany", "1 Euro", 2010);

        assertNotEquals(firstView, secondView);
    }
}