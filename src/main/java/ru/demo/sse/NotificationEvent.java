package ru.demo.sse;

import java.time.Instant;

public record NotificationEvent(String id, String message, Instant createdAt) {
}
