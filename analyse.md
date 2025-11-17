# V1

## Pre coaching

### Geschatte progress: 10%

#### Status

Het begin verliep heel moeizaam door de vage manier hoe JPA werkt. Maar het begint al vlotter en vlotter met vaak nog
problemen waar we al doende uit leren.
Heel vaak zitten we lang

#### Quality

De code is niet meteen altijd optimaal volgens de DDD-principes. Maar al doende merken we fouten en refactoren we waar
nodig
We moeten ook nog meer van de speciale functies van spring leren implementeren en gebruiken

#### Vragen

Moeten we nu enkel voor Restaurant-service een front-end maken of voor ze allemaal want Jan had enkel voor 1 een demo
gegeven terwijl ik denk dat ze allemaal een front en back -end nodig hebben

## Post Coaching

#### status

we hadden betere code dan verwacht DDD zat relatief goed buiten dat in restaurant-service een owner aggragate-root was
van restaurant --> opgelost
mappenstructuur aangepast zonder hoofdletters en meeste van de ongebruikte code verwijdert

# V2

## Pre Coaching

### Feedback

```text
flush weghalen in restaurant repository

dishes moeten tegelijk in draft en published staan

owners weg gooien --> keycloak

ownerservice --> validatie voor op dto's --> doet niks

restaurantOrderservice --> jpa en rabbit --> moet een implementatie detail --> service/interface

restaurantMessagehandler ook service niet zomaar jpa 

transaf51c564a-c538-4f63-b96b-85734df33b94

rare transactionhandeler weghalen

@Crossorigin weghalen in deliveryservice

createDelivery/driver kan via listener en rest api 
```

### Geschatte progress: 60%

#### Status

We beginnen jpa al wat door te hebben waardoor alles nu veel vlotter vooruit gaat, zijn aan het werken met rabbitMQ
zodat de restaurant en order en delivery service elkaar kunnen communiceren wat redelijk vlot verloopt.
We moeten vooral nog veel front-end implementeren en de testing schrijven

#### Quality

We proberen dat de code goed volgens DDD-principes en we goed de best practises gebruiken en naar mijn gevoel lukt het
wel

## Post Coaching

#### Vragen

geen vragen momenteel

# V3

## Pre Coaching

### Geschatte progress: 100%

#### Status

Alle user stories zijn geimplementeerd hier en daar is er wel nog ruimte voor verteringen of nice to have's.

#### Quality

We proberen de code wo goed mogelijk volgens de DDD-principes en best practices te implementeren. 