package com.aarontopping.textrpg.repository;

import com.aarontopping.textrpg.model.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    Optional<GameSession> findByPlayerId(Long playerId); // Find an existing ongoing game

    // ERROR?? - Spring Data JPA says it should auto generate query to findByName,
    // but it's throwing an
    // error in VSC so I am just adding this "optional" to eliminate the error, and
    // it handles a case where no player
    // with the name exists as well
    Optional<GameSession> findByPlayerIdAndGameOver(Long playerId, boolean gameOver);

    List<GameSession> findAllByPlayerId(Long playerId);
}