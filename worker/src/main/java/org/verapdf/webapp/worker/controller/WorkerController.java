package org.verapdf.webapp.worker.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/apps")
public class WorkerController {

	@GetMapping(value = "/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody String getAppsInfoFromArtifactory(@PathVariable String version) {
		RestTemplate restTemplate = new RestTemplate();
		String url = "https://artifactory.openpreservation.org/artifactory/" +
		             "api/storage/vera-dev/org/verapdf/apps/greenfield-apps/" + version;

		ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
		return responseEntity.getBody();
	}
}
