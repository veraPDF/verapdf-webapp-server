package org.verapdf.webapp.jobservice.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.verapdf.webapp.jobservice.server.entity.JobTask;


@Repository
public interface JobTaskRepository extends JpaRepository<JobTask, JobTask.JobEntryId> {

}
