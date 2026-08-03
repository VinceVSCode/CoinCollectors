// v0.9.0: Admin-only REST controller for coin catalog management (create, update, delete).
package com.vincevscode.cointracker.api;

import com.vincevscode.cointracker.api.dto.CoinRequest;
import com.vincevscode.cointracker.service.CoinCatalogManagementService;
import com.vincevscode.cointracker.view.CoinCatalogView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Catalog writes, ADMIN-only via the class-level {@code @PreAuthorize} (same shape as
 * {@link AdminUserController}). Reads stay on the public-to-any-authenticated-user
 * {@link CoinCatalogController}, so this controller deliberately has no GET — the admin page
 * lists coins through {@code GET /api/coins} like every other page does.
 *
 * <p>Business rules, including the delete cascade guard, live in
 * {@link CoinCatalogManagementService}; this class only unpacks the request.
 */
@RestController
@RequestMapping("/api/admin/coins")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCoinController {
    private final CoinCatalogManagementService coinCatalogManagementService;

    public AdminCoinController(CoinCatalogManagementService coinCatalogManagementService) {
        this.coinCatalogManagementService = coinCatalogManagementService;
    }

    @PostMapping
    public ResponseEntity<CoinCatalogView> createCoin(@RequestBody CoinRequest request) {
        requireBody(request);

        CoinCatalogView created = coinCatalogManagementService.createCoin(
                request.getCountry(),
                request.getDenomination(),
                request.getYear()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{coinId}")
    public CoinCatalogView updateCoin(
            @PathVariable("coinId") int coinId,
            @RequestBody CoinRequest request
    ) {
        requireBody(request);

        return coinCatalogManagementService.updateCoin(
                coinId,
                request.getCountry(),
                request.getDenomination(),
                request.getYear()
        );
    }

    /**
     * @param force opt-in acknowledgement that deleting a coin users own also deletes their
     *              collection entries (the FK cascades). Defaults to false, so the destructive
     *              case has to be asked for explicitly.
     */
    @DeleteMapping("/{coinId}")
    public ResponseEntity<Void> deleteCoin(
            @PathVariable("coinId") int coinId,
            @RequestParam(name = "force", required = false, defaultValue = "false") boolean force
    ) {
        coinCatalogManagementService.deleteCoin(coinId, force);
        return ResponseEntity.noContent().build();
    }

    private void requireBody(CoinRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Coin details are required.");
        }
    }
}
