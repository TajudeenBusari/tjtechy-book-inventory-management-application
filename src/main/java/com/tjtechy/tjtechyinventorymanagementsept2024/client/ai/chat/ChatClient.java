package com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat;

import com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat.dto.ChatRequest;
import com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat.dto.ChatResponse;

public interface ChatClient {
  ChatResponse generate(ChatRequest chatRequest);
}
/***
 * This interface can have multiple implementations.
 * E.g. OpenAIChatClient, GPT3ChatClient,  GoogleGeminiChatClient etc.
 */