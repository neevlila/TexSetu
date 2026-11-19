package com.texsetu.entity.enums;

public enum OrderStatus {
    CONFIRMED,
    MATERIAL_RECEIVED,
    PROCESSING,
    QUALITY_CHECK,
    COMPLETED,
    DISPATCHED,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus next) {
        if (this == next) return false;
        return switch (this) {
            case CONFIRMED -> next == MATERIAL_RECEIVED || next == CANCELLED;
            case MATERIAL_RECEIVED -> next == PROCESSING || next == CANCELLED;
            case PROCESSING -> next == QUALITY_CHECK || next == CANCELLED;
            case QUALITY_CHECK -> next == COMPLETED || next == PROCESSING || next == CANCELLED; // PROCESSING on rework
            case COMPLETED -> next == DISPATCHED;
            case DISPATCHED -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
