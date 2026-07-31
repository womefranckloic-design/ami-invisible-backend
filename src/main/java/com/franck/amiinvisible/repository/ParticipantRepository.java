package com.franck.amiinvisible.repository;

import com.franck.amiinvisible.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    List<Participant> findByActiviteIdOrderByDateInscriptionAsc(Long activiteId);

    long countByActiviteId(Long activiteId);

    boolean existsByIdentifiantAnonyme(String identifiantAnonyme);

    Optional<Participant> findByActiviteIdAndCodeSecret(Long activiteId, String codeSecret);

    Optional<Participant> findByIdAndActiviteId(Long id, Long activiteId);

    List<Participant> findByCompteParticipantIdOrderByDateInscriptionDesc(Long compteParticipantId);
}
