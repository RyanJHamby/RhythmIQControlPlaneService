package com.rhythmiq.controlplaneservice.model;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class AiRuleTest {
    @Test
    void testBuilder() {
        // Given
        String ruleId = "rule-123";
        String name = "Test Rule";
        String description = "Test Description";
        String category = "Test Category";
        String content = "Test Content";
        boolean isActive = true;
        String createdAt = "2024-01-01T00:00:00Z";
        String updatedAt = "2024-01-01T00:00:00Z";

        // When
        AiRule rule = AiRule.builder()
            .ruleId(ruleId)
            .name(name)
            .description(description)
            .category(category)
            .content(content)
            .isActive(isActive)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();

        // Then
        assertEquals(ruleId, rule.getRuleId());
        assertEquals(name, rule.getName());
        assertEquals(description, rule.getDescription());
        assertEquals(category, rule.getCategory());
        assertEquals(content, rule.getContent());
        assertEquals(isActive, rule.isActive());
        assertEquals(createdAt, rule.getCreatedAt());
        assertEquals(updatedAt, rule.getUpdatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        // When
        AiRule rule = new AiRule();

        // Then
        assertNull(rule.getRuleId());
        assertNull(rule.getName());
        assertNull(rule.getDescription());
        assertNull(rule.getCategory());
        assertNull(rule.getContent());
        assertFalse(rule.isActive());
        assertNull(rule.getCreatedAt());
        assertNull(rule.getUpdatedAt());
    }

    @Test
    void testSetters() {
        // Given
        AiRule rule = new AiRule();
        String ruleId = "rule-123";
        String name = "Test Rule";
        String description = "Test Description";
        String category = "Test Category";
        String content = "Test Content";
        boolean isActive = true;
        String createdAt = "2024-01-01T00:00:00Z";
        String updatedAt = "2024-01-01T00:00:00Z";

        // When
        rule.setRuleId(ruleId);
        rule.setName(name);
        rule.setDescription(description);
        rule.setCategory(category);
        rule.setContent(content);
        rule.setActive(isActive);
        rule.setCreatedAt(createdAt);
        rule.setUpdatedAt(updatedAt);

        // Then
        assertEquals(ruleId, rule.getRuleId());
        assertEquals(name, rule.getName());
        assertEquals(description, rule.getDescription());
        assertEquals(category, rule.getCategory());
        assertEquals(content, rule.getContent());
        assertEquals(isActive, rule.isActive());
        assertEquals(createdAt, rule.getCreatedAt());
        assertEquals(updatedAt, rule.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        String createdAt = "2024-01-01T00:00:00Z";
        String updatedAt = "2024-01-01T00:00:00Z";
        AiRule rule1 = AiRule.builder()
            .ruleId("rule-123")
            .name("Test Rule")
            .description("Test Description")
            .category("Test Category")
            .content("Test Content")
            .isActive(true)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();

        AiRule rule2 = AiRule.builder()
            .ruleId("rule-123")
            .name("Test Rule")
            .description("Test Description")
            .category("Test Category")
            .content("Test Content")
            .isActive(true)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();

        AiRule differentRule = AiRule.builder()
            .ruleId("rule-456")
            .name("Different Rule")
            .build();

        // Then
        assertEquals(rule1, rule2);
        assertEquals(rule1.hashCode(), rule2.hashCode());
        assertNotEquals(rule1, differentRule);
        assertNotEquals(rule1.hashCode(), differentRule.hashCode());
    }

    @Test
    void testToString() {
        // Given
        String createdAt = "2024-01-01T00:00:00Z";
        String updatedAt = "2024-01-01T00:00:00Z";
        AiRule rule = AiRule.builder()
            .ruleId("rule-123")
            .name("Test Rule")
            .description("Test Description")
            .category("Test Category")
            .content("Test Content")
            .isActive(true)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();

        // When
        String toString = rule.toString();

        // Then
        assertTrue(toString.contains("ruleId=rule-123"));
        assertTrue(toString.contains("name=Test Rule"));
        assertTrue(toString.contains("description=Test Description"));
        assertTrue(toString.contains("category=Test Category"));
        assertTrue(toString.contains("content=Test Content"));
        assertTrue(toString.contains("isActive=true"));
        assertTrue(toString.contains("createdAt=" + createdAt));
        assertTrue(toString.contains("updatedAt=" + updatedAt));
    }
} 