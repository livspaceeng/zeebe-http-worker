package io.zeebe.http;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.http.lambda.LambdaJobHandler;
import io.zeebe.spring.client.EnableZeebeClient;
import io.zeebe.spring.client.annotation.ZeebeWorker;

@SpringBootApplication
@EnableZeebeClient
public class ZeebeHttpWorkerApplication extends SpringBootServletInitializer {

  public static void main(String[] args) {
    SpringApplication.run(ZeebeHttpWorkerApplication.class, args);
  }

  @Autowired private HttpJobHandler httpJobHandler;
  @Autowired private LambdaJobHandler lambdaJobHandler;

  @ZeebeWorker(type = "platform.commons.http", name = "platform-http-worker", maxJobsActive = 32)
  public void handleHttpJob(final JobClient client, final ActivatedJob job)
      throws IOException, InterruptedException {
    httpJobHandler.handle(client, job);
  }

  @ZeebeWorker(
      type = "platform.commons.lambda",
      name = "platform-lambda-worker",
      maxJobsActive = 32)
  public void handleLambdaJob(final JobClient client, final ActivatedJob job) {
    lambdaJobHandler.handle(client, job);
  }
}
