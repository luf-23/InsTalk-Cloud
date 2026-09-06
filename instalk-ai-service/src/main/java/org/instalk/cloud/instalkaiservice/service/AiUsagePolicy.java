package org.instalk.cloud.instalkaiservice.service;

import org.instalk.cloud.common.model.po.UserAiConfig;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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
}
