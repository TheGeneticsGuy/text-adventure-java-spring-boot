package com.aarontopping.textrpg.repository;

import com.aarontopping.textrpg.model.Weapon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WeaponRepository extends JpaRepository<Weapon, Long> {
    Optional<Weapon> findByName(String name);

    Optional<Weapon> findByType(String type); // To find a generic sword or bow - I might expand on this.
}