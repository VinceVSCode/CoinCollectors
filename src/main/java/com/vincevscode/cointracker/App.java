// v0.1.0: Minimal application entry point to verify build/run works.
// v0.2.0: Application entry point for testing PostgreSQL-backed coin storage.
package com.vincevscode.cointracker;

import com.vincevscode.cointracker.model.Coin;
import com.vincevscode.cointracker.repository.PostgresCoinRepository;
import com.vincevscode.cointracker.service.CoinCatalogService;

public class App {
    public static void main(String[] args) {
        PostgresCoinRepository coinRepository = new PostgresCoinRepository();
        CoinCatalogService coinCatalogService = new CoinCatalogService(coinRepository);

        coinCatalogService.addCoin(1, "Bulgaria", "1 Lev", 2002);
        coinCatalogService.addCoin(2, "Germany", "1 Euro", 2010);

        System.out.println("Coins stored in PostgreSQL:");
        for (Coin coin : coinCatalogService.getAllCoins()) {
            System.out.println(coin);
        }
    }
}