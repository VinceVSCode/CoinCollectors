// v0.1.0: Minimal application entry point to verify build/run works.
package com.vincevscode.cointracker;

import com.vincevscode.cointracker.model.Coin;
import com.vincevscode.cointracker.repository.CoinRepository;

public class App {
    public static void main(String[] args) {
        System.out.println("Coin Tracker (v0.1.2) - Setup OK");
        CoinRepository coinRepository = new CoinRepository();

        coinRepository.addCoin(new Coin(1, "Bulgaria", "1 Lev", 2002));
        coinRepository.addCoin(new Coin(2, "Germany", "1 Euro", 2010));
        coinRepository.addCoin(new Coin(3, "France", "2 Euro", 2015));

        System.out.println("Coins in repository:");

        for (Coin coin : coinRepository.getAllCoins()) {
            System.out.println(coin);
        }

        System.out.println();

        boolean removed = coinRepository.removeCoinById(2);

        if (removed) {
            System.out.println("Coin with ID 2 removed successfully.");
        } else {
            System.out.println("Coin with ID 2 was not found.");
        }

        System.out.println();
        System.out.println("All coins after removal:");
        for (Coin coin : coinRepository.getAllCoins()) {
            System.out.println(coin);
        }
    }
}