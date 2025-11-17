# Keep Dishes Going

*Distributed Microservices Platform – Restaurant • Order • Delivery*

Keep Dishes Going is een **DDD-gedreven microservicesplatform** dat restaurants, klantbestellingen en leveringen orchestratet.
Elke service is autonoom, bezit zijn eigen database en communiceert via **RabbitMQ-domeinevents**.
Het project demonstreert **software-architectuur**, **event-driven design**, **service-isolatie** en **bounded contexts**.

---

## Architectuur

### Bounded Contexts / Services

Dit monorepo bevat drie onafhankelijke services:

```
restaurant-service/   → restaurant data, dishes, availability
order-service/        → order flow, validation, preparation
delivery-service/     → drivers, routing, delivery lifecycle
```

### Belangrijkste principes

* **Domain-Driven Design**
  Aggregates, Value Objects, Domain Events (jMolecules)

* **Event-driven integratie (RabbitMQ)**

  * `OrderPlacedEvent`
  * `DishAvailabilityChangedEvent`
  * `DeliveryAssignedEvent`
  * …

* **Autonome services (Database per service)**

  * PostgreSQL per bounded context
  * Neo4j voor route- en graph-data (Delivery)

* **Security**
  Keycloak voor authentication & roles per context

---

## Technische stack

* Java 21
* Spring Boot 3
* RabbitMQ (AMQP)
* PostgreSQL
* jMolecules (DDD)
* Keycloak

---

## Development

### Vereisten

* Java 21
* Docker & Docker Compose
* RabbitMQ
* PostgreSQL
* 
### Start alle services via Docker Compose

```
cd ./restaurant-service/infrastructure/
docker compose up -d
```

Elke service start op een eigen poort en verbindt met zijn eigen database + RabbitMQ instance.

---

## Messaging Flow (Simplified)

```
Restaurant Service → DishAvailabilityChangedEvent → Order Service
Order Service      → OrderPlacedEvent            → Delivery Service
Delivery Service   → DeliveryCompletedEvent      → Order Service
```

---

## Repository structuur

```
keep-dishes-going/
  restaurant-service/
  order-service/
  delivery-service/
  README.md
```

---

## Auteurs

* Mathias Meeus
* Lee
