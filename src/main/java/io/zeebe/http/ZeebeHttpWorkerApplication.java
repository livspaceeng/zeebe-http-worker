package io.zeebe.http;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.http.lambda.LambdaJobHandler;
import io.zeebe.http.rabbitconnector.RabbitEventConnector;
import io.zeebe.spring.client.EnableZeebeClient;
import io.zeebe.spring.client.annotation.ZeebeWorker;

@SpringBootApplication
@EnableZeebeClient
public class ZeebeHttpWorkerApplication extends SpringBootServletInitializer {

  @Autowired private HttpJobHandler httpJobHandler;
  @Autowired private LambdaJobHandler lambdaJobHandler;
  @Autowired private RabbitEventConnector rabbitEventConnector;

  public static void main(String[] args) {
    SpringApplication.run(ZeebeHttpWorkerApplication.class, args);
  }

  @ZeebeWorker(
      type = "${zeebe.worker.http.type}",
      name = "${zeebe.worker.http.name}",
      maxJobsActive = 32)
  public void handleHttpJob(final JobClient client, final ActivatedJob job)
      throws IOException, InterruptedException {
    httpJobHandler.handle(client, job);
  }

  @ZeebeWorker(
      type = "${zeebe.worker.lambda.type}",
      name = "${zeebe.worker.lambda.name}",
      maxJobsActive = 32)
  public void handleLambdaJob(final JobClient client, final ActivatedJob job) throws Exception {
    lambdaJobHandler.handle(client, job);
  }

  //@RabbitListener(queues = "${rabbit.queue.name}", concurrency = "${rabbit.queue.concurrency}")
  public void handleRabbitEvents(String message)
      throws JsonMappingException, JsonProcessingException {
    rabbitEventConnector.correlateEvent(message);
  }
}
