// v0.1.0: Minimal application entry point to verify build/run works.
package com.vincevscode.cointracker;

import com.vincevscode.cointracker.cli.CoinCatalogCli;
import com.vincevscode.cointracker.repository.CoinRepository;
import com.vincevscode.cointracker.service.CoinCatalogService;

public class App {
    public static void main(String[] args) {
        System.out.println("Coin Tracker (v0.1.7) - A print.");
        CoinRepository coinRepository = new CoinRepository();
        CoinCatalogService coinCatalogService = new CoinCatalogService(coinRepository);
        CoinCatalogCli coinCatalogCli = new CoinCatalogCli(coinCatalogService);

        coinCatalogCli.start();
    }
}