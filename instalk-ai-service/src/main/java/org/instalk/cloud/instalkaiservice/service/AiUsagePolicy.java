package org.instalk.cloud.instalkaiservice.service;

import org.instalk.cloud.common.model.po.Message;
import org.instalk.cloud.common.model.po.UserAiConfig;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AiUsagePolicy {

    public boolean canSendMessage(UserAiConfig userAiConfig) {
        if (userAiConfig.getDailyMessageLimit() == null) {
            return true;
        }
        return userAiConfig.getDailyMessageCount() < userAiConfig.getDailyMessageLimit();
    }

    public boolean needsReset(UserAiConfig userAiConfig) {
        if (userAiConfig.getLastResetDate() == null) {
            return true;
        }
        return !userAiConfig.getLastResetDate().isEqual(LocalDate.now());
    }

    public Long estimateTokenCount(String text) {
        if (text == null || text.isEmpty()) {
            return 0L;
        }
        long chineseCharCount = text.chars().filter(ch -> ch >= 0x4e00 && ch <= 0x9fff).count();
        long englishCharCount = text.length() - chineseCharCount;
        long englishTokens = (long) Math.ceil(englishCharCount / 4.0);
        long chineseTokens = chineseCharCount;
        return englishTokens + chineseTokens;
    }

    public String buildSimpleSummary(List<Message> messages, Long userId) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }
        List<String> lines = new ArrayList<>();
        for (Message message : messages) {
            String role = message.getSenderId().equals(userId) ? "用户" : "助手";
            String content = message.getContent() == null ? "" : message.getContent().trim();
            if (content.isEmpty()) {
                continue;
            }
            lines.add(role + ": " + content);
        }
        return trimSummaryLines(lines, 12, 2000);
    }

    private String trimSummaryLines(List<String> lines, int maxLines, int maxChars) {
        if (lines == null || lines.isEmpty()) {
            return "";
        }
        List<String> trimmed = lines;
        if (lines.size() > maxLines) {
            trimmed = lines.subList(lines.size() - maxLines, lines.size());
        }
        String joined = trimmed.stream().collect(Collectors.joining("\n"));
        if (joined.length() > maxChars) {
            return joined.substring(joined.length() - maxChars);
        }
        return joined;
    }
}
