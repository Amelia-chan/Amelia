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
```shell
git clone https://github.com/ManaNet/Amelia && cd Amelia && docker build -t amelia .
```

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
