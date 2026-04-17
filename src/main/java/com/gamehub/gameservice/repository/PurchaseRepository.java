package com.gamehub.gameservice.repository;

import com.gamehub.gameservice.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByPlayerId(Long playerId);
}
