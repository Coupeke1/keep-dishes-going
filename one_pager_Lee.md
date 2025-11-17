# One Pager Spring ApplicationEvents

## Wat zijn String ApplicationEvents

Spring ApplicationEvents zijn een manier om gebeurtenissen binnen een Spring-applicatie te publiceren en te luisteren,
zonder dat de betrokken beans direct aan elkaar gekoppeld zijn. Dit bevordert **loose coupling** en maakt je applicatie
uitbreidbaar en modulair.

---

## Voorbeeld: `OrderPlacedDomainEvent`

```java
// OrderIntegrationPublisher.java
@Component
public class OrderIntegrationPublisher {

    private final OrderRepository orders;
    private final RestaurantEventPublisher restaurantPublisher;
    private final OrderEventPublisher orderPublisher;

    public OrderIntegrationPublisher(OrderRepository orders,
                                     RestaurantEventPublisher restaurantPublisher,
                                     OrderEventPublisher orderPublisher) {
        this.orders = orders;
        this.restaurantPublisher = restaurantPublisher;
        this.orderPublisher = orderPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPlaced(OrderPlacedDomainEvent evt) {
        Order order = orders.findById(new OrderId(evt.orderId()))
                .orElseThrow(() -> new IllegalStateException("Order not found for integration publish: " + evt.orderId()));

        OrderCreatedEvent created = buildOrderCreatedEvent(order);

        OrderTimeoutEvent timeout = new OrderTimeoutEvent(evt.orderId());

        restaurantPublisher.publishOrderCreated(created);
        orderPublisher.publishOrderTimeout(timeout);
    }
    
    ...
}
```

Een voorbeeld van een **Spring ApplicationEvent** binnen het project van Keep Dishes Going is bij het plaatsen van een
bestelling. Wanneer een klant een bestelling indient, willen we zowel de **restaurant-service** als de **order-service**
op de hoogte brengen van een nieuwe bestelling en een time-out event starten.

Wanneer een bestelling geplaatst wordt:

1. Een `OrderPlacedDomainEvent` wordt aangemaakt binnen de **order-service**.
2. De `OrderIntegrationPublisher` luistert naar dit event met
   `@TransactionalEventListern(phase= TransactionPhase.AFTER_COMIT`.
    - Dit garandeert dat het event pas wordt gepubliceerd nadat de databank-transactie succesvol is doorgevoerd.
3. Binnen de listener worder twee interne events gepubliceerd:
    - `OrderCreatedEvent` -> voor andere interne processen die een nieuwe bestelling nodig hebben.
    - `OrderTimeoutEvent` -> voor het afhandelen van de automatische weigering als een restaurant niet binnen de 5
      minuten reageert.

Deze opzet laat zien hoe events interne workflows modulair en betrouwbaar kunnen sturen zonder dat de service-methodes
direct aan elkaar gekoppeld zijn.

---

## Voordelen

- **Loose coupling:** Componenten reageren op events zonder elkaar direct te kennen.
- **Transaction awareness:** Met `@TransactionEventListener` weet je zeker dat events alleen na een succesvolle commit
  worden verwerkt.
- **Gemakkelijk uitbreidbaar:** Nieuwe features kunnen reageren op bestaande events zonder bestaande code te bewerken.
- **Testbaarheid:** Unit tests kunnen eenvoudig events mocken en de listeners onafhankelijk testen.

---

## Nadelen / Uitdagingen

- **Debuggen kan complex zijn:** Het volgen van de event-flow vereist logging of tracing, anders is het moeilijk om te
  zien wat er gebeurt.
- **Extra infrastructuur en boilerplate:** Voor elk event en listener moet code worden geschreven; bij te veel events
  kan dit leiden tot complexiteit.
- **Event ordering:** Als meerdere events afhankelijk zijn van elkaar, kan de volgorde belangrijk zijn en extra aandacht
  vragen.

---

## Conclusie

Door Spring ApplicationEvents te gebruiken in je applicatie zorg je voor een **heldere, transaction-aware en
uitbreidbare architectuur**. Perfect voor applicatie waarbij interne processen modulair en onafhankelijk van externe
services moeten functioneren.