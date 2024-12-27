package com.tjtechy.tjtechyinventorymanagementsept2024.client.ai.chat.dto;

public record Choice(int index, Message message) {
}



/***
 * Multiple choices are offered by the chatgpt model to
 * the users to provide diversity and options to choose from.
 * This is useful in scenarios where there multiple correct answers
 */