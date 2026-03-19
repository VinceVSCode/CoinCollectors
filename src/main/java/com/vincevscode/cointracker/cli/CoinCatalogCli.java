package com.vincevscode.cointracker.cli;

import com.vincevscode.cointracker.model.Coin;
import com.vincevscode.cointracker.service.CoinCatalogService;

import java.util.List;
import java.util.Scanner;

public class CoinCatalogCli {
    private CoinCatalogService coinCatalogService;
    private Scanner scanner;

    public CoinCatalogCli(CoinCatalogService coinCatalogService) {
        this.coinCatalogService = coinCatalogService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;

        while (running) {
            printMenu();
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    addCoin();
                    break;
                case 2:
                    listAllCoins();
                    break;
                case 3:
                    findCoinById();
                    break;
                case 4:
                    running = false;
                    System.out.println("Exiting Coin Tracker.");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }

            System.out.println();
        }
    }

    private void printMenu() {
        System.out.println("=== Coin Tracker Menu ===");
        System.out.println("1. Add coin");
        System.out.println("2. List all coins");
        System.out.println("3. Find coin by ID");
        System.out.println("4. Exit");
        System.out.print("Choose an option: ");
    }

    private void addCoin() {
        System.out.print("Enter coin ID: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter country: ");
        String country = scanner.nextLine();

        System.out.print("Enter denomination: ");
        String denomination = scanner.nextLine();

        System.out.print("Enter year: ");
        int year = scanner.nextInt();
        scanner.nextLine();

        try {
            coinCatalogService.addCoin(id, country, denomination, year);
            System.out.println("Coin added successfully.");
        } catch (IllegalArgumentException exception) {
            System.out.println("Error: " + exception.getMessage());
        }
    }

    private void listAllCoins() {
        List<Coin> coins = coinCatalogService.getAllCoins();

        if (coins.isEmpty()) {
            System.out.println("No coins in the catalog.");
            return;
        }

        System.out.println("Coins in catalog:");
        for (Coin coin : coins) {
            System.out.println(coin);
        }
    }

    private void findCoinById() {
        System.out.print("Enter coin ID to search: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        Coin coin = coinCatalogService.findCoinById(id);

        if (coin != null) {
            System.out.println("Coin found:");
            System.out.println(coin);
        } else {
            System.out.println("Coin not found.");
        }
    }
}
