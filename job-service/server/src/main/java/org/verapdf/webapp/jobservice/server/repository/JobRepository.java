package org.verapdf.webapp.jobservice.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import org.verapdf.webapp.jobservice.server.entity.Job;

import javax.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {
	@Override
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<Job> findById(UUID jobId);

	void deleteAllByCreatedAtLessThan(Instant createdAt);
}
