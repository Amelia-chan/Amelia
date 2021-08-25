## 🌃 Amelia
[![Build & Release](https://github.com/ManaNet/Amelia/actions/workflows/release.yml/badge.svg)](https://github.com/ManaNet/Amelia/actions/workflows/release.yml)

Amelia is a open-source, forever-free Discord bot written in Java with Javacord that is developed specifically for ScribbleHub. It is used to fetch RSS feeds and updates users when they or a certain author that they like updates or enters into trending.

## 🧰 Requirements
Amelia requires the following to function:
- A server of at least 1.5 gigabytes memory and 2 vCPUs (one for Websocket and one for Discord bot).
- A MongoDB database (recommend to self-host it on the same server as the bot and websocket).
- JDK 11 (preferably OpenJDK or AdoptOpenJDK).

## 💌 Invite
You can invite the bot which runs all-for-free with no limitations here: **__[Invite Amelia now](https://discord.com/api/oauth2/authorize?client_id=786464598835986483&permissions=67488832&scope=bot%20applications.commands)__**

## ☁️ Self-hosting
To self-host Amelia, you need to follow the steps below:
1. Create a Discord Bot Application on https://discordapp.com/developers.
2. Install MongoDB onto where you will host the bot (VPS or some sort of server): https://docs.mongodb.com/manual/tutorial/install-mongodb-on-ubuntu/
3. Install Amelia Websocket (instructions below).
4. Prepare the environmental variables (instructions below).
5. Download the jar from GitHub releases: https://github.com/ManaNet/Amelia/releases.
6. Run the jar with `java -jar Amelia.jar`.

### ⚙️ Websocket Installation
Amelia Websocket, which is a new addition implemented on Amelia 2.0, is a separate process of Amelia which can be said as the heart or the brain
of the Discord bot. The websocket handles the checking for updates on RSS Feeds and also Trending Notifications.

To install the websocket, you need to download the websocket jar from the [weboscket repository's releases page](https://github.com/ManaNet/Amelia-Websocket/releases) and then setup the following environment variables:
```
amelia_auth=AUTHENTICATION
amelia_db=mongodb://user:pass@ip:port
(OPTIONAL) amelia_websocket=ws://ip:3201/
```

After the environment variables are set, you can now start up the websocket with no configuration via: `java -jar Amelia-Websocket.jar`

### 🔧 Client Installation
After the websocket is running, we can start setting up the client. To start, please follow the instructions below:
1. Retrieve your token from [Discord App's Developer Portal](https://discord.com/developers/)
2. Add the token onto your System Environmental Variables as `amelia_token`.
3. Check your Java Version whether it is JDK 11.
4. Download the Client jar file and run it via `java -jar Amelia.jar`

#### 🗞️ JDK Version Example
```
openjdk 11.0.9.1 2020-11-04
OpenJDK Runtime Environment (build 11.0.9.1+1-Ubuntu-0ubuntu1.18.04)
OpenJDK 64-Bit Server VM (build 11.0.9.1+1-Ubuntu-0ubuntu1.18.04, mixed mode, sharing)
```

## 📰 References
- [MongoDB Connection String](https://docs.mongodb.com/manual/reference/connection-string/)
- [Environmental Variables Ubuntu (1)](https://mkyong.com/linux/how-to-set-environment-variable-in-ubuntu)
- [Environmental Variables Ubuntu (2)](https://help.ubuntu.com/community/EnvironmentVariables)
- [Environmental Variables Windows (1)](https://docs.oracle.com/en/database/oracle/r-enterprise/1.5.1/oread/creating-and-modifying-environment-variables-on-windows.html)
- [Environmental Variables Windows (2)](https://www.computerhope.com/issues/ch000549.htm)


## Contribute
- [Amelia Websocket Repository](https://github.com/ManaNet/Amelia-Websocket)
- [Amelia Client Repository](https://github.com/ManaNet/Amelia)
