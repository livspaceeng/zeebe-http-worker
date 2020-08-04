/** */
package io.zeebe.http.lambda;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.AWSLambdaException;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import lombok.extern.slf4j.Slf4j;

/** @author Ankit Agrawal */
@Component
@Slf4j
public class LambdaJobHandler implements JobHandler {

  private AWSLambda lambdaClient;
  private final ObjectMapper MAPPER = new ObjectMapper();
  private final String PARAMETER_LAMBDA_NAME = "lambdaName";

  @PostConstruct
  public void init() {
    this.lambdaClient =
        AWSLambdaClientBuilder.standard().withRegion(Regions.AP_SOUTHEAST_1).build();
  }

  @Override
  public void handle(JobClient client, ActivatedJob job) throws Exception {
    log.info(":::: Handling job {} of type {} ::::", job.getKey(), job.getType());
    try {
      Map<String, Object> jobVariables = job.getVariablesAsMap();
      String lambdaName = job.getCustomHeaders().get(PARAMETER_LAMBDA_NAME);

      String jobVariablesString = MAPPER.writeValueAsString(jobVariables);
      InvokeRequest lambdaRequest =
          new InvokeRequest().withFunctionName(lambdaName).withPayload(jobVariablesString);
      lambdaRequest.setInvocationType(InvocationType.RequestResponse); // Use Event for Async

      InvokeResult lambdaResult = this.lambdaClient.invoke(lambdaRequest);
      ByteBuffer resultBuffer = lambdaResult.getPayload().asReadOnlyBuffer();
      String responseData = null;
      if (resultBuffer != null) {
        responseData = StandardCharsets.UTF_8.decode(resultBuffer).toString();
      }
      log.info(":::: Output of lambda function {} : {} ::::", lambdaName, responseData);

      Map<String, Object> outputVariables;
      if (responseData != null) {
        outputVariables =
            MAPPER.readValue(responseData, new TypeReference<Map<String, Object>>() {});
      } else {
        outputVariables = jobVariables;
      }
      client.newCompleteCommand(job.getKey()).variables(outputVariables).send().join();

    } catch (AWSLambdaException e) {
      log.error(
          ":::: Exception occurred while invoking Lambda due to {} ::::", e.getErrorMessage());
      client
          .newFailCommand(job.getKey())
          .retries(0)
          .errorMessage(e.getErrorMessage())
          .send()
          .join();
    }
  }
}
