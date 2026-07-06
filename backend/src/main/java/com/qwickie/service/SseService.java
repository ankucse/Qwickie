package com.qwickie.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core Service for managing Server-Sent Events (SSE).
 * SSE enables unidirectional, real-time data streaming from the server to the client.
 * This service handles two distinct types of real-time streams:
 * 1. Individual Order Streams: Watched by consumers to track a specific order.
 * 2. Global Partner Stream: Watched by riders to see all new orders globally.
 *
 * @author Ankit Sinha
 */
@Service
public class SseService {

    // Thread-safe map storing connections tied to specific order IDs.
    // Key: Order ID, Value: The active SSE connection for that order.
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    
    // Thread-safe list storing all active rider connections globally.
    // Uses CopyOnWriteArrayList to prevent ConcurrentModificationException during broadcasts.
    private final List<SseEmitter> partnerEmitters = new java.util.concurrent.CopyOnWriteArrayList<>();

    /**
     * Subscribes a consumer client to tracking updates for a specific order.
     */
    public SseEmitter subscribe(Long orderId) {
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1 hour timeout
        emitters.put(orderId, emitter);

        // Cleanup handlers to avoid memory leaks when clients disconnect
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

    /**
     * Subscribes a rider to the global broadcast stream for new and accepted orders.
     */
    public SseEmitter subscribeToPartnerStream() {
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);
        partnerEmitters.add(emitter);

        // Cleanup handlers
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

    /**
     * Broadcasts an event to EVERY currently connected delivery partner.
     * Used for things like "NEW_ORDER" (so all riders see it) or "ORDER_ACCEPTED"
     * (so all riders see the button disable).
     */
    public void broadcastToPartners(String eventName, Object data) {
        for (SseEmitter emitter : partnerEmitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                // If a client disconnected ungracefully, this write will throw an IOException.
                // We simply remove them from the active list.
                partnerEmitters.remove(emitter);
            }
        }
    }

    /**
     * Sends a direct, targeted status update to the consumer watching a specific order.
     */
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
