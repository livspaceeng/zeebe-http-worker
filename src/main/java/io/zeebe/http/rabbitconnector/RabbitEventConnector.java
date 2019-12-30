/** */
package io.zeebe.http.rabbitconnector;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.zeebe.spring.client.ZeebeClientLifecycle;
import lombok.extern.slf4j.Slf4j;

/** @author Ankit Agrawal */
@Component
@Slf4j
public class RabbitEventConnector {

  @Autowired private ZeebeClientLifecycle zeebeClient;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static final String DATA_KEY = "data";

  @Value("${zeebe.message.buffer.ttl}")
  private String defaultTTL;

  public void correlateEvent(String message) throws JsonMappingException, JsonProcessingException {
    log.info(":::: Handling RabbitMQ event :::: {}", message);
    MessageDTO messageDTO = objectMapper.readValue(message, MessageDTO.class);

    String messageTTL = StringUtils.isEmpty(messageDTO.getTtl()) ? defaultTTL : messageDTO.getTtl();
    Map<String, String> dataMap = new HashMap<String, String>();
    dataMap.put(DATA_KEY, messageDTO.getData());

    zeebeClient
        .newPublishMessageCommand()
        .messageName(messageDTO.getName())
        .correlationKey(messageDTO.getCorrelationKey())
        .timeToLive(Duration.ofSeconds(Long.valueOf(messageTTL)))
        .variables(dataMap)
        .send()
        .join(); // To wait for callback. Remove this for no-wait.
    log.info(":::: Published the message to Zeebe ::::");
  }
}
