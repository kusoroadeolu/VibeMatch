package com.victor.VibeMatch.tasteprofile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TasteProfileRepository extends JpaRepository<TasteProfile, UUID> {
}
