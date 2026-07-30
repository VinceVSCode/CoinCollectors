// v0.8.0: RFC 4180 CSV rendering for collection exports.
package com.vincevscode.cointracker.api;

import com.vincevscode.cointracker.view.OwnedCoinView;

import java.util.List;

/**
 * Renders a user's owned coins as CSV for {@code GET /api/users/{userId}/collection/export}.
 *
 * <p>Stateless and static rather than a Spring bean: it holds no collaborators, so wiring it
 * through {@code ApplicationConfiguration} (where the services are declared) would add
 * indirection without buying anything.
 */
public final class CollectionCsvFormatter {
    private static final String HEADER = "Coin ID,Country,Denomination,Year,Quantity";

    // RFC 4180 specifies CRLF between records. Excel and LibreOffice both accept bare LF, but
    // emitting CRLF keeps the output spec-correct for stricter parsers.
    private static final String LINE_SEPARATOR = "\r\n";

    private CollectionCsvFormatter() {
    }

    public static String toCsv(List<OwnedCoinView> ownedCoins) {
        StringBuilder csv = new StringBuilder(HEADER);

        for (OwnedCoinView coin : ownedCoins) {
            csv.append(LINE_SEPARATOR)
                    .append(coin.getCoinId())
                    .append(',')
                    .append(escapeField(coin.getCountry()))
                    .append(',')
                    .append(escapeField(coin.getDenomination()))
                    .append(',')
                    .append(coin.getYear())
                    .append(',')
                    .append(coin.getQuantity());
        }

        // Trailing newline so the file ends on a record boundary; parsers treat a final empty
        // line as absent rather than as a blank record.
        return csv.append(LINE_SEPARATOR).toString();
    }

    private static String escapeField(String value) {
        if (value == null) {
            return "";
        }

        String field = neutralizeFormula(value);

        // RFC 4180: a field containing a comma, quote, CR or LF must be quoted, and any quote
        // inside it doubled. Fields without those characters are emitted bare.
        if (field.indexOf(',') < 0
                && field.indexOf('"') < 0
                && field.indexOf('\r') < 0
                && field.indexOf('\n') < 0) {
            return field;
        }

        return '"' + field.replace("\"", "\"\"") + '"';
    }

    /**
     * Guards against CSV injection: a spreadsheet treats a cell starting with {@code = + - @} (or
     * a leading tab/CR) as a formula, so an exported value could execute on open. Prefixing with
     * an apostrophe forces the cell to be read as text.
     *
     * <p>Catalog text is admin-managed today, so nothing user-controlled currently reaches this
     * — but the export would silently become an attack vector the moment that changes (the same
     * reasoning behind the HTML escaping on the collection page).
     */
    private static String neutralizeFormula(String value) {
        if (value.isEmpty()) {
            return value;
        }

        char first = value.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r') {
            return "'" + value;
        }

        return value;
    }
}
