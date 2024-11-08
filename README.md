# Amelia-chan

An open-source Discord bot designed for ScribbleHub RSS feeds to get notifications for chapter updates. 
Amelia-chan offers an alternative to bots such as MonitoRSS in handling ScribbleHub RSS feeds with complete support for author and reading list feeds.

### 📚 Features
- [x] **Author Feed Notifications**: Get notified when an author posts a new chapter on all their stories.
- [x] **Reading List Feed Notifications**: Get notified when a story on your reading list has a new chapter.
- [x] **Infinite, Free Feeds**: You can add as many feeds as you want, there is no limit.
- [x] **Open-source**: Amelia-chan is open-source and you can contribute to it, or even self-host it yourself.

### 🖱️ Self-Hosting

You can set up Amelia's client by running the simple command below.

> **Warning**
>
> This assumes that you have Docker installed otherwise please install Docker first.
> **Amelia-chan WILL NOT WORK without Tony (ScribbleHub's Owner) WHITELISTING your IP address first.**

> **Note**
>
> You can skip the build part if you plan on using the ones produced by GitHub Actions.
> The builds can be found on [Packages](https://github.com/Amelia-chan/Amelia/pkgs/container/amelia).
```shell
git clone https://github.com/Amelia-chan/Amelia && cd Amelia && docker build -t amelia .
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

### 🚀 Contributing

For developers wishing to contribute to Amelia's code, here are some other things that you may want to take note of:
1. **Code Style**: We prefer you have **ktlint** installed and use it to format your code.
2. **Proxy**: ScribbleHub cannot be scraped without proper whitelisting, you may use [`scribble-proxy`](https://github.com/Amelia-chan/scribble-proxy) to 
proxy requests from a whitelisted server to ScribbleHub. We do not provide the server for you, you must set it up yourself and ask Tony to whitelist your
server's IP address. You can configure the proxy settings at the `.env` file.
3. **Testing**: We do not have a testing suite yet, so please test your code before submitting a PR.
4. **Issues**: If you find any issues, please report them on the [`Issues`](https://github.com/Amelia-chan/Amelia/issues) page.

# 📚 License
Amelia follows Apache 2.0 license which allows the following permissions:
- ✔ Commercial Use
- ✔ Modification
- ✔ Distribution
- ✔ Patent use
- ✔ Private use

The contributors and maintainers of Amelia are not to be held liability over any creations that uses Amelia. We also forbid trademark use of
the library and there is no warranty as stated by Apache 2.0 license. You can read more about the Apache 2.0 license on [GitHub](https://github.com/ShindouMihou/Amelia/blob/master/LICENSE).
