// v0.1.0: Minimal application entry point to verify build/run works.
package com.vincevscode.cointracker;

import java.util.List;

public class App {
    public static void main(String[] args) {
        System.out.println("Coin Tracker (v0.1.2) - Setup OK");
        CoinRepository coinRepository = new CoinRepository();

        coinRepository.addCoin(new Coin(1, "Bulgaria", "1 Lev", 2002));
        coinRepository.addCoin(new Coin(2, "Germany", "1 Euro", 2010));
        coinRepository.addCoin(new Coin(3, "France", "2 Euro", 2015));

        List<Coin> coins = coinRepository.getAllCoins();

        System.out.println("Coins in repository:");

        for (Coin coin : coins) {
            System.out.println(
                    "ID: " + coin.getId()
                            + ", Country: " + coin.getCountry()
                            + ", Denomination: " + coin.getDenomination()
                            + ", Year: " + coin.getYear()
            );
        }

    }
}