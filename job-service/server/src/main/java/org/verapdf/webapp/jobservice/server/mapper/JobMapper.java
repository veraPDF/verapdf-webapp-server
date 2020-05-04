package org.verapdf.webapp.jobservice.server.mapper;

import org.springframework.stereotype.Component;
import org.verapdf.webapp.jobservice.model.dto.JobDTO;
import org.verapdf.webapp.jobservice.model.dto.JobTaskDTO;
import org.verapdf.webapp.jobservice.server.entity.Job;
import org.verapdf.webapp.jobservice.server.entity.JobTask;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JobMapper {
	private final JobTaskMapper jobTaskMapper;

	public JobMapper(JobTaskMapper jobTaskMapper) {
		this.jobTaskMapper = jobTaskMapper;
	}

	public JobDTO createDTOFromEntity(Job job) {
		JobDTO res = new JobDTO();
		res.setId(job.getId());
		res.setStatus(job.getStatus());
		res.setProfile(job.getProfile());
		List<JobTask> jobTasks = job.getJobTasks();
		if (jobTasks != null && !jobTasks.isEmpty()) {
			res.setTasks(jobTasks.stream()
			                     .map(jobTaskMapper::createDTOFromEntity)
			                     .sorted(Comparator.comparing(JobTaskDTO::getFileId))
			                     .collect(Collectors.toList()));
		}
		return res;
	}

	public Job createEntityFromDTO(JobDTO jobDTO) {
		Job res = new Job();
		updateEntityFromDTO(res, jobDTO);
		return res;
	}

	public void updateEntityFromDTO(Job job, JobDTO jobDTO) {
		job.setProfile(jobDTO.getProfile());

		job.clearJobTasks();

		List<JobTaskDTO> tasks = jobDTO.getTasks();
		if (tasks != null && !tasks.isEmpty()) {
			jobDTO.getTasks().stream()
			      .map(jobTaskMapper::createEntityFromDTO)
			      .forEach(job::addTask);
		}
	}
}
