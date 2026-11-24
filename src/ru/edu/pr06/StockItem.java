package ru.edu.pr06;

/**
 * Карточка позиции на складе.
 * code, name, category задают идентичность и не меняются (final),
 * quantity меняется только через increase/decrease.
 */
public class StockItem {
    private final String code;
    private final String name;
    private final Category category;
    private int quantity;

    // R7, R8: инварианты проверяются при создании (fail-fast)
    public StockItem(String code, String name, Category category, int quantity) {
        if (code == null || code.isBlank())
            throw new IllegalArgumentException("code is null or blank");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name is null or blank");
        if (category == null)
            throw new IllegalArgumentException("category is null");
        if (quantity < 0)
            throw new IllegalArgumentException("quantity must be >= 0");
        this.code = code;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
    }

    // Задание 1 каркаса: увеличение остатка
    public void increase(int q) {
        if (q <= 0)
            throw new IllegalArgumentException("q must be > 0");
        this.quantity += q;
    }

    // Задание 2 каркаса: уменьшение остатка, R2 — остаток не может стать отрицательным
    public void decrease(int q) {
        if (q <= 0)
            throw new IllegalArgumentException("q must be > 0");
        if (quantity - q < 0)
            throw new IllegalArgumentException("quantity cannot become negative");
        this.quantity -= q;
    }

    // Идентичность позиции — только code (см. 4.5)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockItem other = (StockItem) o;
        return code.equals(other.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return code + " " + name + " " + category + " qty=" + quantity;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public Category getCategory() { return category; }
    public int getQuantity() { return quantity; }
}
