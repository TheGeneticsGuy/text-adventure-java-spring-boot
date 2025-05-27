package com.aarontopping.textrpg.repository;

import com.aarontopping.textrpg.model.WeaponTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WeaponTemplateRepository extends JpaRepository<WeaponTemplate, Long> {
    Optional<WeaponTemplate> findByName(String name);

    Optional<WeaponTemplate> findByType(String type); // To find a generic sword or bow - I might expand on this.
}