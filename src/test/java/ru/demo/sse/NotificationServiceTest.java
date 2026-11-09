package ru.demo.sse;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NotificationServiceTest {
    @Test
    void createsAndKeepsRecentNotificationInMemory() {
        NotificationService service = new NotificationService();
        NotificationEvent event = service.publish("hello");

        assertNotNull(event.id());
        assertEquals("hello", event.message());
        assertEquals(1, service.recent().size());
        assertEquals(event, service.recent().get(0));
    }

    @Test
    void registersSseClient() {
        NotificationService service = new NotificationService();
        SseEmitter emitter = service.subscribe();

        assertNotNull(emitter);
        assertEquals(1, service.connectedClients());
    }
}
