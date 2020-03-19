## Дипломная работа AQA

### Документация
[План автоматизации](https://github.com/ELvovo7/netology-aqa-diplom/blob/master/docs/Plan.md)

[Отчёт по итогам тестирования](https://github.com/ELvovo7/netology-aqa-diplom/blob/master/docs/report.md)

[Отчет по итогам автоматизации](https://github.com/ELvovo7/netology-aqa-diplom/blob/master/docs/Summary.md)

#### Подготовка и запуск теста

1. Клонировать репозиторий
    * ```git clone https://github.com/ELvovo7/netology-aqa-diplom.git```
1. Перейти в каталог со скачанным содержимым репозитория и скачать докер-контейнеры
    * ```cd ./netology-aqa-diplom/```
1. Запуск контейнеров Docker и эмулятора биллинга
    * ```docker-compose up -d --quiet-pull --build```
1. Запуск SUT с поддержкой MySQL через отдельный терминал (для Windows команда "cmd" в меня поиска)
   * ```java -Dspring.datasource.url=jdbc:mysql://localhost:3306/app -jar artifacts/aqa-shop.jar```
1. **ИЛИ** Запуск SUT с поддержкой Postgres через отдельный терминал (для Windows команда "cmd" в меня поиска)
   * ```java -Dspring.datasource.url=jdbc:postgresql://localhost:5432/app -jar artifacts/aqa-shop.jar```
1. Запуск тестов с MySQL
   * ```gradlew -Ddb.url=jdbc:mysql://localhost:3306/app clean test```
1. **ИЛИ** Запуск тестов с Postgres
   * ```gradlew -Ddb.url=jdbc:postgresql://localhost:5432/app clean test```


#### Отчёт Allure
Для генерации отчёта нужно выполнить команду ```gradlew allureReport allureServe```

#### Окончание тестов и остановка контейнеров

   * Прервать выполнение SUT по Ctrl+C в терминале Windows (или закрытием окна терминала)
   * Остановить контейнеры командой ```docker-compose down```