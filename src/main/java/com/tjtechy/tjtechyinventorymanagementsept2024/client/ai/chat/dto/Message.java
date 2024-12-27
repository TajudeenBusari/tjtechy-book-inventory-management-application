package com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat.dto;

public record Message(String role, String content) {
    public Message {
    }
}
/***
 * This is a record class which is used to create a DTO object for Message.
 * It is also possible to create a DTO object using a normal class.
 * role can be user, System or Assistant. Check the openai chatgpt doc.
 */