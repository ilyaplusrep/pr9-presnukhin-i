# Практическая работа №9: коллекции Java и складской учёт оборудования

Преснухин Илья, ЭФБО-12-25, вариант 16.

Склад хранит позиции в `Map<String, StockItem>` (доступ по инвентарному коду) и историю операций в `List<StockMovement>`. Операции `receive`/`issue` проверяют существование кода и остаток, каждая операция записывается в историю. Правила R1–R9 и изменения варианта описаны в [DECISIONS.md](DECISIONS.md), отчёт — [REPORT.docx](REPORT.docx).

Пакет `ru.edu.pr06` сохранён по требованию задания (исторический номер).

## Вариант 16

- Часть А: приход больше 500 единиц требует подтверждения. В `StockMovement` добавлено поле `confirmed`; `receive(code, q)` записывает такой приход с `confirmed = false`, `receive(code, q, true)` — как подтверждённый.
- Часть Б: `Warehouse.addUnconfirmedMovements(Warehouse wh)` → `List<StockMovement>` — неподтверждённые операции.

## Структура

```
src/ru/edu/pr06/
  Category.java        enum категорий
  MovementType.java    enum RECEIVE / ISSUE
  StockItem.java       позиция: инварианты R7, R8, increase/decrease (R2), equals/hashCode по code
  StockMovement.java   record операции (+ confirmed, вариант 16)
  Warehouse.java       склад: addItem, receive, issue, findByCategory, totalsByCategory, addUnconfirmedMovements
  SelfCheck.java       unit-проверки, data-driven по sample.csv, интерактивный режим
data/sample.csv        позиции и операции с ожидаемым результатом
diagrams/              activity.dot/.png, class.dot/.png
```

## Сборка и запуск (Java 21+, без Maven)

Windows:
```
run_selfcheck.bat       проверки
run_interactive.bat     проверки, затем команды с консоли
```

Вручную:
```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out ru.edu.pr06.SelfCheck                 # проверки
java -cp out ru.edu.pr06.SelfCheck --interactive   # + интерактивный режим (можно -i)
```

Команды интерактивного режима:
```
add <code> <name> <category> <qty>     add NB-01 Notebook COMPUTER 12
receive <code> <qty> [confirm]         receive NB-01 600
issue <code> <qty>                     issue NB-01 3
find <category>                        find COMPUTER
totals
unconfirmed
exit
```

## Формат sample.csv

Заголовок `code;name;category;quantity;expected`, строки с `#` — комментарии.

- Позиция: `NB-01;Notebook;COMPUTER;12;OK:qty=12` → `addItem`; ожидание `OK:qty=N` или `SKIP:<причина>`.
- Операция над загруженным складом: `RECEIVE;KB-01;-;501;OK:qty=1021,confirmed=false` (или `ISSUE`).
- Проверка части Б: `UNCONFIRMED;-;-;-;OK:count=2`.
