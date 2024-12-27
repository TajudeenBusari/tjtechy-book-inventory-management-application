package com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat.dto;

import java.util.List;

public record ChatRequest(String model, List<Message> messages) {
    public ChatRequest {
    }

}
/***
 * This is a record class which is used to create a DTO object for ChatRequest.
 * It is also possible to create a DTO object using a normal class.
 * Records are used to create immutable objects. And when the properties are few,
 * records are the best choice.
 */