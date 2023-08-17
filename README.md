# evcs-matching

Repository that hosts the web applications, the underlying protocol API, and its implementations.

### Usage

Generate sources from protobuf definition with

```sh
$ ./gradlew generateProto
```

The respective sources are not contained in the repository and some IDEs will not generate them automatically.

There are two applications, for the EV and for the CS each, which are located in `webapp-ev/` and `webapp-cs`, respectively.
These backends are programmed against defined interfaces (declared in `protocol-api/`), which are implemented in `protocol-ledger` (uses IOTA and ZMQ) and `protocol-tcp/` (pure ZMQ).

To start the ZMQ CS backend with a ZMQ Router on `localhost:5555` and Paypal, Bitcoin, Litecoin, and Ripple as presented payment options
issue following command

```sh
$ cd webapp-cs/
$ ../gradlew bootRun --args='zmq --endpoint=tcp://*:5555 --payment-option=PayPal,BTC,LTC,XRP'
```
or, alternatively, without Gradle

```sh
$ java -jar webapp-cs.jar zmq --endpoint=tcp://*:5555 --payment-option=PayPal,BTC,LTC,XRP
```
Visit <http://localhost:9000> to view the CS control page.

Then start the EV backend that connects to the server hosted by the CS backend

```sh
cd webapp-ev/
$ ../gradlew bootRun --args='zmq --endpoint=tcp://*:5555'
```
again, alternatively, without Gradle

```sh
$ java -jar webapp-ev.jar zmq --endpoint=tcp://*:5555
```

Visit <http://localhost:8000> to view the EV control page.

Two actuator endpoints for shutdown and restart are enabled and provided under the root context path, i.e. `/shutdown` and `/restart`
Use it like
```sh
$ curl -X POST <ip>:<port>/shutdown
```
