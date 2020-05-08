package org.verapdf.webapp.jobservice.server.mapper;

import org.springframework.stereotype.Component;
import org.verapdf.webapp.jobservice.model.dto.JobTaskDTO;
import org.verapdf.webapp.jobservice.server.entity.JobTask;

@Component
public class JobTaskMapper {

	public JobTaskDTO createDTOFromEntity(JobTask jobTask) {
		JobTaskDTO res = new JobTaskDTO();
		res.setFileId(jobTask.getFileId());
		res.setStatus(jobTask.getStatus());
		res.setErrorType(jobTask.getErrorType());
		res.setErrorMessage(jobTask.getErrorMessage());
		res.setValidationResultId(jobTask.getResultFileId());
		return res;
	}

	public JobTask createEntityFromDTO(JobTaskDTO jobTaskDTO) {
		JobTask res = new JobTask();
		res.setFileId(jobTaskDTO.getFileId());
		return res;
	}
}
