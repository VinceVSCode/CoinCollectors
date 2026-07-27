// v0.3.3: Supported sort directions for screen-oriented queries.
package com.vincevscode.cointracker.query;

// Shared across all three sortable screens (catalog/owned/missing) — repository code appends
// this enum's .name() literally after ORDER BY <field>, so only ASC/DESC can ever appear there.
public enum SortDirection {
    ASC,
    DESC
}