package com.franck.amiinvisible.repository;

import com.franck.amiinvisible.entity.Activite;
import com.franck.amiinvisible.entity.enums.StatutActivite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ActiviteRepository extends JpaRepository<Activite, Long> {

    Optional<Activite> findByCodeAcces(String codeAcces);

    boolean existsByCodeAcces(String codeAcces);

    List<Activite> findByAdminIdOrderByDateCreationDesc(Long adminId);

    List<Activite> findByStatutAndDateFinInscriptionBefore(StatutActivite statut, Instant instant);

    List<Activite> findByStatutAndDateFinActiviteBefore(StatutActivite statut, Instant instant);
}
