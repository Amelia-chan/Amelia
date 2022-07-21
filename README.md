<div align=center>
  Luminous Amelia 🌃
</div>

#

An open-source, forever-free Discord bot written in Kotlin-Java with Javacord and Nexus designed for ScribbleHub RSS feeds. Amelia offers an alternative to the likes of MonitoRSS in handling ScribbleHub RSS feeds with complete support of both author feeds and story feeds in ScribbleHub.

## 💭 Luminous Amelia?

Luminous Amelia is the second major iteration of Amelia which takes notes and lessons from her sisters, Mana and Bellus, to provide a more optimized, cleaner and stable experience compared to her previous iterations. Unlike her previous iterations, she has more functions removed (e.g. the trending feature, custom formats, etc.) to make way for simplicity.

### 🖱️ Self-Hosting

You can set up Amelia's client by running the simple command below.

> **Warning**
>
> This assumes that you have Docker installed otherwise please install Docker first.

> **Note**
>
> You can skip the build part if you plan on using the ones produced by GitHub Actions.
> The builds can be found on [Packages](https://github.com/Amelia-chan/Amelia/pkgs/container/amelia).
```shell
git clone https://github.com/ManaNet/Amelia && cd Amelia && docker build -t amelia .
```

> **Warning**
>
> This assumes that you have MongoDB setup otherwise please create an instance by running the following command:
> ```shell
> docker volume create amelia_db && docker run -d -i -t --name amelia_db --restart=always -e MONGO_INITDB_ROOT_USERNAME=amelia -e MONGO_INITDB_ROOT_PASSWORD=SOME_SECURE_PASSOWORD_HERE_ANYONEEEEEE -p 27017:27017 -v amelia_db:/data/db mongo
>```
>
> If you are using the following command above, then use this as the MongoDB URI: `mongodb://amelia:SOME_SECURE_PASSOWORD_HERE_ANYONEEEEEE@172.17.0.1:27017`

> **Note**
> 
> When writing host machine addresses (e.g. 0.0.0.0 or 127.0.0.1), please use the Docker host address instead which should be 172.17.0.1 always.
> Since Docker is isolated and the original 127.0.0.1 or 0.0.0.0 routes back to the container itself.

Please configure the `.env` file before continuing, here is a shortcut command to creating the .env file:
```shell
cp .env.example .env && nano .env
```

Afterwards, you can run the following command:
```shell
docker run --name amelia-client --env-file .env amelia:latest
```

# 📚 License
Amelia follows Apache 2.0 license which allows the following permissions:
- ✔ Commercial Use
- ✔ Modification
- ✔ Distribution
- ✔ Patent use
- ✔ Private use

The contributors and maintainers of Amelia are not to be held liability over any creations that uses Amelia. We also forbid trademark use of
the library and there is no warranty as stated by Apache 2.0 license. You can read more about the Apache 2.0 license on [GitHub](https://github.com/ShindouMihou/Amelia/blob/master/LICENSE).
