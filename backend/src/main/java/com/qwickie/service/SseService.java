package com.qwickie.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final List<SseEmitter> partnerEmitters = new java.util.concurrent.CopyOnWriteArrayList<>();

    public SseEmitter subscribe(Long orderId) {
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1 hour timeout
        emitters.put(orderId, emitter);

        emitter.onCompletion(() -> emitters.remove(orderId));
        emitter.onTimeout(() -> emitters.remove(orderId));
        emitter.onError((e) -> emitters.remove(orderId));

        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connected"));
        } catch (IOException e) {
            emitters.remove(orderId);
        }

        return emitter;
    }

    public SseEmitter subscribeToPartnerStream() {
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);
        partnerEmitters.add(emitter);

        emitter.onCompletion(() -> partnerEmitters.remove(emitter));
        emitter.onTimeout(() -> partnerEmitters.remove(emitter));
        emitter.onError((e) -> partnerEmitters.remove(emitter));

        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connected to partner stream"));
        } catch (IOException e) {
            partnerEmitters.remove(emitter);
        }

        return emitter;
    }

    public void broadcastToPartners(String eventName, Object data) {
        for (SseEmitter emitter : partnerEmitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                partnerEmitters.remove(emitter);
            }
        }
    }

    public void sendOrderStatusUpdate(Long orderId, String status) {
        SseEmitter emitter = emitters.get(orderId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("status-update").data(status));
            } catch (IOException e) {
                emitters.remove(orderId);
            }
        }
    }
}
