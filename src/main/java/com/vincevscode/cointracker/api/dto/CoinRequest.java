// v0.9.0: Request DTO for creating/updating a catalog coin (admin operation).
package com.vincevscode.cointracker.api.dto;

/**
 * Body shape for {@code POST /api/admin/coins} and {@code PUT /api/admin/coins/{coinId}}.
 * Fields are boxed/nullable so an omitted value arrives as null and
 * {@link com.vincevscode.cointracker.service.CoinCatalogManagementService} can reject it with a
 * clear message, rather than a missing year silently defaulting to 0.
 *
 * <p>Note there's no {@code id} field: the coin id comes from the path on update and from the
 * database sequence on create, so a client can't reassign one by putting it in the body.
 */
public class CoinRequest {
    private String country;
    private String denomination;
    private Integer year;

    public CoinRequest() {
    }

    public CoinRequest(String country, String denomination, Integer year) {
        this.country = country;
        this.denomination = denomination;
        this.year = year;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDenomination() {
        return denomination;
    }

    public void setDenomination(String denomination) {
        this.denomination = denomination;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
