package com.op.ludo.service;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.op.ludo.exceptions.CoinServiceException;
import com.op.ludo.model.UserData;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@Slf4j
public class CoinService {

    private static final String USER_COIN_PATH = "/users/%s/coins";
    private static final String USER_PATH = "/users/%s";

    public void issueCoin(String playerId, Long numCoins) {
        FirebaseDatabase fDb = FirebaseDatabase.getInstance();
        DatabaseReference userRef = fDb.getReference(getUserCoinPath(playerId));
        userRef.runTransaction(
                new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData currentData) {
                        Long value = currentData.getValue(Long.class);
                        if (value == null) {
                            currentData.setValue(0);
                            value = currentData.getValue(Long.class);
                        }
                        currentData.setValue(value + numCoins);

                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(
                            DatabaseError error, boolean committed, DataSnapshot currentData) {
                        if (error == null) {
                            log.info(
                                    "Issued coins={} to player={}, current coins={}",
                                    numCoins,
                                    playerId,
                                    currentData.getValue(Long.class));
                        } else {
                            log.error(
                                    "failed to issue coins={} to player={}, error={}",
                                    numCoins,
                                    playerId,
                                    error.getMessage());
                        }
                    }
                });
    }

    public void deductCoins(String playerId, Long numCoins) {
        FirebaseDatabase fDb = FirebaseDatabase.getInstance();
        DatabaseReference userCoinRef = fDb.getReference(getUserCoinPath(playerId));
        userCoinRef.runTransaction(
                new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData currentData) {
                        Long value = currentData.getValue(Long.class);
                        if (value == null) {
                            currentData.setValue(0);
                        } else if (value < numCoins) {
                            log.warn(
                                    "Cannot deduct coins, insufficient balance. current={}, deduct={}",
                                    value,
                                    numCoins);
                        } else {
                            currentData.setValue(value - numCoins);
                        }

                        return Transaction.success(currentData);
                    }

                    @Override
                    public void onComplete(
                            DatabaseError error, boolean committed, DataSnapshot currentData) {
                        if (error == null) {
                            log.info(
                                    "Deducted coins={} for player={}, current coins={}",
                                    numCoins,
                                    playerId,
                                    currentData.getValue(Long.class));
                        } else {
                            log.error(
                                    "failed to deduct coins={} for player={}, error={}",
                                    numCoins,
                                    playerId,
                                    error.getMessage());
                        }
                    }
                });
    }

    public Long getCoins(String playerId) {
        FirebaseDatabase fDb = FirebaseDatabase.getInstance();
        DatabaseReference userCoinRef = fDb.getReference(getUserCoinPath(playerId));
        CompletableFuture<Long> coinFuture = new CompletableFuture<>();
        userCoinRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            coinFuture.complete(0L);
                        } else {
                            Long coins = snapshot.getValue(Long.class);
                            coinFuture.complete(coins);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        coinFuture.completeExceptionally(error.toException());
                    }
                });

        try {
            return coinFuture.get();
        } catch (InterruptedException e) {
            log.error("couldn't retrieve player={} coins", playerId);
            throw new CoinServiceException("failed to retrieve coins data", e);
        } catch (ExecutionException e) {
            log.error("failed to retrieve players={} coins", playerId, e.getCause());
            throw new CoinServiceException("failed to retrieve coins data", e);
        }
    }

    public void issueCoinForSignUp(String playerId) {
        FirebaseDatabase fDb = FirebaseDatabase.getInstance();
        DatabaseReference userRef = fDb.getReference(getUserPath(playerId));
        userRef.runTransaction(
                new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData userData) {
                        UserData info = userData.getValue(UserData.class);
                        if (ObjectUtils.isEmpty(info)) {
                            info = new UserData();
                            info.setCoins(1000L);
                            info.setSignUpDone(true);
                            userData.setValue(info);
                        } else if (!info.isSignUpDone()) {
                            info.setSignUpDone(true);
                            info.setCoins(1000L);
                            userData.setValue(info);
                        }

                        return Transaction.success(userData);
                    }

                    @Override
                    public void onComplete(
                            DatabaseError error, boolean committed, DataSnapshot currentData) {
                        if (error == null) {
                            log.info("Added signup coins={} for player={}", 1000L, playerId);
                        } else {
                            log.error(
                                    "failed to add signup coins={} for player={}, error={}",
                                    1000L,
                                    playerId,
                                    error.getMessage());
                        }
                    }
                });
    }

    private String getUserCoinPath(String playerId) {
        return String.format(USER_COIN_PATH, playerId);
    }

    private String getUserPath(String playerId) {
        return String.format(USER_PATH, playerId);
    }
}
