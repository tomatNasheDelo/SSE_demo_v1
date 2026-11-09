package ru.demo.sse;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class NotificationService {
    private static final long SSE_TIMEOUT_MILLIS = 0L;
    private static final int MAX_HISTORY = 50;

    private final CopyOnWriteArraySet<SseEmitter> clients = new CopyOnWriteArraySet<>();
    private final List<NotificationEvent> history = new ArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        clients.add(emitter);
        emitter.onCompletion(() -> clients.remove(emitter));
        emitter.onTimeout(() -> clients.remove(emitter));
        emitter.onError(error -> clients.remove(emitter));

        try {
            emitter.send(SseEmitter.event().name("connected").data("SSE connection established"));
        } catch (IOException exception) {
            clients.remove(emitter);
            emitter.completeWithError(exception);
        }
        return emitter;
    }

    public NotificationEvent publish(String message) {
        NotificationEvent event = new NotificationEvent(UUID.randomUUID().toString(), message, Instant.now());
        synchronized (history) {
            history.add(event);
            if (history.size() > MAX_HISTORY) {
                history.remove(0);
            }
        }

        for (SseEmitter client : clients) {
            try {
                client.send(SseEmitter.event().id(event.id()).name("notification").data(event));
            } catch (IOException | IllegalStateException exception) {
                clients.remove(client);
                client.completeWithError(exception);
            }
        }
        return event;
    }

    public List<NotificationEvent> recent() {
        synchronized (history) {
            List<NotificationEvent> copy = new ArrayList<>(history);
            Collections.reverse(copy);
            return copy;
        }
    }

    public int connectedClients() {
        return clients.size();
    }
}
