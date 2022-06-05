<div align="center">
    The official mono-repo for Amelia-chan's next version.
</div>

#
### 📦 Modules
The mono-repo contains multiple modules that all handles different sections of Amelia-chan. Here are all the responsibilites of each module:
- **Akari-chan**: Akari-chan is the back-facing module of Amelia which can be called as her brain or senses, she communicates with ScribbleHub and provides the other modules a clean and simple layer of abstraction over RSS feeds and trending notifications.
- **Ame-chan**: Ame-chan is the front-end side of Amelia, she communicates with Discord, manages the database while also keeping a good line of communication with Akari-chan.

### 🗡️ Unit Testing
It is expected that both **Akari-chan** and **Ame-chan** should have some form of unit-testing to ensure that critical issues such as looping and other bothersome issues does not reach production and nuke servers. For contributors of Amelia-chan, it is expected that you should run them after writing your contribution to ensure that it doesn't break any of the critical, testable functions. 

Please refer to the unit testing documentations of each module for more information.

### 🗒️ License
Amelia-chan follows Apache 2.0 license which allows the following permissions:
- ✔ Commercial Use
- ✔ Modification
- ✔ Distribution
- ✔ Patent use
- ✔ Private use

The contributors and maintainers of Amelia-chan are not to be held liability over any creations that uses Amelia-chan. We also forbid trademark use of
the library and there is no warranty as stated by Apache 2.0 license. You can read more about the Apache 2.0 license on [GitHub](https://github.com/Amelia-chan/Amelia/blob/master/LICENSE).
