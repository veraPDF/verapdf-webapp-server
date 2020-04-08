package org.verapdf.localstorageservice.server.mapper;

import org.springframework.stereotype.Component;
import org.verapdf.localstorageservice.model.dto.StoredFileDTO;
import org.verapdf.localstorageservice.server.entity.StoredFile;

@Component
public class StoredFileMapper {

	public StoredFileDTO createDTOFromEntity(StoredFile entity) {
		StoredFileDTO res = new StoredFileDTO();
		res.setId(entity.getId());
		res.setContentMD5(entity.getContentMD5());
		res.setContentType(entity.getContentType());
		res.setContentSize(entity.getContentSize());
		res.setFileName(entity.getFileName());
		return res;
	}
}
