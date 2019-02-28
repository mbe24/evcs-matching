# webapp-cs

Charging Station Webapplication

### Usage

The frontend webapplication is located in `app/`. There will also be information on the build process.
Build the frontend and put it in `src/main/resources/public` in order to bundle it together with the backend.

Start the CS backend in your IDE or from the command line. There are three backends each has its own flags and arguments.

```shell
$ ../gradlew bootRun
Usage: java -jar webapp-cs.jar [-hV] [-log=<logDir>] [COMMAND]
  -h, --help      Show this help message and exit.
  -log, --log-directory=<logDir>
                  Logging directory
  -V, --version   Print version information and exit.
Commands:
  zmq   Use ZMQ CS backend.
  iota  Use IOTA CS backend.
  stub  Use stub CS backend.
```
