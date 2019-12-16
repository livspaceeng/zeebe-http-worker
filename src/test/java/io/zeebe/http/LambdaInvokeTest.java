/** */
package io.zeebe.http;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;

/** @author Ankit Agrawal */
public class LambdaInvokeTest {

  public static void main(String[] args) {
    String lambdaName = "myTestLambda";
    String lambdaInput = null;
    final String AWS_ACCESS_KEY_ID = "access";
    final String AWS_SECRET_ACCESS_KEY = "secret";

    AWSCredentials credentials = new BasicAWSCredentials(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);
    AWSLambda lambdaClient =
        AWSLambdaClientBuilder.standard()
            .withRegion(Regions.AP_SOUTHEAST_1)
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .build();

    InvokeRequest lambdaRequest =
        new InvokeRequest().withFunctionName(lambdaName).withPayload(lambdaInput);

    lambdaRequest.setInvocationType(InvocationType.RequestResponse); // Use Event for Async

    InvokeResult lambdaResult = lambdaClient.invoke(lambdaRequest);
    ByteBuffer resultBuffer = lambdaResult.getPayload().asReadOnlyBuffer();
    String responseData = null;
    if (resultBuffer != null) {
      responseData = StandardCharsets.UTF_8.decode(resultBuffer).toString();
    }
    System.out.println("testLambdaFunction::Lambda result: " + responseData);
  }
}
