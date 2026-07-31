package com.franck.amiinvisible.repository;

import com.franck.amiinvisible.entity.Tirage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TirageRepository extends JpaRepository<Tirage, Long> {

    List<Tirage> findByActiviteId(Long activiteId);

    Optional<Tirage> findByActiviteIdAndOffrantId(Long activiteId, Long offrantId);

    Optional<Tirage> findByActiviteIdAndDestinataireId(Long activiteId, Long destinataireId);

    void deleteByActiviteId(Long activiteId);
}
