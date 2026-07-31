package com.franck.amiinvisible.repository;

import com.franck.amiinvisible.entity.CompteParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompteParticipantRepository extends JpaRepository<CompteParticipant, Long> {
    Optional<CompteParticipant> findByEmail(String email);
    boolean existsByEmail(String email);
}
