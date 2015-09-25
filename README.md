# Reactive application with frontend and backend cooperating in akka cluster

The application is splitted into fronted and backend projects. They share common messages API through api project. 
Additionally, the api project conatins `akka.pubsub` package with implementation of durable subscription.

It was presented at [Scala World Unconference 2015](https://scala.world/unconference)
## Durable subscription in Akka
Durable subscription allows frontend application to subscribe for events in Akka cluster. To implement durable subscription in Akka I have used Akka persistence and distributed publish-subscribe. The idea is to use persistent actors as publishers and persistent views as subscribers.

There are three different types of durable subscription:

1. Persistent mode - which is simplest and uses only Akka persistence (without distributed publish-subscribe). A persistent view (subscriber) only reads events periodically
2. Triggered mode - subscriber (persistent view) is triggered with distributed pub-sub to fetch events from database. It reduces number of database reads.
3. **Direct mode** - subscriber receives events directly with distributed pub-sub and needs to read from database only in case of missed messages - when starting or recovering.

## Running application
To run the application you need a database and its driver for Akka Persistence in version **2.4**. 
The app is configured to work with Cassandra and the driver had to be installed locally from snapshot version. It takes a few seconds, but for further info and official releases look at [Cassandra plugin Github](https://github.com/krasserm/akka-persistence-cassandra/)
