/** */
package io.zeebe.http.rabbitconnector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** @author Ankit Agrawal */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {

  private String name;
  private String data;
  private String correlationKey;
  private String ttl; // Time to live in seconds
}
