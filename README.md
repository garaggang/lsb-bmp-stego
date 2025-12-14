# Вложение текста в BMP по алгоритму НЗБ (LSB)

## Основная информация
Приложение:
- запрашивает путь к **оригинальному BMP** и к **файлу результата**;
- принимает текст для вложения;
- встраивает сообщение в **младшие биты** каналов R/G/B (LSB / НЗБ);
- показывает **оригинал** и **визуализацию младших битов** результата.

Дополнительно (для максимальных баллов):
- логирование (Log4j2) в консоль и файл `logs/app.log`;
- сборка Gradle + fat-jar (`shadowJar`);
- unit-тесты JUnit 5;
- JavaDoc на основные классы/методы;
- CLI-режим для запуска из консоли.

## Запуск (IDE)
Откройте проект в IntelliJ IDEA как Gradle-проект и запустите `com.example.stego.App`.

## Сборка JAR
### Вариант 1 (рекомендуется): через IntelliJ
Gradle tool window → Tasks → shadow → `shadowJar`

Итоговый файл:
`build/libs/lsb-bmp-stego-1.0.0-all.jar`

### Вариант 2: через консоль (если Gradle установлен)
```bash
gradle clean test shadowJar
```

## Запуск JAR (GUI)
```bash
java -jar build/libs/lsb-bmp-stego-1.0.0-all.jar
```

## CLI-режим (для демонстрации запуска из консоли)
Вложить:
```bash
java -cp build/libs/lsb-bmp-stego-1.0.0-all.jar com.example.stego.Cli embed --in input.bmp --out out.bmp --text "test"
```

Извлечь:
```bash
java -cp build/libs/lsb-bmp-stego-1.0.0-all.jar com.example.stego.Cli extract --in out.bmp
```
