# Spring Boot SSE Notifications — учебный пример

Компактный backend на Java 17 и Spring Boot показывает одностороннюю доставку событий от сервера в браузер через Server-Sent Events. База данных и отдельный frontend-проект не нужны: страница находится в `src/main/resources/static`, история хранится в памяти процесса.

## Как работает

1. Браузер подключается к `GET /api/events` через `EventSource`.
2. Сервер держит открытое HTTP-соединение и отправ��яет именованные события `connected` и `notification`.
3. Форма отправляет новое уведомление отдельным `POST /api/notifications`; сервер рассылает его всем подключённым вкладкам.
4. История последних 50 уведомлений и список SSE-клиентов находятся в памяти. После перезапуска приложения данные исчезнут.

## Технологии

- Java 17, Spring Boot 3.4.3, Spring MVC `SseEmitter`
- Gradle Kotlin DSL (`build.gradle.kts`)
- Обычная HTML-страница + JavaScript `EventSource` без React/Vite
- JUnit 5, Spring Boot Test

## Запуск

Требуются JDK 17+ и Gradle 8.x. Из корня проекта:

```bash
gradle bootRun
```

Откройте http://localhost:8080. Откройте страницу в нескольких вкладках, отправьте уведомление и наблюдайте, как оно появляется в каждой.

## API

- `GET /api/events` — SSE-поток `text/event-stream`
- `POST /api/notifications` — опубликовать уведомление, JSON: `{"message":"Привет!"}`
- `GET /api/notifications` — последние уведомления из памяти
- `GET /api/status` — число подключённых SSE-клиентов

Пример публикации через curl:

```bash
curl -X POST http://localhost:8080/api/notifications \
  -H 'Content-Type: application/json' \
  -d '{"message":"Привет из curl"}'
```

Посмотреть поток отдельно в другом терминале:

```bash
curl -N http://localhost:8080/api/events
```

## Что важно заметить

- SSE передаёт данные только от сервера к клиенту; для отправки используется обычный POST.
- `EventSource` в браузере автоматически переподключается после разрыва.
- Это однопроцессный учебный пример: при нескольких экземплярах приложения рассылка не синхронизируется между ними.
- Не добавляйте `block()` или долгие синхронные операции в обработчик SSE; для production учитывайте тайм-ауты, heartbeat и лимиты соединений.
