package com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat.dto.ChatRequest;
import com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat.dto.ChatResponse;
import com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat.dto.Choice;
import com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat.dto.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;


@RestClientTest(OpenAiChatClient.class)
class OpenAiChatClientTest {

  @Autowired
  private OpenAiChatClient openAiChatClient; //This is the class that we are testing

  @Autowired
  private MockRestServiceServer mockRestServiceServer; //This is the mock server that we are using to test the API

  @Autowired
  private ObjectMapper objectMapper;

  private String url;

  private ChatRequest chatRequest;

  @BeforeEach
  void setUp() {
    this.url = "https://api.openai.com/v1/chat/completions";

     this.chatRequest = new ChatRequest("gpt-4", List.of(
            new Message("system", "Your task is to generate a short summary of a given JSON array in at most 100 words. The summary must include the number of books, each book's description and the publisher information. Please don't include the statement The JSON array. This is the JSON array"),
            new Message("user", "A json Array.")));
  }

  @Test
  void testGenerateSuccess() throws JsonProcessingException {
    //Given:
//    ChatRequest chatRequest = new ChatRequest("gpt-4", List.of(
//            new Message("system", "Your task is to generate a short summary of a given JSON array in at most 100 words. The summary must include the number of books, each book's description and the publisher information. Please don't include the statement The JSON array. This is the JSON array"),
//            new Message("user", "A json Array.")
//    ));

    ChatResponse chatResponse = new ChatResponse(List.of(new Choice(0,
            new Message("assistant", "The summary includes the number of books, each book's description and the publisher information. The JSON array has 3 books."))));

    //define the behavior of the mock server
    this.mockRestServiceServer
            .expect(requestTo(this.url))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", startsWith("Bearer ")))
            .andExpect(content().json(this.objectMapper.writeValueAsString(this.chatRequest)))
            .andRespond(withSuccess(this.objectMapper.writeValueAsString(chatResponse), MediaType.APPLICATION_JSON));

    //When:
    ChatResponse generatedChatResponse = this.openAiChatClient.generate(this.chatRequest);

    //Then:
    this.mockRestServiceServer.verify(); //verify that all expected requests set up via expect and andExpect were actually performed.
    assertThat(generatedChatResponse.choices().get(0).message().content())
            .isEqualTo("The summary includes the number of books, each book's description and the publisher information. The JSON array has 3 books.");
  }

  @Test
  void testGenerateUnauthorizedRequest(){
    //Given:
    this.mockRestServiceServer
            .expect(requestTo(this.url))
            .andExpect(method(HttpMethod.POST))

            .andRespond(withUnauthorizedRequest());

    //When
    Throwable thrown = catchThrowable(() -> {
      ChatResponse generatedResponse = this.openAiChatClient.generate(this.chatRequest);
    });

    //Then:
    this.mockRestServiceServer.verify();
    assertThat(thrown).isInstanceOf(RuntimeException.class)
            .hasMessageContaining("401 Unauthorized");
  }

  //create test cases for 429, 500 and 503 status codes
  @Test
  void testGenerateRateLimitExceeded(){
    //Given:
    this.mockRestServiceServer
            .expect(requestTo(this.url))
            .andExpect(method(HttpMethod.POST))

            .andRespond(withTooManyRequests());

    //When
    Throwable thrown = catchThrowable(() -> {
      ChatResponse generatedResponse = this.openAiChatClient.generate(this.chatRequest);
    });

    //Then:
    this.mockRestServiceServer.verify();
    assertThat(thrown).isInstanceOf(RuntimeException.class)
            .hasMessageContaining("429 Too Many Requests");
  }

  @Test
  void testGenerateInternalServerError(){
    //Given:
    this.mockRestServiceServer
            .expect(requestTo(this.url))
            .andExpect(method(HttpMethod.POST))

            .andRespond(withServerError());

    //When
    Throwable thrown = catchThrowable(() -> {
      ChatResponse generatedResponse = this.openAiChatClient.generate(this.chatRequest);
    });

    //Then:
    this.mockRestServiceServer.verify();
    assertThat(thrown).isInstanceOf(RuntimeException.class)
            .hasMessageContaining("500 Internal Server Error");
  }

  @Test
  void testGenerateServiceUnavailable(){
    //Given:
    this.mockRestServiceServer
            .expect(requestTo(this.url))
            .andExpect(method(HttpMethod.POST))

            .andRespond(withServiceUnavailable());

    //When
    Throwable thrown = catchThrowable(() -> {
      ChatResponse generatedResponse = this.openAiChatClient.generate(this.chatRequest);
    });

    //Then:
    this.mockRestServiceServer.verify();
    assertThat(thrown).isInstanceOf(RuntimeException.class)
            .hasMessageContaining("503 Service Unavailable");
  }


}