package com.rbi.loyaltysystem.repository;

import com.rbi.loyaltysystem.exception.TransactionNotFoundException;
import com.rbi.loyaltysystem.model.Transaction;
import com.rbi.loyaltysystem.repository.api.InMemory;
import com.rbi.loyaltysystem.repository.api.TransactionRepositoryInMemory;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TransactionRepository implements InMemory<Transaction>, TransactionRepositoryInMemory {

    List<Transaction> transactions;

    @Override
    public long add(Transaction transaction) {
        if (transactions == null){
            transactions = new ArrayList<>();
        }
        transaction.setId(transactions.size());
        transactions.add(transaction);
        return transaction.getId();
    }

    @Override
    public Transaction findById(long id) {
        if (transactions == null){
            throw new TransactionNotFoundException();
        }
        for (Transaction transaction: transactions) {
            if (transaction.getId() == id){
                return transaction;
            }
        }
        throw new TransactionNotFoundException();
    }

    @Override
    public double findSumOrderByDate(long id) {
        if (transactions == null){
            return 0;
        }
        List<Transaction> customerTransactions = findAllOrderByCustomer(id);
        if (customerTransactions.size() == 0){
            throw new TransactionNotFoundException();
        }

        return getLastWeekSpendings(customerTransactions);
    }

    @Override
    public LocalDate findTransactionOrderByDate(long id) {
        List<Transaction> customerTransactions = findAllOrderByCustomer(id);
        if (customerTransactions.size() == 0){
            throw new TransactionNotFoundException();
        }
        return customerTransactions.get(customerTransactions.size() - 1).getDate();
    }

    @Override
    public List<Transaction> findAllOrderByCustomer(long id) {
        if (transactions == null){
            return new ArrayList<>();
        }

        List<Transaction> customerTransactions = new ArrayList<>();
        for(Transaction transaction : transactions){
            if (transaction.getSenderId() == id){
                customerTransactions.add(transaction);
            }
        }
        return customerTransactions;
    }

    @Override
    public List<Transaction> findAllOrderByDate(long id) {
        List<Transaction> customerTransactions = findAllOrderByCustomer(id);
        LocalDate aWeekAgoDate = LocalDate.now().minusWeeks(1);
        List<Transaction> weeklyTransactions = new ArrayList<>();
        for (Transaction transaction : customerTransactions){
            if (aWeekAgoDate.compareTo(transaction.getDate()) >= 0){
                weeklyTransactions.add(transaction);
            }
        }
        return weeklyTransactions;
    }

    private double getLastWeekSpendings(List<Transaction> transactions){
        double spendings = 0;
        LocalDate lastWeek = LocalDate.now().minusWeeks(1);
        for (Transaction transaction : transactions) {
            if (transaction.getDate().compareTo(lastWeek) >= 0) {
                spendings += transaction.getAmount();
            }
        }
        return spendings;
    }
}
