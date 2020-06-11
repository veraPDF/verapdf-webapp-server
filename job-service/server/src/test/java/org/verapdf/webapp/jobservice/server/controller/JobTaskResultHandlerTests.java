package org.verapdf.webapp.jobservice.server.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.verapdf.webapp.jobservice.model.entity.enums.Profile;
import org.verapdf.webapp.jobservice.server.entity.Job;
import org.verapdf.webapp.jobservice.server.repository.JobRepository;
import org.verapdf.webapp.jobservice.server.repository.JobTaskRepository;
import org.verapdf.webapp.jobservice.server.service.JobTaskResultHandler;
import org.verapdf.webapp.queueclient.entity.QueueErrorEventType;
import org.verapdf.webapp.queueclient.entity.SendingToQueueErrorData;
import org.verapdf.webapp.queueclient.listener.QueueListener;
import org.verapdf.webapp.queueclient.sender.QueueSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class JobTaskResultHandlerTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private JobTaskRepository jobTaskRepository;

	@Autowired
	private JobTaskResultHandler jobTaskResultHandler;

	@MockBean
	private QueueSender queueSender;

	@MockBean
	private QueueListener queueListener;

	@BeforeEach
	public void cleanJobsAndTasks() {
		jobRepository.deleteAll();
		jobTaskRepository.deleteAll();
	}

	@Test
	public void invalidMessageOnHandleMessageTest() {
		jobTaskResultHandler.handleMessage("Invalid message");

		assertDBEmpty();
	}

	@Test
	public void nullFileIdOnHandleMessageTest() {
		jobTaskResultHandler.handleMessage(
				"{\"jobId\":\"774bd16b-7ad5-354e-808e-5dc731c73963\","
						+ "\"validationResultId\":\"d39da7b6-3665-4374-84e2-70c1e24e7029\"}");

		assertDBEmpty();
	}

	@Test
	public void nullJobIdOnHandleMessageTest() {
		jobTaskResultHandler.handleMessage(
				"{\"jobId\":null,"
						+ "\"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\","
						+ "\"validationResultId\":\"d39da7b6-3665-4374-84e2-70c1e24e7029\"}");

		assertDBEmpty();
	}

	@Test
	public void jobDoesNotExistInDatabaseOnHandleMessageTest() {
		jobTaskResultHandler.handleMessage(
				"{\"jobId\":\"774bd16b-7ad5-354e-808e-5dc731c73963\","
						+ "\"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\","
						+ "\"validationResultId\":\"d39da7b6-3665-4374-84e2-70c1e24e7029\"}");

		assertDBEmpty();
	}

	@Test
	public void taskDoesNotExistInDatabaseOnHandleMessageTest() {
		Job job = new Job(Profile.TAGGED_PDF);
		job = jobRepository.saveAndFlush(job);

		jobTaskResultHandler.handleMessage(
				"{\"jobId\":\"" + job.getId() + "\","
						+ "\"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\","
						+ "\"validationResultId\":\"d39da7b6-3665-4374-84e2-70c1e24e7029\"}");

		Assertions.assertEquals(1, jobRepository.count());
		Assertions.assertEquals(0, jobTaskRepository.count());
	}

	@Test
	public void theSameTaskWithValidationResultAndProcessingError() throws Exception {
		String uploadedJobId = createStartGetJobWithTasksAndReturnJobId();

		jobTaskResultHandler.handleMessage(
				"{\"jobId\":\"" + uploadedJobId + "\","
						+ "\"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\","
						+ "\"validationResultId\":\"d39da7b6-3665-4374-84e2-70c1e24e7029\"}");

		//Getting job
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json("{'id':'" + uploadedJobId + "'," +
						"'profile':'TAGGED_PDF'," +
						"'status':'PROCESSING'," +
						"'tasks':[{" +
						"'fileId' : '534bd16b-6bd5-404e-808e-5dc731c73963'," +
						"'status':'FINISHED'," +
						"'validationResultId':'d39da7b6-3665-4374-84e2-70c1e24e7029'" +
						"}," +
						"{" +
						"'fileId' : '774bd16b-7ad5-354e-808e-5dc731c73963'," +
						"'status':'PROCESSING'" +
						"}" +
						"]}", true));


		jobTaskResultHandler.handleMessage(
				"{\"jobId\":\"" + uploadedJobId + "\","
						+ "\"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\","
						+ "\"errorType\":\"FILE_OBTAINING_TO_PROCESS_ERROR\","
						+ "\"errorMessage\":\"no body\"}");

		//Getting job
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json("{'id':'" + uploadedJobId + "'," +
						"'profile':'TAGGED_PDF'," +
						"'status':'PROCESSING'," +
						"'tasks':[{" +
						"'fileId' : '534bd16b-6bd5-404e-808e-5dc731c73963'," +
						"'status':'ERROR'," +
						"'errorType':'FILE_OBTAINING_TO_PROCESS_ERROR'," +
						"'errorMessage':'no body'" +
						"}," +
						"{" +
						"'fileId' : '774bd16b-7ad5-354e-808e-5dc731c73963'," +
						"'status':'PROCESSING'" +
						"}" +
						"]}", true));
	}

	@Test
	public void theSameTaskWithSendingErrorAndValidationResult() throws Exception {
		String uploadedJobId = createStartGetJobWithTasksAndReturnJobId();

		String message = "{\"jobId\":\"" + uploadedJobId + "\","
				+ "\"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\","
				+ "\"profile\":\"TAGGED_PDF\"}";
		SendingToQueueErrorData sendingToQueueErrorData
				= new SendingToQueueErrorData("queueName", message,
				QueueErrorEventType.SENDING_ERROR_CALLBACK, "cause", null);
		jobTaskResultHandler.handleEvent(sendingToQueueErrorData);
		String responseMessage = "Message: " + message + " cannot be send" +
				" into the queue \\'queueName\\', cause: cause";

		//Getting job
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json("{'id':'" + uploadedJobId + "'," +
						"'profile':'TAGGED_PDF'," +
						"'status':'PROCESSING'," +
						"'tasks':[{" +
						"'fileId':'534bd16b-6bd5-404e-808e-5dc731c73963'," +
						"'status':'ERROR'," +
						"'errorType':'SENDING_TO_QUEUE_ERROR'," +
						"'errorMessage':'" + responseMessage + "'" +
						"}," +
						"{" +
						"'fileId' : '774bd16b-7ad5-354e-808e-5dc731c73963'," +
						"'status':'PROCESSING'" +
						"}" +
						"]}", true));

		jobTaskResultHandler.handleMessage(
				"{\"jobId\":\"" + uploadedJobId + "\","
						+ "\"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\","
						+ "\"validationResultId\":\"d39da7b6-3665-4374-84e2-70c1e24e7029\"}");

		//Getting job
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json("{'id':'" + uploadedJobId + "'," +
						"'profile':'TAGGED_PDF'," +
						"'status':'PROCESSING'," +
						"'tasks':[{" +
						"'fileId' : '534bd16b-6bd5-404e-808e-5dc731c73963'," +
						"'status':'FINISHED'," +
						"'validationResultId':'d39da7b6-3665-4374-84e2-70c1e24e7029'" +
						"}," +
						"{" +
						"'fileId' : '774bd16b-7ad5-354e-808e-5dc731c73963'," +
						"'status':'PROCESSING'" +
						"}" +
						"]}", true));
	}

	@Test
	public void invalidMessageOnHandleEventTest() {
		String message = "Invalid message";
		SendingToQueueErrorData sendingToQueueErrorData
				= new SendingToQueueErrorData("queueName", message,
				QueueErrorEventType.SENDING_ERROR_CALLBACK, "cause", null);
		jobTaskResultHandler.handleEvent(sendingToQueueErrorData);

		assertDBEmpty();
	}

	@Test
	public void nullFileIdOnHandleEventTest() {
		String message = "{\"jobId\":\"774bd16b-7ad5-354e-808e-5dc731c73963\","
				+ "\"fileId\":null,"
				+ "\"validationResultId\":\"d39da7b6-3665-4374-84e2-70c1e24e7029\"}";
		SendingToQueueErrorData sendingToQueueErrorData
				= new SendingToQueueErrorData("queueName", message,
				QueueErrorEventType.SENDING_ERROR_CALLBACK, "cause", null);
		jobTaskResultHandler.handleEvent(sendingToQueueErrorData);

		assertDBEmpty();
	}

	@Test
	public void nullJobIdOnHandleEventTest() {
		String message = "{\"jobId\":null,"
				+ "\"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\","
				+ "\"validationResultId\":\"d39da7b6-3665-4374-84e2-70c1e24e7029\"}";
		SendingToQueueErrorData sendingToQueueErrorData
				= new SendingToQueueErrorData("queueName", message,
				QueueErrorEventType.SENDING_ERROR_CALLBACK, "cause", null);
		jobTaskResultHandler.handleEvent(sendingToQueueErrorData);

		assertDBEmpty();
	}

	@Test
	public void jobDoesNotExistInDatabaseOnHandleEventTest() {
		String message = "{\"jobId\":\"774bd16b-7ad5-354e-808e-5dc731c73963\","
				+ "\"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\","
				+ "\"validationResultId\":\"d39da7b6-3665-4374-84e2-70c1e24e7029\"}";
		SendingToQueueErrorData sendingToQueueErrorData
				= new SendingToQueueErrorData("queueName", message,
				QueueErrorEventType.SENDING_ERROR_CALLBACK, "cause", null);
		jobTaskResultHandler.handleEvent(sendingToQueueErrorData);

		assertDBEmpty();
	}

	@Test
	public void taskDoesNotExistInDatabaseOnHandleEventTest() {
		Job job = new Job(Profile.TAGGED_PDF);
		job = jobRepository.saveAndFlush(job);

		String message = "{\"jobId\":\"" + job.getId() + "\","
				+ "\"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\","
				+ "\"validationResultId\":\"d39da7b6-3665-4374-84e2-70c1e24e7029\"}";
		SendingToQueueErrorData sendingToQueueErrorData
				= new SendingToQueueErrorData("queueName", message,
				QueueErrorEventType.SENDING_ERROR_CALLBACK, "cause", null);
		jobTaskResultHandler.handleEvent(sendingToQueueErrorData);

		Assertions.assertEquals(1, jobRepository.count());
		Assertions.assertEquals(0, jobTaskRepository.count());
	}

	@Test
	public void createStartAndGetExecutedJobWithTasksTest() throws Exception {
		String uploadedJobId = createStartGetJobWithTasksAndReturnJobId();

		jobTaskResultHandler.handleMessage(
				"{\"jobId\":\"" + uploadedJobId + "\","
						+ "\"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\","
						+ "\"validationResultId\":\"d39da7b6-3665-4374-84e2-70c1e24e7029\"}");


		jobTaskResultHandler.handleMessage(
				"{\"jobId\":\"" + uploadedJobId + "\","
						+ "\"fileId\":\"774bd16b-7ad5-354e-808e-5dc731c73963\","
						+ "\"validationResultId\":\"d39da7b6-3665-4374-84e2-70c1e24e7029\"}");

		//Getting job
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json("{'id':'" + uploadedJobId + "'," +
						"'profile':'TAGGED_PDF'," +
						"'status':'FINISHED'," +
						"'tasks':[{" +
						"'fileId' : '534bd16b-6bd5-404e-808e-5dc731c73963'," +
						"'status':'FINISHED'," +
						"'validationResultId':'d39da7b6-3665-4374-84e2-70c1e24e7029'" +
						"}," +
						"{" +
						"'fileId' : '774bd16b-7ad5-354e-808e-5dc731c73963'," +
						"'status':'FINISHED'," +
						"'validationResultId':'d39da7b6-3665-4374-84e2-70c1e24e7029'" +
						"}" +
						"]}", true));
	}

	@Test
	public void createStartAndGetJobWithExecutedSingleTasksTest() throws Exception {
		String uploadedJobId = createStartGetJobWithTasksAndReturnJobId();

		jobTaskResultHandler.handleMessage(
				"{\"jobId\":\"" + uploadedJobId + "\","
						+ "\"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\","
						+ "\"validationResultId\":\"d39da7b6-3665-4374-84e2-70c1e24e7029\"}");

		//Getting job
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json("{'id':'" + uploadedJobId + "'," +
						"'profile':'TAGGED_PDF'," +
						"'status':'PROCESSING'," +
						"'tasks':[{" +
						"'fileId' : '534bd16b-6bd5-404e-808e-5dc731c73963'," +
						"'status':'FINISHED'," +
						"'validationResultId':'d39da7b6-3665-4374-84e2-70c1e24e7029'" +
						"}," +
						"{" +
						"'fileId' : '774bd16b-7ad5-354e-808e-5dc731c73963'," +
						"'status':'PROCESSING'" +
						"}" +
						"]}", true));
	}

	@Test
	public void createStartAndGetExecutedJobWithTasksWithTasksProcessingErrorsTest() throws Exception {
		String uploadedJobId = createStartGetJobWithTasksAndReturnJobId();

		jobTaskResultHandler.handleMessage(
				"{\"jobId\":\"" + uploadedJobId + "\","
						+ "\"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\","
						+ "\"errorType\":\"FILE_OBTAINING_TO_PROCESS_ERROR\","
						+ "\"errorMessage\":\"no body\"}");


		jobTaskResultHandler.handleMessage(
				"{\"jobId\":\"" + uploadedJobId + "\","
						+ "\"fileId\":\"774bd16b-7ad5-354e-808e-5dc731c73963\","
						+ "\"errorType\":\"PROCESSING_INTERNAL_ERROR\","
						+ "\"errorMessage\":\"some message\"}");

		//Getting job
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json("{'id':'" + uploadedJobId + "'," +
						"'profile':'TAGGED_PDF'," +
						"'status':'FINISHED'," +
						"'tasks':[{" +
						"'fileId' : '534bd16b-6bd5-404e-808e-5dc731c73963'," +
						"'status':'ERROR'," +
						"'errorType':'FILE_OBTAINING_TO_PROCESS_ERROR'," +
						"'errorMessage':'no body'" +
						"}," +
						"{" +
						"'fileId' : '774bd16b-7ad5-354e-808e-5dc731c73963'," +
						"'status':'ERROR'," +
						"'errorType':'PROCESSING_INTERNAL_ERROR'," +
						"'errorMessage':'some message'" +
						"}" +
						"]}", true));
	}

	@Test
	public void createStartAndGetExecutedJobWithTasksWithTaskProcessingErrorAndWithoutErrorTest() throws Exception {
		String uploadedJobId = createStartGetJobWithTasksAndReturnJobId();

		jobTaskResultHandler.handleMessage(
				"{\"jobId\":\"" + uploadedJobId + "\","
						+ "\"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\","
						+ "\"validationResultId\":\"d39da7b6-3665-4374-84e2-70c1e24e7029\"}");

		jobTaskResultHandler.handleMessage(
				"{\"jobId\":\"" + uploadedJobId + "\","
						+ "\"fileId\":\"774bd16b-7ad5-354e-808e-5dc731c73963\","
						+ "\"errorType\":\"FILE_OBTAINING_TO_PROCESS_ERROR\","
						+ "\"errorMessage\":\"no body\"}");

		//Getting job
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json("{'id':'" + uploadedJobId + "'," +
						"'profile':'TAGGED_PDF'," +
						"'status':'FINISHED'," +
						"'tasks':[{" +
						"'fileId' : '534bd16b-6bd5-404e-808e-5dc731c73963'," +
						"'status':'FINISHED'," +
						"'validationResultId':'d39da7b6-3665-4374-84e2-70c1e24e7029'" +
						"}," +
						"{" +
						"'fileId' : '774bd16b-7ad5-354e-808e-5dc731c73963'," +
						"'status':'ERROR'," +
						"'errorType':'FILE_OBTAINING_TO_PROCESS_ERROR'," +
						"'errorMessage':'no body'" +
						"}" +
						"]}", true));
	}

	@Test
	public void createStartAndGetExecutedJobWithTasksWithSingleTaskProcessingErrorTest() throws Exception {
		String uploadedJobId = createStartGetJobWithTasksAndReturnJobId();

		jobTaskResultHandler.handleMessage(
				"{\"jobId\":\"" + uploadedJobId + "\","
						+ "\"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\","
						+ "\"errorType\":\"FILE_OBTAINING_TO_PROCESS_ERROR\","
						+ "\"errorMessage\":\"no body\"}");

		//Getting job
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json("{'id':'" + uploadedJobId + "'," +
						"'profile':'TAGGED_PDF'," +
						"'status':'PROCESSING'," +
						"'tasks':[{" +
						"'fileId' : '534bd16b-6bd5-404e-808e-5dc731c73963'," +
						"'status':'ERROR'," +
						"'errorType':'FILE_OBTAINING_TO_PROCESS_ERROR'," +
						"'errorMessage':'no body'" +
						"}," +
						"{" +
						"'fileId' : '774bd16b-7ad5-354e-808e-5dc731c73963'," +
						"'status':'PROCESSING'" +
						"}" +
						"]}", true));
	}

	@Test
	public void createStartAndGetExecutedJobWithTasksWithSendingErrorsTest() throws Exception {
		String uploadedJobId = createStartGetJobWithTasksAndReturnJobId();

		String message1 = "{\"jobId\":\"" + uploadedJobId + "\","
				+ "\"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\","
				+ "\"profile\":\"TAGGED_PDF\"}";
		SendingToQueueErrorData sendingToQueueErrorData1
				= new SendingToQueueErrorData("queueName", message1,
				QueueErrorEventType.SENDING_ERROR_CALLBACK, "cause", null);
		jobTaskResultHandler.handleEvent(sendingToQueueErrorData1);

		String message2 = "{\"jobId\":\"" + uploadedJobId + "\","
				+ "\"fileId\":\"774bd16b-7ad5-354e-808e-5dc731c73963\","
				+ "\"profile\":\"TAGGED_PDF\"}";
		SendingToQueueErrorData sendingToQueueErrorData2
				= new SendingToQueueErrorData("queueName", message2,
				QueueErrorEventType.SENDING_ERROR_CALLBACK, "cause", null);
		jobTaskResultHandler.handleEvent(sendingToQueueErrorData2);

		String responseMessage1 = "Message: " + message1 + " cannot be send" +
				" into the queue \\'queueName\\', cause: cause";
		String responseMessage2 = "Message: " + message2 + " cannot be send" +
				" into the queue \\'queueName\\', cause: cause";

		//Getting job
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json("{'id':'" + uploadedJobId + "'," +
						"'profile':'TAGGED_PDF'," +
						"'status':'FINISHED'," +
						"'tasks':[{" +
						"'fileId':'534bd16b-6bd5-404e-808e-5dc731c73963'," +
						"'status':'ERROR'," +
						"'errorType':'SENDING_TO_QUEUE_ERROR'," +
						"'errorMessage':'" + responseMessage1 + "'" +
						"}," +
						"{" +
						"'fileId':'774bd16b-7ad5-354e-808e-5dc731c73963'," +
						"'status':'ERROR'," +
						"'errorType':'SENDING_TO_QUEUE_ERROR'," +
						"'errorMessage':'" + responseMessage2 + "'" +
						"}" +
						"]}", true));
	}

	@Test
	public void createStartAndGetExecutedJobWithTasksWithSendingErrorAndWithoutErrorTest() throws Exception {
		String uploadedJobId = createStartGetJobWithTasksAndReturnJobId();

		String message = "{\"jobId\":\"" + uploadedJobId + "\","
				+ "\"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\","
				+ "\"profile\":\"TAGGED_PDF\"}";
		SendingToQueueErrorData sendingToQueueErrorData
				= new SendingToQueueErrorData("queueName", message,
				QueueErrorEventType.SENDING_ERROR_CALLBACK, "cause", null);
		jobTaskResultHandler.handleEvent(sendingToQueueErrorData);
		String responseMessage = "Message: " + message + " cannot be send" +
				" into the queue \\'queueName\\', cause: cause";

		jobTaskResultHandler.handleMessage(
				"{\"jobId\":\"" + uploadedJobId + "\","
						+ "\"fileId\":\"774bd16b-7ad5-354e-808e-5dc731c73963\","
						+ "\"validationResultId\":\"d39da7b6-3665-4374-84e2-70c1e24e7029\"}");

		//Getting job
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json("{'id':'" + uploadedJobId + "'," +
						"'profile':'TAGGED_PDF'," +
						"'status':'FINISHED'," +
						"'tasks':[{" +
						"'fileId':'534bd16b-6bd5-404e-808e-5dc731c73963'," +
						"'status':'ERROR'," +
						"'errorType':'SENDING_TO_QUEUE_ERROR'," +
						"'errorMessage':'" + responseMessage + "'" +
						"}," +
						"{" +
						"'fileId' : '774bd16b-7ad5-354e-808e-5dc731c73963'," +
						"'status':'FINISHED'," +
						"'validationResultId':'d39da7b6-3665-4374-84e2-70c1e24e7029'" +
						"}" +
						"]}", true));
	}

	@Test
	public void createStartAndGetExecutedJobWithTasksWithSingleSendingErrorTest() throws Exception {
		String uploadedJobId = createStartGetJobWithTasksAndReturnJobId();

		String message = "{\"jobId\":\"" + uploadedJobId + "\","
				+ "\"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\","
				+ "\"profile\":\"TAGGED_PDF\"}";
		SendingToQueueErrorData sendingToQueueErrorData
				= new SendingToQueueErrorData("queueName", message,
				QueueErrorEventType.SENDING_ERROR_CALLBACK, "cause", null);
		jobTaskResultHandler.handleEvent(sendingToQueueErrorData);
		String responseMessage = "Message: " + message + " cannot be send" +
				" into the queue \\'queueName\\', cause: cause";

		//Getting job
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json("{'id':'" + uploadedJobId + "'," +
						"'profile':'TAGGED_PDF'," +
						"'status':'PROCESSING'," +
						"'tasks':[{" +
						"'fileId':'534bd16b-6bd5-404e-808e-5dc731c73963'," +
						"'status':'ERROR'," +
						"'errorType':'SENDING_TO_QUEUE_ERROR'," +
						"'errorMessage':'" + responseMessage + "'" +
						"}," +
						"{" +
						"'fileId' : '774bd16b-7ad5-354e-808e-5dc731c73963'," +
						"'status':'PROCESSING'" +
						"}" +
						"]}", true));
	}

	@Test
	public void createStartAndGetExecutedJobWithTasksWithSendingErrorAndTaskProcessingErrorTest() throws Exception {
		String uploadedJobId = createStartGetJobWithTasksAndReturnJobId();

		String message = "{\"jobId\":\"" + uploadedJobId + "\","
				+ "\"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\","
				+ "\"profile\":\"TAGGED_PDF\"}";
		SendingToQueueErrorData sendingToQueueErrorData
				= new SendingToQueueErrorData("queueName", message,
				QueueErrorEventType.SENDING_ERROR_CALLBACK, "cause", null);
		jobTaskResultHandler.handleEvent(sendingToQueueErrorData);
		String responseMessage = "Message: " + message + " cannot be send" +
				" into the queue \\'queueName\\', cause: cause";

		jobTaskResultHandler.handleMessage(
				"{\"jobId\":\"" + uploadedJobId + "\","
						+ "\"fileId\":\"774bd16b-7ad5-354e-808e-5dc731c73963\","
						+ "\"errorType\":\"PROCESSING_INTERNAL_ERROR\","
						+ "\"errorMessage\":\"some message\"}");

		//Getting job
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json("{'id':'" + uploadedJobId + "'," +
						"'profile':'TAGGED_PDF'," +
						"'status':'FINISHED'," +
						"'tasks':[{" +
						"'fileId':'534bd16b-6bd5-404e-808e-5dc731c73963'," +
						"'status':'ERROR'," +
						"'errorType':'SENDING_TO_QUEUE_ERROR'," +
						"'errorMessage':'" + responseMessage + "'" +
						"}," +
						"{" +
						"'fileId' : '774bd16b-7ad5-354e-808e-5dc731c73963'," +
						"'status':'ERROR'," +
						"'errorType':'PROCESSING_INTERNAL_ERROR'," +
						"'errorMessage':'some message'" +
						"}" +
						"]}", true));
	}

	private String createStartGetJobWithTasksAndReturnJobId() throws Exception {
		String requestBody = "{\"profile\": \"TAGGED_PDF\"," +
				"\"tasks\": [" +
				"    {" +
				"    \"fileId\":\"534bd16b-6bd5-404e-808e-5dc731c73963\"" +
				"    }," +
				"    {" +
				"    \"fileId\":\"774bd16b-7ad5-354e-808e-5dc731c73963\"" +
				"    }" +
				"]" +
				"}";

		//Creating job
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/jobs")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
				.andExpect(status().isCreated())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").isNotEmpty())
				.andExpect(jsonPath("$.profile").value("TAGGED_PDF"))
				.andExpect(jsonPath("$.status").value("CREATED"))
				.andExpect(jsonPath("$.tasks").isArray())
				.andExpect(jsonPath("$.tasks.length()").value(2))
				.andExpect(jsonPath("$.tasks[0].fileId")
						.value("534bd16b-6bd5-404e-808e-5dc731c73963"))
				.andExpect(jsonPath("$.tasks[0].status").value("CREATED"))
				.andExpect(jsonPath("$.tasks[1].fileId")
						.value("774bd16b-7ad5-354e-808e-5dc731c73963"))
				.andExpect(jsonPath("$.tasks[1].status").value("CREATED"))
				.andReturn();

		String jsonResponse = result.getResponse().getContentAsString();
		String uploadedJobId = JsonPath.read(jsonResponse, "$.id");

		assertEquals("http://localhost/jobs/" + uploadedJobId,
				result.getResponse().getHeader("Location"));

		//Starting job
		mockMvc.perform(MockMvcRequestBuilders.post("/jobs/" + uploadedJobId + "/execution")
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json("{'id':'" + uploadedJobId + "'," +
						"'profile':'TAGGED_PDF'," +
						"'status':'PROCESSING'," +
						"'tasks':[{" +
						"'fileId' : '534bd16b-6bd5-404e-808e-5dc731c73963'," +
						"'status':'PROCESSING'" +
						"}," +
						"{" +
						"'fileId' : '774bd16b-7ad5-354e-808e-5dc731c73963'," +
						"'status':'PROCESSING'" +
						"}" +
						"]}", true));

		//Getting job
		mockMvc.perform(MockMvcRequestBuilders.get("/jobs/" + uploadedJobId)
				.accept(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(content().json("{'id':'" + uploadedJobId + "'," +
						"'profile':'TAGGED_PDF'," +
						"'status':'PROCESSING'," +
						"'tasks':[{" +
						"'fileId' : '534bd16b-6bd5-404e-808e-5dc731c73963'," +
						"'status':'PROCESSING'" +
						"}," +
						"{" +
						"'fileId' : '774bd16b-7ad5-354e-808e-5dc731c73963'," +
						"'status':'PROCESSING'" +
						"}" +
						"]}", true));

		return uploadedJobId;
	}

	private void assertDBEmpty() {
		Assertions.assertEquals(0, jobRepository.count());
		Assertions.assertEquals(0, jobTaskRepository.count());
	}
}
