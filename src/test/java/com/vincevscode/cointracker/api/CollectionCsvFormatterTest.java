// v0.8.0: Unit tests for CSV rendering of collection exports.
package com.vincevscode.cointracker.api;

import com.vincevscode.cointracker.view.OwnedCoinView;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionCsvFormatterTest {

    @Test
    void toCsv_shouldWriteHeaderAndRows() {
        String csv = CollectionCsvFormatter.toCsv(List.of(
                new OwnedCoinView(1, "Bulgaria", "1 Lev", 2002, 2),
                new OwnedCoinView(2, "Germany", "1 Euro", 2010, 1)
        ));

        assertEquals(
                "Coin ID,Country,Denomination,Year,Quantity\r\n"
                        + "1,Bulgaria,1 Lev,2002,2\r\n"
                        + "2,Germany,1 Euro,2010,1\r\n",
                csv
        );
    }

    @Test
    void toCsv_shouldWriteHeaderOnlyWhenCollectionIsEmpty() {
        assertEquals("Coin ID,Country,Denomination,Year,Quantity\r\n", CollectionCsvFormatter.toCsv(List.of()));
    }

    @Test
    void toCsv_shouldQuoteFieldsContainingCommas() {
        String csv = CollectionCsvFormatter.toCsv(List.of(
                new OwnedCoinView(1, "Congo, Republic of the", "1 Franc", 1990, 1)
        ));

        assertTrue(csv.contains("1,\"Congo, Republic of the\",1 Franc,1990,1"), csv);
    }

    @Test
    void toCsv_shouldDoubleEmbeddedQuotes() {
        String csv = CollectionCsvFormatter.toCsv(List.of(
                new OwnedCoinView(1, "Testland", "The \"Big\" One", 1990, 1)
        ));

        assertTrue(csv.contains("1,Testland,\"The \"\"Big\"\" One\",1990,1"), csv);
    }

    @Test
    void toCsv_shouldQuoteFieldsContainingNewlines() {
        String csv = CollectionCsvFormatter.toCsv(List.of(
                new OwnedCoinView(1, "Line\nBreak", "1 Unit", 1990, 1)
        ));

        assertTrue(csv.contains("\"Line\nBreak\""), csv);
    }

    // A spreadsheet would evaluate a cell starting with = + - @ as a formula, so the export
    // prefixes those with an apostrophe to force text. Nothing user-controlled reaches the
    // catalog today, but the export shouldn't become an injection vector when that changes.
    @Test
    void toCsv_shouldNeutralizeLeadingFormulaCharacters() {
        String csv = CollectionCsvFormatter.toCsv(List.of(
                new OwnedCoinView(1, "=1+1", "+SUM(A1)", 1990, 1),
                new OwnedCoinView(2, "-2", "@import", 1991, 1)
        ));

        assertTrue(csv.contains("1,'=1+1,'+SUM(A1),1990,1"), csv);
        assertTrue(csv.contains("2,'-2,'@import,1991,1"), csv);
    }

    @Test
    void toCsv_shouldRenderNullTextAsEmptyField() {
        String csv = CollectionCsvFormatter.toCsv(List.of(
                new OwnedCoinView(1, null, null, 1990, 1)
        ));

        assertTrue(csv.contains("1,,,1990,1"), csv);
    }
}
