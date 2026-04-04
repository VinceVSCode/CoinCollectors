// v0.1.0: Minimal application entry point to verify build/run works.
// v0.2.5: Application entry point with factory-based repository selection.
package com.vincevscode.cointracker;

import com.vincevscode.cointracker.cli.CoinCatalogCli;
import com.vincevscode.cointracker.config.RepositoryFactory;
import com.vincevscode.cointracker.repository.CoinRepositoryInterface;
import com.vincevscode.cointracker.service.CoinCatalogService;

public class App {
    public static void main(String[] args) {
        CoinRepositoryInterface coinRepository = RepositoryFactory.createRepository();
        CoinCatalogService coinCatalogService = new CoinCatalogService(coinRepository);
        CoinCatalogCli coinCatalogCli = new CoinCatalogCli(coinCatalogService);

        coinCatalogCli.start();
    }
}