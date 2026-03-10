// v0.1.0: Minimal JUnit test to verify test runner works.
package com.vincevscode.cointracker;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {

    @Test
    void sanityCheck() {
        // This test verifies that the test runner is working.
        System.out.print ("Sanity is checked!");
        assertTrue(true);
    }
}