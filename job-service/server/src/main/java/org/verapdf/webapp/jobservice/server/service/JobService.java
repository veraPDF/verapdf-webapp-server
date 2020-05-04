package org.verapdf.webapp.jobservice.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.verapdf.webapp.error.exception.ConflictException;
import org.verapdf.webapp.error.exception.NotFoundException;
import org.verapdf.webapp.jobservice.model.dto.JobDTO;
import org.verapdf.webapp.jobservice.model.entity.enums.JobStatus;
import org.verapdf.webapp.jobservice.model.dto.JobTaskDTO;
import org.verapdf.webapp.jobservice.server.entity.Job;
import org.verapdf.webapp.jobservice.server.mapper.JobTaskMapper;
import org.verapdf.webapp.jobservice.server.mapper.JobMapper;
import org.verapdf.webapp.jobservice.server.repository.JobRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class JobService {
	private static final Logger LOGGER = LoggerFactory.getLogger(JobService.class);

	private final JobRepository jobRepository;
	private final JobMapper jobMapper;
	private final JobTaskMapper jobTaskMapper;

	public JobService(JobRepository jobRepository,
	                  JobMapper jobMapper, JobTaskMapper jobTaskMapper) {
		this.jobRepository = jobRepository;
		this.jobMapper = jobMapper;
		this.jobTaskMapper = jobTaskMapper;
	}

	@Transactional
	public JobDTO createJob(JobDTO jobDTO) {
		Job job = jobMapper.createEntityFromDTO(jobDTO);
		job = jobRepository.saveAndFlush(job);
		return jobMapper.createDTOFromEntity(job);
	}

	@Transactional
	public JobDTO getJobById(UUID jobId) throws NotFoundException {
		Job job = findJobById(jobId);
		return jobMapper.createDTOFromEntity(job);
	}

	@Transactional
	public JobDTO updateJob(UUID jobId, JobDTO jobDTO) throws NotFoundException, ConflictException {
		Job job = findJobById(jobId);

		if (job.getStatus() != JobStatus.CREATED) {
			throw new ConflictException("Cannot update already started job with specified id: " + jobId);
		}

		jobMapper.updateEntityFromDTO(job, jobDTO);

		job = jobRepository.save(job);
		return jobMapper.createDTOFromEntity(job);
	}


	private Job findJobById(UUID jobId) throws NotFoundException {
		return jobRepository.findById(jobId)
		                    .orElseThrow(() -> new NotFoundException("Job with specified id not found in DB: " + jobId));
	}

}
