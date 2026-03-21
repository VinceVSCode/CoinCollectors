package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.Coin;

import java.util.ArrayList;
import java.util.List;

public class InMemoryCoinRepository implements CoinRepositoryInterface{
    private List<Coin> coins;

    public InMemoryCoinRepository() {
        this.coins = new ArrayList<>();
    }

    @Override
    public void addCoin(Coin coin) {
        if (findCoinById(coin.getId()) != null) {
            throw new IllegalArgumentException("Coin with Id " + coin.getId() + " already exists.");
        }
        coins.add(coin);
    }

    @Override
    public List<Coin> getAllCoins() {
        // changed so it does not expose the direct list.
        return new ArrayList<>(coins);
    }

    @Override
    public Coin findCoinById(int id) {
        for (Coin coin : coins) {
            if (coin.getId() == id) {
                return coin;
            }
        }
        return null;
    }

    @Override
    public boolean updateCoin(Coin updatedCoin) {
        // Simple logic. If the id is the same, update the coin with the parameters.
        for(Coin coin : coins) {
            if(coin.getId() == updatedCoin.getId()) {
                coins.set(coin.getId(), updatedCoin);
                return true;
            }
        }
        return false;
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

    @Override
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
