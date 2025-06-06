package com.thilina_jayasinghe.w2052199.RealTimeEventTicketingSystem.tasks;

import com.thilina_jayasinghe.w2052199.RealTimeEventTicketingSystem.model.Customer;
import com.thilina_jayasinghe.w2052199.RealTimeEventTicketingSystem.model.Ticket;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class TicketPool {

    private ConcurrentLinkedQueue<Ticket> ticketList;
    private int maxTicketCapacity;
    private int totalTickets;
    private AtomicInteger ticketCount = new AtomicInteger(0);
    private AtomicInteger unsoldTickets;
    ReentrantLock reentrantLock = new ReentrantLock();
    Condition condition = reentrantLock.newCondition();
    private static final Logger logger = Logger.getLogger(TicketPool.class.getName());
    // Stocke les messages de log en mémoire pour consultation
    private List<String> logs = new ArrayList<>();

    public TicketPool(int totalTickets, int maxTicketCapacity) {
        ticketList = new ConcurrentLinkedQueue<>();
        this.totalTickets = totalTickets;
        this.maxTicketCapacity = maxTicketCapacity;
        this.unsoldTickets = new AtomicInteger(totalTickets);
    }

    protected void addTickets(Ticket ticket) {
        try {
            reentrantLock.lock();
            while ((ticketList.size() == maxTicketCapacity) && ticketCount.get() != totalTickets) {
                logMessages("File d'attente pleine. En attente d'un consommateur...", "WARNING");
                condition.await();
                logMessages("Le vendeur a été notifié par un consommateur", "WARNING");
            }
            if (ticketCount.get() == totalTickets) {
                return;
            }
            ticketCount.incrementAndGet();
            ticket.setTicketNo(ticketCount.get());
            ticketList.add(ticket);
            logMessages(ticket.getVendor() + " a publié le billet numéro " + ticketCount, "INFO");
            condition.signalAll();
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            reentrantLock.unlock();
        }
    }

    protected Ticket removeTicket(Customer customerName, LocalDateTime timestamp) {
        try {
            reentrantLock.lock();
            condition.signalAll();
            while (ticketList.isEmpty() && unsoldTickets.get() != 0 && !Thread.currentThread().isInterrupted()) {
                condition.await();
                logMessages("File d'attente vide. En attente d'un producteur...", "WARNING");
            }
            if (unsoldTickets.get() == 0 || Thread.currentThread().isInterrupted()) {
                return null;
            }
            Ticket ticket = ticketList.peek();
            if (ticket != null) {
                ticket.setCustomer(customerName);
                ticket.setTimestamp(Timestamp.valueOf(timestamp));
                ticketList.remove();
                unsoldTickets.decrementAndGet();
                logMessages(ticket.toString(), "INFO");
            }
            condition.signalAll();
            return ticket;
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
            return null;
        } finally {
            reentrantLock.unlock();
        }
    }

    public AtomicInteger getTicketCount() {
        return ticketCount;
    }

    public void setTicketCount(AtomicInteger ticketCount) {
        this.ticketCount = ticketCount;
    }

    public AtomicInteger getUnsoldTickets() {
        return unsoldTickets;
    }

    public void setUnsoldTickets(AtomicInteger unsoldTickets) {
        this.unsoldTickets = unsoldTickets;
    }

    public Object getStatus() {
        Map<String, Object> status = new HashMap<>();

        // Validation et ajout de totalTickets
        if (totalTickets > 0) {
            status.put("totalTickets", totalTickets);
        }

        // Validation et ajout de unsoldTickets
        if (unsoldTickets != null) {
            status.put("unsoldTickets", unsoldTickets.get());
        }

        // Validation et ajout de currentQueueSize
        if (ticketList != null) {
            status.put("currentQueueSize", ticketList.size());
        }

        // Validation et ajout de maxTicketCapacity
        if (maxTicketCapacity > 0) {
            status.put("maxTicketCapacity", maxTicketCapacity);
        }

        return status;
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(int totalTickets) {
        this.totalTickets = totalTickets;
    }

    public void logMessages(String log, String level) {
        if (level.equals("INFO")) {
            logger.info(log);
        } else {
            logger.warning(log);
        }

        try {
            reentrantLock.lock();
            logs.add(log);
        } finally {
            reentrantLock.unlock();
        }
    }

    public List<String> getLogs() {
        try {
            reentrantLock.lock();
            return new ArrayList<>(logs);
        } finally {
            reentrantLock.unlock();
        }
    }

    public void clearLogs() {
        try {
            reentrantLock.lock();
            logs.clear();
        } finally {
            reentrantLock.unlock();
        }
    }
}