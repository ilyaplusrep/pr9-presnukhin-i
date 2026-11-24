package ru.edu.pr06;

/**
 * Запись об операции (иммутабельная).
 * Вариант 16, часть А: поле confirmed — подтверждён ли приход.
 * Крупный приход (больше Warehouse.CONFIRMATION_LIMIT) без подтверждения записывается с confirmed = false.
 */
public record StockMovement(String itemCode, MovementType type, int quantity, boolean confirmed) {

    // Обычная операция не требует подтверждения
    public StockMovement(String itemCode, MovementType type, int quantity) {
        this(itemCode, type, quantity, true);
    }
}
