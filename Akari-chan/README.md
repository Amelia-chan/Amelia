<div align="center">
    A fast yet simple and scalable ScribbleHub HTTP API for Amelia-chan.
</div>

#
### 📦 Installation

> **Warning**
> 
> Akari-chan is not created to be a full HTTP API for ScribbleHub. It's intended to be used by Amelia-chan for RSS feeds and related purposes.

Clone the repository:
```shell
git clone https://github.com/ShindouMihou/Flora && cd Flora
```

Configure the required configurations:
```shell
cp .env.example .env && nano .env
```

> **Warning**
>
> Akari-chan heavily uses Redis to reduce the total amount of outgoing ScribbleHub requests, preventing a potential or accidental DoS attack from heavily requesting 
> information. It also uses Redis to cache trending results up to the next GMT+0 1:00 time which is the finalization time of ScribbleHub trending.
> 
> You are expected to have installed Redis via Docker or similar.

You can then start building the docker image for Akari-chan and run the application:
```shell
docker build -t akari .
docker run -d -i -t -p 5056:5056 --env-efile .env --name akari akari:latest
```

> **Note**
> 
> Akari-chan should be available via GitHub Packages once she is finished writing, if you don't want to build her from scratch then you can use 
> the built Docker image from GitHub Packages instead.

### 🧮 Schemas
Akari-chan has two schemas available which can be viewed in a little "preview"-like manner in the `schemas/` folder. All dates that is sent by Akari-chan should be 
in GMT+0 which is to ensure that it is on the same timezone with ScribbleHub.

### 🚀 Routes
There are three actual routes that are useable in Amelia which consists of the following:
- `/user/:id`: This is used to request the RSS feed of the user given in a JSON format.
- `/series/:id`: This is used to request the RSS feed of the series given in a JSON format.
- `/trending`: This is used to request a little general information of the current trending stories.

All routes that serves RSS Feeds have a little assistive parameter that reduces the total response size which is called `after`. These routes always includes an 
`after` field which is a Base64-URL of the `lastBuildDate` which can be added in the query params (`?after=<after field value>`) to filter out the results down to only the chapters that have been published after that date.

The `/trending` route also includes a `cachedUntil` field that should be always the next day's `1:00 GMT+0` since that is what we determine as the best time to collect the latest trending results. ScribbleHub calculates over hundreds of thousands of stories' data starting from `0:00 GMT+0` which from experience has only stabilized after an hour which is why the `cachedUntil` value is always an hour after `0:00 GMT+0`.

### 🗡️ Unit Testing
For contributors, you are expected to write unit tests when possible to ensure that the functionality of this module or other modules depending on this module is not affected during production. All unit tests are written with [MochaJS](https://mochajs.org/) and can be run with the following command:
```shell
npm test
```

### 🗒️ License
Akari-chan follows Apache 2.0 license which allows the following permissions:
- ✔ Commercial Use
- ✔ Modification
- ✔ Distribution
- ✔ Patent use
- ✔ Private use

The contributors and maintainers of Akari-chan are not to be held liability over any creations that uses Amaririsu. We also forbid trademark use of
the library and there is no warranty as stated by Apache 2.0 license. You can read more about the Apache 2.0 license on [GitHub](https://github.com/Amelia-chan/Amelia/blob/master/LICENSE).