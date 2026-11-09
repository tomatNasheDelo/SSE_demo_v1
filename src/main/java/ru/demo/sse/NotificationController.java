package ru.demo.sse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping(path = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events() {
        return notificationService.subscribe();
    }

    @PostMapping("/notifications")
    public NotificationEvent publish(@RequestBody NotificationRequest request) {
        if (request == null || request.message() == null || request.message().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message must not be blank");
        }
        return notificationService.publish(request.message().trim());
    }

    @GetMapping("/notifications")
    public List<NotificationEvent> recent() {
        return notificationService.recent();
    }

    @GetMapping("/status")
    public StatusResponse status() {
        return new StatusResponse(notificationService.connectedClients());
    }

    public record NotificationRequest(String message) { }
    public record StatusResponse(int connectedClients) { }
}
