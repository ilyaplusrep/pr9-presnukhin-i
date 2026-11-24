package ru.edu.pr06;

import java.util.*;

/**
 * Склад: каталог позиций по коду и история движений.
 * Коллекции private final и наружу не отдаются (R6).
 */
public class Warehouse {
    // Вариант 16, часть А: приход больше этого количества требует подтверждения
    public static final int CONFIRMATION_LIMIT = 500;

    // Map для доступа по коду за O(1)
    private final Map<String, StockItem> items = new HashMap<>();
    // List для упорядоченной истории движений
    private final List<StockMovement> movements = new ArrayList<>();

    // Задание 3 каркаса: R8 (null), R1 (уникальность кода)
    public void addItem(StockItem item) {
        if (item == null)
            throw new IllegalArgumentException("item is null");
        if (items.containsKey(item.getCode()))
            throw new IllegalArgumentException("code already exists");
        items.put(item.getCode(), item);
    }

    // Задание 4 каркаса: приход без подтверждения (R3, R9; вариант 16 — крупный приход помечается неподтверждённым)
    public void receive(String code, int q) {
        receive(code, q, false);
    }

    // Вариант 16, часть А: приход с отметкой о подтверждении
    public void receive(String code, int q, boolean approved) {
        StockItem item = findExisting(code);
        item.increase(q);
        boolean confirmed = !requiresConfirmation(q) || approved;
        movements.add(new StockMovement(code, MovementType.RECEIVE, q, confirmed));
    }

    // Задание 5 каркаса: выдача (R2 через decrease, R3, R9)
    public void issue(String code, int q) {
        StockItem item = findExisting(code);
        item.decrease(q);
        movements.add(new StockMovement(code, MovementType.ISSUE, q));
    }

    // Задание 6 каркаса: поиск по категории, возвращается новый список (R5)
    public List<StockItem> findByCategory(Category c) {
        List<StockItem> result = new ArrayList<>();
        for (StockItem item : items.values()) {
            if (item.getCategory() == c) {
                result.add(item);
            }
        }
        return result;
    }

    // Задание 7 каркаса: сумма остатков по категориям; отсутствующие категории не включаются
    public Map<Category, Integer> totalsByCategory() {
        Map<Category, Integer> result = new HashMap<>();
        for (StockItem item : items.values()) {
            Category cat = item.getCategory();
            if (result.containsKey(cat)) {
                result.put(cat, result.get(cat) + item.getQuantity());
            } else {
                result.put(cat, item.getQuantity());
            }
        }
        return result;
    }

    // Вариант 16, часть Б: неподтверждённые операции (новый список, R5/R6)
    public static List<StockMovement> addUnconfirmedMovements(Warehouse wh) {
        if (wh == null)
            throw new IllegalArgumentException("warehouse is null");
        List<StockMovement> result = new ArrayList<>();
        for (StockMovement m : wh.movements) {
            if (!m.confirmed()) {
                result.add(m);
            }
        }
        return result;
    }

    // Остаток по коду — число, а не ссылка на StockItem (R6)
    public int quantityOf(String code) {
        return findExisting(code).getQuantity();
    }

    // Размеры коллекций без выдачи самих коллекций (R6)
    public int itemsCount() { return items.size(); }
    public int movementsCount() { return movements.size(); }

    // R9: код должен существовать
    private StockItem findExisting(String code) {
        StockItem item = items.get(code);
        if (item == null)
            throw new IllegalArgumentException("code not found: " + code);
        return item;
    }

    private static boolean requiresConfirmation(int q) {
        return q > CONFIRMATION_LIMIT;
    }
}
