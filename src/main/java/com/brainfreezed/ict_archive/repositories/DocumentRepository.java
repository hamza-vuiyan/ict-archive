package com.brainfreezed.ict_archive.repositories;

import com.brainfreezed.ict_archive.entities.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
}