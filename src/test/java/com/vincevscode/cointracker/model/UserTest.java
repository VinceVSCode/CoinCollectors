// v0.3.0: Tests for User equality and hash code behavior.
package com.vincevscode.cointracker.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class UserTest {

    @Test
    void equals_shouldReturnTrueWhenUsersHaveSameFieldValues() {
        User firstUser = new User(1, "vince");
        User secondUser = new User(1, "vince");

        assertEquals(firstUser, secondUser);
    }

    @Test
    void hashCode_shouldBeEqualWhenUsersHaveSameFieldValues() {
        User firstUser = new User(1, "vince");
        User secondUser = new User(1, "vince");

        assertEquals(firstUser.hashCode(), secondUser.hashCode());
    }

    @Test
    void equals_shouldReturnFalseWhenUsersHaveDifferentFieldValues() {
        User firstUser = new User(1, "vince");
        User secondUser = new User(2, "alex");

        assertNotEquals(firstUser, secondUser);
    }
}