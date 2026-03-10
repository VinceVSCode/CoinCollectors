package com.vincevscode.cointracker;

import java.util.ArrayList;
import java.util.List;

public class CoinRepository {
    private List<Coin> coins;

    public CoinRepository(){
        this.coins = new ArrayList<>();
    }

    public void addCoin(Coin coin) {
        coins.add(coin);
    }

    public List<Coin> getAllCoins() {
        return coins;
    }
}
