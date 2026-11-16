# audio-2d-room

## Run the room planner

This Java program starts a small web server that serves the room planner to a
browser. Modern browsers do not run Java applets, so the interactive editor is
served as a web page by the Java program.

Requires JDK 17 or newer. From this directory, run:

```sh
java audio-2d-room.java
```

Then open [http://localhost:8080](http://localhost:8080). To use another port,
pass it as the first argument, for example `java audio-2d-room.java 9000`.

Drag furniture and fixtures into the room, move items to arrange them, and use
the selection panel to rotate or remove them. Room dimensions can be changed
between 3 m and 12 m. **Save layout** stores the plan in that browser, and
**Export layout** downloads it as JSON.