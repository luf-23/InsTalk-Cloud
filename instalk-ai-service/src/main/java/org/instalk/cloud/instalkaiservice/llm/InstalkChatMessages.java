package org.instalk.cloud.instalkaiservice.llm;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import org.instalk.cloud.common.model.dto.AiChatDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class InstalkChatMessages {

    private InstalkChatMessages() {}

    public static List<ChatMessage> forChatCompletion(List<AiChatDTO.AiChatMessage> contextMessages, String currentUserContent) {
        List<ChatMessage> messages = new ArrayList<>();
        if (contextMessages != null) {
            for (AiChatDTO.AiChatMessage m : contextMessages) {
                String role = m.getRole() == null ? "user" : m.getRole().toLowerCase(Locale.ROOT);
                String content = m.getContent() == null ? "" : m.getContent();
                switch (role) {
                    case "system" -> messages.add(SystemMessage.from(content));
                    case "assistant" -> messages.add(AiMessage.from(content));
                    case "user" -> messages.add(UserMessage.from(content));
                    default -> messages.add(UserMessage.from(content));
                }
            }
        }
        messages.add(UserMessage.from(currentUserContent == null ? "" : currentUserContent));
        return messages;
    }
}
