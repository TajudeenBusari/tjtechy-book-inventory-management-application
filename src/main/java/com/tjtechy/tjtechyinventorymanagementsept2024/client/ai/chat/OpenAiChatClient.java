package com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat;

import com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat.dto.ChatRequest;
import com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenAiChatClient implements ChatClient {
  private final RestClient restClient;

  public OpenAiChatClient(@Value("${ai.openai.endpoint}") String endpoint,
                          @Value("${ai.openai.api-key}") String apiKey,
                          RestClient.Builder restClientBuilder) {

    this.restClient = restClientBuilder
            .baseUrl(endpoint)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .build();
  }



    @Override
    public ChatResponse generate(ChatRequest chatRequest) {
        return this.restClient
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(chatRequest)
                .retrieve()
                .body(ChatResponse.class); //convert to the ChatResponse object
    }
}
/***
 * The job of this class is to interact with the OpenAI chat API.
 * So, we need the RestClient to make the API call.
 * We need to define the ResClient builder (RestClientBuilderConfiguration) in the application context.
 * endpoint and apiKey are the properties that we need to define in the postgres.yml file.
 * The values are set in the environment variables of the IDE.
 */