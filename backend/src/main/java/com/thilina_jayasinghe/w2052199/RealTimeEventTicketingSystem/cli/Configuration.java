package com.thilina_jayasinghe.w2052199.RealTimeEventTicketingSystem.cli;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Configuration {
    private int totalTickets;
    private double ticketReleaseRate;
    private double customerRetrievalRate;
    private int maxTicketCapacity;

    static Scanner input = new Scanner(System.in);

    Configuration(int totalTickets, double ticketReleaseRate, double customerRetrievalRate, int maxTicketCapacity) {
        setTotalTickets(totalTickets);
        setTicketReleaseRate(ticketReleaseRate);
        setCustomerRetrievalRate(customerRetrievalRate);
        setMaxTicketCapacity(maxTicketCapacity);
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(int totalTickets) {
        if (
            (totalTickets > 0) && (totalTickets < 1000)
        ){
            this.totalTickets = totalTickets;
        }
    }

    public double getTicketReleaseRate() {
        return ticketReleaseRate;
    }

    public void setTicketReleaseRate(double ticketReleaseRate) {
        if (
                (ticketReleaseRate > 0.0) && (ticketReleaseRate < totalTickets)
        ){
            this.ticketReleaseRate = ticketReleaseRate;
        }
    }

    public double getCustomerRetrievalRate() {
        return customerRetrievalRate;
    }

    public void setCustomerRetrievalRate(double customerRetrievalRate) {
        if (
                (customerRetrievalRate > 0.0) && (customerRetrievalRate <= ticketReleaseRate)
        ){
            this.customerRetrievalRate = customerRetrievalRate;
        }
    }

    public int getMaxTicketCapacity() {
        return maxTicketCapacity;
    }

    public void setMaxTicketCapacity(int maxTicketCapacity) {
        if (
                (maxTicketCapacity < totalTickets) && (maxTicketCapacity > ticketReleaseRate)
        ){
            this.maxTicketCapacity = maxTicketCapacity;
        }
    }

    /**
     * Prompts input from user and initializes system configuration settings
     * Calls the method to save the configuration settings in a json file
     */
    public static void configureSystem() {
        int totTickets = 0;
        double sellRate = 0;
        double buyRate = 0;
        int maxCapacity = 0;
        boolean isConfiguring = true;
        while (isConfiguring) {
            try {
                System.out.println("Enter total number of tickets available.");
                totTickets = input.nextInt();
                input.nextLine();

                if (totTickets <= 0) {
                    System.out.println("Enter a number greater than 0");
                    continue;
                }

                System.out.println("Enter ticket release rate.");
                sellRate = input.nextDouble();
                input.nextLine();

                if ((sellRate <= 0)) {
                    System.out.println("Sell rate should be less than total tickets");
                    continue;
                }

                System.out.println("Enter ticket purchase rate of customers.");
                buyRate = input.nextDouble();
                input.nextLine();

                if ((buyRate <= 0) || (buyRate < sellRate)) {
                    System.out.println("Buy rate should be greater than or equal to sell rate.");
                    continue;
                }

                System.out.println("Enter maximum number of tickets available at any given instance.");
                maxCapacity = input.nextInt();
                input.nextLine();

                if ((maxCapacity > totTickets)) {
                    System.out.println("Maximum buffer capacity should be less than total tickets.");
                    continue;
                }


                isConfiguring = false;
            } catch (InputMismatchException exception) {
                input.nextLine();
                System.out.println("Enter valid input.");
            }
            GsonSerializer.serializeConfig(new Configuration(totTickets, buyRate, sellRate, maxCapacity));
        }
    }

}
