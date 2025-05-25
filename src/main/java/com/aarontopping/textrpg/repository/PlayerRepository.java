package com.aarontopping.textrpg.repository;

import com.aarontopping.textrpg.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    // JpaRepository provides CRUD methods like save(), findById(), findAll(),
    // deleteById()

    // ERROR?? - Spring Data JPA says it should auto generate query to findByName,
    // but it's throwing an
    // error in VSC so I am just adding this "optional" to eliminate the error, and
    // it handles a case where no player
    // with the name exists as well
    Optional<Player> findByName(String name);
}