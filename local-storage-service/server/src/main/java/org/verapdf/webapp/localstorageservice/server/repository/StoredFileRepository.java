package org.verapdf.webapp.localstorageservice.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.verapdf.webapp.localstorageservice.server.entity.StoredFile;

import java.util.UUID;

@Repository
public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {

}
