package org.instalk.cloud.common.model.enums;

/**
 * Stored memory category for RAG / user profiling.
 */
public enum AiMemoryType {
    /** Objective facts about the user or context */
    FACT,
    /** Likes, dislikes, habits, explicit choices */
    PREFERENCE,
    /** Time-bound or sequential happenings worth recalling */
    EVENT;

    public static AiMemoryType fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return FACT;
        }
        try {
            return AiMemoryType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return FACT;
        }
    }
}
