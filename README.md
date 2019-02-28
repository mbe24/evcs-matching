# evcs-matching

Repository that hosts the web applications, the underlying protocol API, and its implementations.

### Usage

Generate sources from protobuf definition with

```shell
$ ./gradlew generateProto
```

The respective sources are not contained in the repository and some IDEs will not generate them automatically.

There are two application, for the EV and the CS, which are located in `webapp-ev/` and `webapp-cs`, respectively.
