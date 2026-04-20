/*
 Copyright 2016-2026 Orange

 This software is distributed under the MIT license, see LICENSE.txt file for more details.

 @author Mathieu LEFEBVRE <mathieu1.lefebvre@orange.com>
 @generated GitHub Copilot (Claude Sonnet 4.6)
 */
package com.orange.iot3core.clients;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MqttClient#isValidMqttPubTopic(String)}.
 * This method is pure logic (no I/O) and can be tested entirely in isolation.
 */
@DisplayName("MQTT publish topic validation")
class MqttTopicValidationTest {

    // ── happy-path ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("single-level topic is valid")
    void singleLevelTopic_returnsTrue() {
        assertTrue(MqttClient.isValidMqttPubTopic("topic"));
    }

    @Test
    @DisplayName("multi-level topic with slashes is valid")
    void multiLevelTopic_returnsTrue() {
        assertTrue(MqttClient.isValidMqttPubTopic("a/b/c/d/e"));
    }

    @Test
    @DisplayName("topic at exactly max length (65535) is valid")
    void topicAtMaxLength_returnsTrue() {
        String maxTopic = "a".repeat(65535);
        assertTrue(MqttClient.isValidMqttPubTopic(maxTopic));
    }

    @Test
    @DisplayName("topic with digits and special chars (except wildcards) is valid")
    void topicWithDigitsAndSpecialChars_returnsTrue() {
        assertTrue(MqttClient.isValidMqttPubTopic("context/inQueue/v2x/cam/uuid-123/0/3/1/2"));
    }

    // ── null / empty ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("null topic is invalid")
    void nullTopic_returnsFalse() {
        assertFalse(MqttClient.isValidMqttPubTopic(null));
    }

    @Test
    @DisplayName("empty topic is invalid")
    void emptyTopic_returnsFalse() {
        assertFalse(MqttClient.isValidMqttPubTopic(""));
    }

    // ── wildcard characters ───────────────────────────────────────────────────

    @Test
    @DisplayName("topic ending with '#' wildcard is invalid")
    void topicWithHashAtEnd_returnsFalse() {
        assertFalse(MqttClient.isValidMqttPubTopic("my/topic/#"));
    }

    @Test
    @DisplayName("topic containing '#' in the middle is invalid")
    void topicWithHashInMiddle_returnsFalse() {
        assertFalse(MqttClient.isValidMqttPubTopic("my/#/topic"));
    }

    @Test
    @DisplayName("topic containing '+' single-level wildcard is invalid")
    void topicWithPlusWildcard_returnsFalse() {
        assertFalse(MqttClient.isValidMqttPubTopic("my/+/topic"));
    }

    @Test
    @DisplayName("topic that is just '+' is invalid")
    void topicIsOnlyPlus_returnsFalse() {
        assertFalse(MqttClient.isValidMqttPubTopic("+"));
    }

    // ── whitespace ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("topic with leading space is invalid")
    void topicWithLeadingSpace_returnsFalse() {
        assertFalse(MqttClient.isValidMqttPubTopic(" my/topic"));
    }

    @Test
    @DisplayName("topic with trailing space is invalid")
    void topicWithTrailingSpace_returnsFalse() {
        assertFalse(MqttClient.isValidMqttPubTopic("my/topic "));
    }

    // ── length limit ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("topic exceeding 65535 chars is invalid")
    void topicExceedingMaxLength_returnsFalse() {
        String tooLong = "a".repeat(65536);
        assertFalse(MqttClient.isValidMqttPubTopic(tooLong));
    }
}

