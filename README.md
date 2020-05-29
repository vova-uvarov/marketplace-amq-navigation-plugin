<h1>Marketplace amq navigation plugin</h1>
Simple plugin for inner project in my current work. Help navigate bitwise senders and listeners for amq
There is a project in which consists of 2 parts (front and back). Communication between them occurs through the queue.
From the front, there are such calls:

```
amqUtil.sendAndReceiveMessage(CONVERSION_DOCUMENT, BATCH, batchRequestDto, BATCH_REF);
```

Где 1-й и 2-й параметры определяют объект и действие которое надо сделать на бэк части. По ним на бэк части можно найти слушателя.
Эти параметры не всегда передаются как enum, а могут быть переменными, вызовами метода и так далее.

Соответсвенно со стороны бэк части есть слушатели которые настраиваются через @Configuration примерно так

```
listener.configure(CONVERSION_DOCUMENT, VALIDATE, ENTITY_TYPE_REF, backendService::validate);
```

Тут так же 1-й и 2-й параметры определяют объект и действие. Они так же могут быть не только константами. 
Плагин как раз рисует маркеры для навигации между amqUtil.sendAndReceiveMessage и соответсвующим ему listener.configure
