package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.Coin;

import java.util.ArrayList;
import java.util.List;

public class CoinRepository {
    private List<Coin> coins;

    public CoinRepository() {
        this.coins = new ArrayList<>();
    }

    public void addCoin(Coin coin) {
        if (findCoinById(coin.getId()) != null) {
            throw new IllegalArgumentException("Coin with Id " + coin.getId() + " already exists.");
        }
        coins.add(coin);
    }

    public List<Coin> getAllCoins() {
        // changed so it does not expose the direct list.
        return new ArrayList<>(coins);
    }

    public Coin findCoinById(int id) {
        for (Coin coin : coins) {
            if (coin.getId() == id) {
                return coin;
            }
        }
        return null;
    }

//   public void removeCoinById(int id){
//        // My implementation, a small copy of findCoinById
//        for (Coin coin: coins){
//            if (coin.getId() == id){
//                // remove the coin object from the List coins where id is a match.
//                coins.remove(id);
//                return ;
//            }
//        }
//        System.out.println(" No coin with such id was found.");
//    }
    public boolean removeCoinById(int id) {
        // AI implementation.
        Coin coinToRemove = findCoinById(id);

        if (coinToRemove == null) {
            return false;
        }

        coins.remove(coinToRemove);
        return true;
    }
}
