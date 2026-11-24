package ru.edu.pr06;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SelfCheck {
    private static final String DATA_FILE = "data/sample.csv";

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        runUnitChecks();
        System.out.println("Unit: " + passed + "/" + (passed + failed) + " passed");

        int passedBefore = passed;
        int failedBefore = failed;
        runDataChecks();
        int dataPassed = passed - passedBefore;
        int dataTotal = dataPassed + (failed - failedBefore);
        System.out.println("Data-driven: " + dataPassed + "/" + dataTotal + " passed");

        if (args.length > 0 && (args[0].equals("-i") || args[0].equals("--interactive"))) {
            runInteractive();
        }
    }

    // ---------- Режим 1: unit-проверки ----------

    private static void runUnitChecks() {
        // 1–6: StockItem
        try {
            StockItem item = new StockItem("NB-01", "Notebook", Category.COMPUTER, 12);
            expect("1 StockItem: корректное создание", item.getQuantity() == 12, "quantity=" + item.getQuantity());
        } catch (RuntimeException e) {
            unexpected("1 StockItem: корректное создание", e);
        }
        try {
            new StockItem("X", "Y", Category.COMPUTER, -1);
            noError("2 StockItem: quantity < 0 (R7)");
        } catch (IllegalArgumentException e) {
            checkMessage("2 StockItem: quantity < 0 (R7)", e, "quantity must be >= 0");
        }
        try {
            StockItem item = notebook();
            item.increase(5);
            expect("3 increase(5) при qty=12", item.getQuantity() == 17, "quantity=" + item.getQuantity());
        } catch (RuntimeException e) {
            unexpected("3 increase(5) при qty=12", e);
        }
        try {
            notebook().increase(0);
            noError("4 increase(0)");
        } catch (IllegalArgumentException e) {
            checkMessage("4 increase(0)", e, "q must be > 0");
        }
        try {
            StockItem item = notebook();
            item.decrease(5);
            expect("5 decrease(5) при qty=12", item.getQuantity() == 7, "quantity=" + item.getQuantity());
        } catch (RuntimeException e) {
            unexpected("5 decrease(5) при qty=12", e);
        }
        try {
            notebook().decrease(13);
            noError("6 decrease(13) при qty=12 (R2)");
        } catch (IllegalArgumentException e) {
            checkMessage("6 decrease(13) при qty=12 (R2)", e, "quantity cannot become negative");
        }

        // 7–9: addItem
        try {
            Warehouse wh = new Warehouse();
            wh.addItem(notebook());
            expect("7 addItem: корректное добавление", wh.itemsCount() == 1, "itemsCount=" + wh.itemsCount());
        } catch (RuntimeException e) {
            unexpected("7 addItem: корректное добавление", e);
        }
        try {
            Warehouse wh = new Warehouse();
            wh.addItem(notebook());
            wh.addItem(new StockItem("NB-01", "Notebook Pro", Category.COMPUTER, 3));
            noError("8 addItem: дубликат кода (R1)");
        } catch (IllegalArgumentException e) {
            checkMessage("8 addItem: дубликат кода (R1)", e, "code already exists");
        }
        try {
            new Warehouse().addItem(null);
            noError("9 addItem(null)");
        } catch (IllegalArgumentException e) {
            checkMessage("9 addItem(null)", e, "item is null");
        }

        // 10–11: receive
        try {
            Warehouse wh = sampleWarehouse();
            wh.receive("NB-01", 5);
            expect("10 receive(NB-01, 5)", wh.quantityOf("NB-01") == 17 && wh.movementsCount() == 1,
                    "quantity=" + wh.quantityOf("NB-01") + ", movements=" + wh.movementsCount());
        } catch (RuntimeException e) {
            unexpected("10 receive(NB-01, 5)", e);
        }
        try {
            sampleWarehouse().receive("XX-99", 5);
            noError("11 receive(XX-99, 5) (R9)");
        } catch (IllegalArgumentException e) {
            checkMessage("11 receive(XX-99, 5) (R9)", e, "code not found");
        }

        // 12–14: issue
        try {
            Warehouse wh = sampleWarehouse();
            wh.issue("NB-01", 3);
            expect("12 issue(NB-01, 3)", wh.quantityOf("NB-01") == 9 && wh.movementsCount() == 1,
                    "quantity=" + wh.quantityOf("NB-01") + ", movements=" + wh.movementsCount());
        } catch (RuntimeException e) {
            unexpected("12 issue(NB-01, 3)", e);
        }
        try {
            sampleWarehouse().issue("NB-01", 100);
            noError("13 issue(NB-01, 100) (R2)");
        } catch (IllegalArgumentException e) {
            checkMessage("13 issue(NB-01, 100) (R2)", e, "quantity cannot become negative");
        }
        try {
            sampleWarehouse().issue("XX-99", 3);
            noError("14 issue(XX-99, 3) (R9)");
        } catch (IllegalArgumentException e) {
            checkMessage("14 issue(XX-99, 3) (R9)", e, "code not found");
        }

        // 15–17: findByCategory
        try {
            Warehouse wh = sampleWarehouse();
            wh.addItem(new StockItem("NB-02", "Notebook Pro", Category.COMPUTER, 4));
            int size = wh.findByCategory(Category.COMPUTER).size();
            expect("15 findByCategory(COMPUTER): 2 позиции", size == 2, "size=" + size);
        } catch (RuntimeException e) {
            unexpected("15 findByCategory(COMPUTER): 2 позиции", e);
        }
        try {
            int size = sampleWarehouse().findByCategory(Category.NETWORK).size();
            expect("16 findByCategory(NETWORK): пустой список", size == 0, "size=" + size);
        } catch (RuntimeException e) {
            unexpected("16 findByCategory(NETWORK): пустой список", e);
        }
        try {
            Warehouse wh = sampleWarehouse();
            List<StockItem> found = wh.findByCategory(Category.COMPUTER);
            found.clear();
            int again = wh.findByCategory(Category.COMPUTER).size();
            expect("17 изменение результата findByCategory не влияет на склад (R5)",
                    wh.itemsCount() == 3 && again == 1,
                    "itemsCount=" + wh.itemsCount() + ", повторный поиск=" + again);
        } catch (RuntimeException e) {
            unexpected("17 изменение результата findByCategory не влияет на склад (R5)", e);
        }

        // 18: totalsByCategory
        try {
            Map<Category, Integer> totals = sampleWarehouse().totalsByCategory();
            boolean ok = totals.size() == 3
                    && totals.get(Category.COMPUTER) == 12
                    && totals.get(Category.DISPLAY) == 7
                    && totals.get(Category.PERIPHERAL) == 20;
            expect("18 totalsByCategory: COMPUTER=12, DISPLAY=7, PERIPHERAL=20", ok, "totals=" + totals);
        } catch (RuntimeException e) {
            unexpected("18 totalsByCategory", e);
        }

        // дополнительные проверки
        try {
            StockItem item = notebook();
            item.decrease(12);
            expect("доп: decrease(12) при qty=12 — граница, остаток 0", item.getQuantity() == 0,
                    "quantity=" + item.getQuantity());
        } catch (RuntimeException e) {
            unexpected("доп: decrease(12) при qty=12", e);
        }
        try {
            Set<StockItem> set = new HashSet<>();
            set.add(notebook());
            set.add(new StockItem("NB-01", "Другое имя", Category.DISPLAY, 1));
            expect("доп: equals/hashCode по code — HashSet хранит одну позицию", set.size() == 1,
                    "size=" + set.size());
        } catch (RuntimeException e) {
            unexpected("доп: equals/hashCode", e);
        }
        try {
            new StockItem("NB-01", " ", Category.COMPUTER, 1);
            noError("доп: пустой name (R8)");
        } catch (IllegalArgumentException e) {
            checkMessage("доп: пустой name (R8)", e, "name is null or blank");
        }
        try {
            Category.valueOf("UNKNOWN");
            noError("доп: Category.valueOf(\"UNKNOWN\") (R4)");
        } catch (IllegalArgumentException e) {
            checkMessage("доп: Category.valueOf(\"UNKNOWN\") (R4)", e, "No enum constant");
        }

        // вариант 16, часть А: приход больше 500 требует подтверждения
        try {
            Warehouse wh = sampleWarehouse();
            wh.receive("KB-01", 600);
            List<StockMovement> un = Warehouse.addUnconfirmedMovements(wh);
            expect("А: receive(KB-01, 600) — confirmed=false",
                    wh.quantityOf("KB-01") == 620 && un.size() == 1 && !un.get(0).confirmed(),
                    "quantity=" + wh.quantityOf("KB-01") + ", unconfirmed=" + un);
        } catch (RuntimeException e) {
            unexpected("А: receive(KB-01, 600)", e);
        }
        try {
            Warehouse wh = sampleWarehouse();
            wh.receive("KB-01", 500);
            int un = Warehouse.addUnconfirmedMovements(wh).size();
            expect("А: receive(KB-01, 500) — граница, подтверждение не нужно",
                    un == 0 && wh.movementsCount() == 1, "unconfirmed=" + un);
        } catch (RuntimeException e) {
            unexpected("А: receive(KB-01, 500)", e);
        }
        try {
            Warehouse wh = sampleWarehouse();
            wh.receive("KB-01", 600, true);
            int un = Warehouse.addUnconfirmedMovements(wh).size();
            expect("А: receive(KB-01, 600, true) — подтверждённый крупный приход",
                    un == 0 && wh.quantityOf("KB-01") == 620, "unconfirmed=" + un);
        } catch (RuntimeException e) {
            unexpected("А: receive(KB-01, 600, true)", e);
        }
        try {
            Warehouse wh = sampleWarehouse();
            wh.receive("KB-01", 600, true);
            wh.issue("KB-01", 600);
            int un = Warehouse.addUnconfirmedMovements(wh).size();
            expect("А: issue на 600 не требует подтверждения", un == 0 && wh.movementsCount() == 2,
                    "unconfirmed=" + un);
        } catch (RuntimeException e) {
            unexpected("А: issue на 600", e);
        }
        try {
            sampleWarehouse().receive("XX-99", 600, true);
            noError("А: receive(XX-99, 600, true) — R9 раньше подтверждения");
        } catch (IllegalArgumentException e) {
            checkMessage("А: receive(XX-99, 600, true) — R9 раньше подтверждения", e, "code not found");
        }

        // вариант 16, часть Б: addUnconfirmedMovements
        try {
            int size = Warehouse.addUnconfirmedMovements(new Warehouse()).size();
            expect("Б: пустой склад — пустой список", size == 0, "size=" + size);
        } catch (RuntimeException e) {
            unexpected("Б: пустой склад", e);
        }
        try {
            Warehouse wh = sampleWarehouse();
            wh.receive("NB-01", 501);
            wh.receive("MN-01", 10);
            wh.issue("NB-01", 100);
            wh.receive("KB-01", 1000);
            List<StockMovement> un = Warehouse.addUnconfirmedMovements(wh);
            boolean ok = un.size() == 2
                    && un.get(0).equals(new StockMovement("NB-01", MovementType.RECEIVE, 501, false))
                    && un.get(1).equals(new StockMovement("KB-01", MovementType.RECEIVE, 1000, false));
            expect("Б: из 4 операций неподтверждённые только 2 крупных прихода", ok, "unconfirmed=" + un);
        } catch (RuntimeException e) {
            unexpected("Б: из 4 операций", e);
        }
        try {
            Warehouse wh = sampleWarehouse();
            wh.receive("NB-01", 700);
            Warehouse.addUnconfirmedMovements(wh).clear();
            int size = Warehouse.addUnconfirmedMovements(wh).size();
            expect("Б: изменение результата не влияет на историю (R6)",
                    size == 1 && wh.movementsCount() == 1, "size=" + size);
        } catch (RuntimeException e) {
            unexpected("Б: изменение результата", e);
        }
        try {
            Warehouse.addUnconfirmedMovements(null);
            noError("Б: addUnconfirmedMovements(null)");
        } catch (IllegalArgumentException e) {
            checkMessage("Б: addUnconfirmedMovements(null)", e, "warehouse is null");
        }
    }

    private static StockItem notebook() {
        return new StockItem("NB-01", "Notebook", Category.COMPUTER, 12);
    }

    private static Warehouse sampleWarehouse() {
        Warehouse wh = new Warehouse();
        wh.addItem(notebook());
        wh.addItem(new StockItem("MN-01", "Monitor", Category.DISPLAY, 7));
        wh.addItem(new StockItem("KB-01", "Keyboard", Category.PERIPHERAL, 20));
        return wh;
    }

    // ---------- Режим 2: data-driven по sample.csv ----------

    private static void runDataChecks() {
        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(DATA_FILE));
        } catch (IOException e) {
            report("data: чтение " + DATA_FILE, false, e.getMessage());
            return;
        }

        Warehouse wh = new Warehouse();
        int caseNumber = 0;
        for (int i = 1; i < lines.size(); i++) {   // строка 0 — заголовок
            String row = lines.get(i);
            if (row.isBlank() || row.startsWith("#")) {
                continue;
            }
            caseNumber++;
            String[] parts = row.split(";", -1);
            String expected = parts.length > 4 ? parts[4] : "OK";
            String actual = applyRow(wh, parts);
            report("data/" + caseNumber + " (" + row + ")", actual.equals(expected),
                    "ожидалось " + expected + ", получено " + actual);
        }
        System.out.println("Склад после data-driven: позиций " + wh.itemsCount()
                + ", движений " + wh.movementsCount() + ", сводка " + wh.totalsByCategory());
    }

    /**
     * Строка позиции:  code;name;category;quantity;expected  → addItem, результат OK:qty=N или SKIP:причина.
     * Строка операции: RECEIVE|ISSUE;code;-;q;expected       → результат OK:qty=N,confirmed=B или SKIP:причина.
     * Строка части Б:  UNCONFIRMED;-;-;-;OK:count=N          → число неподтверждённых операций.
     */
    private static String applyRow(Warehouse wh, String[] parts) {
        if (parts.length < 4 || parts.length > 5) {
            return "SKIP:wrong number of columns";
        }
        String first = parts[0];
        try {
            switch (first) {
                case "RECEIVE", "ISSUE" -> {
                    int q = parseQuantity(parts[3]);
                    int unconfirmedBefore = Warehouse.addUnconfirmedMovements(wh).size();
                    if (first.equals("RECEIVE")) {
                        wh.receive(parts[1], q);
                    } else {
                        wh.issue(parts[1], q);
                    }
                    boolean confirmed = Warehouse.addUnconfirmedMovements(wh).size() == unconfirmedBefore;
                    return "OK:qty=" + wh.quantityOf(parts[1]) + ",confirmed=" + confirmed;
                }
                case "UNCONFIRMED" -> {
                    return "OK:count=" + Warehouse.addUnconfirmedMovements(wh).size();
                }
                default -> {
                    Category category = parseCategory(parts[2]);
                    int quantity = parseQuantity(parts[3]);
                    StockItem item = new StockItem(parts[0], parts[1], category, quantity);
                    wh.addItem(item);
                    return "OK:qty=" + wh.quantityOf(item.getCode());
                }
            }
        } catch (IllegalArgumentException e) {
            return "SKIP:" + shortReason(e.getMessage());
        }
    }

    // "code not found: XX-99" → "code not found"
    private static String shortReason(String message) {
        int colon = message.indexOf(':');
        return colon > 0 ? message.substring(0, colon) : message;
    }

    private static Category parseCategory(String value) {
        try {
            return Category.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("category not found");
        }
    }

    private static int parseQuantity(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("quantity not a number");
        }
    }

    // ---------- Режим 3: интерактивный ввод ----------

    private static void runInteractive() {
        Warehouse wh = new Warehouse();
        Scanner sc = new Scanner(System.in);
        System.out.println("""
                Интерактивный режим (склад пустой). Команды:
                  add <code> <name> <category> <qty>
                  receive <code> <qty> [confirm]
                  issue <code> <qty>
                  find <category>
                  totals
                  unconfirmed
                  exit""");
        while (true) {
            System.out.print("> ");
            if (!sc.hasNextLine()) {
                break;
            }
            String line = sc.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.equals("exit")) {
                break;
            }
            try {
                System.out.println(execute(wh, line.split("\\s+")));
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private static String execute(Warehouse wh, String[] cmd) {
        switch (cmd[0]) {
            case "add" -> {
                requireArgs(cmd, 5);
                wh.addItem(new StockItem(cmd[1], cmd[2], parseCategory(cmd[3]), parseQuantity(cmd[4])));
                return "Добавлено: " + cmd[1] + ", позиций на складе " + wh.itemsCount();
            }
            case "receive" -> {
                if (cmd.length != 3 && cmd.length != 4) {
                    throw new IllegalArgumentException("usage: receive <code> <qty> [confirm]");
                }
                boolean approved = cmd.length == 4 && cmd[3].equals("confirm");
                wh.receive(cmd[1], parseQuantity(cmd[2]), approved);
                return "Приход: " + cmd[1] + " qty=" + wh.quantityOf(cmd[1])
                        + ", движений " + wh.movementsCount()
                        + ", неподтверждённых " + Warehouse.addUnconfirmedMovements(wh).size();
            }
            case "issue" -> {
                requireArgs(cmd, 3);
                wh.issue(cmd[1], parseQuantity(cmd[2]));
                return "Выдача: " + cmd[1] + " qty=" + wh.quantityOf(cmd[1]) + ", движений " + wh.movementsCount();
            }
            case "find" -> {
                requireArgs(cmd, 2);
                return "Найдено: " + wh.findByCategory(parseCategory(cmd[1]));
            }
            case "totals" -> {
                return "Сводка: " + wh.totalsByCategory();
            }
            case "unconfirmed" -> {
                return "Неподтверждённые: " + Warehouse.addUnconfirmedMovements(wh);
            }
            default -> throw new IllegalArgumentException("unknown command: " + cmd[0]);
        }
    }

    private static void requireArgs(String[] cmd, int count) {
        if (cmd.length != count) {
            throw new IllegalArgumentException(cmd[0] + " expects " + (count - 1) + " arguments");
        }
    }

    // ---------- Помощники проверок ----------

    private static void expect(String name, boolean ok, String actual) {
        report(name, ok, "получено " + actual);
    }

    private static void unexpected(String name, RuntimeException e) {
        report(name, false, "неожиданное исключение " + e);
    }

    private static void noError(String name) {
        report(name, false, "ожидалось IllegalArgumentException");
    }

    private static void checkMessage(String name, IllegalArgumentException e, String expectedPart) {
        boolean ok = e.getMessage() != null && e.getMessage().contains(expectedPart);
        report(name, ok, "ожидалось сообщение с \"" + expectedPart + "\", получено \"" + e.getMessage() + "\"");
    }

    private static void report(String name, boolean ok, String reason) {
        if (ok) {
            passed++;
            System.out.println("PASS " + name);
        } else {
            failed++;
            System.out.println("FAIL " + name + ": " + reason);
        }
    }
}
