# Amelia
Amelia is a simple Discord bot that is used to fetch RSS feeds, and updates users
about a certain author updating their stories.

## Requirements
The specification requirements for Amelia are as of follows:
- OpenJDK 11 (or JDK 11)
- A MongoDB server.
- A server that is at least 1.5 gigabytes of memory and 2 vCPUs.

## How to install, public bot.

The Amelia bot is already hosted publicly on our servers, as such, you can add it to your own
discord server by inviting it from our website. [Invite the bot now](https://discord.com/oauth2/authorize?client_id=786464598835986483&scope=bot&permissions=67488832)

## How to install, self-hosting.
If you want to install this on your own server, first, create a Discord bot application on https://discordapp.com/developers,
after which, you need to have a MongoDB server installed on either a Windows or a Ubuntu server, I will not go into detail
over how to do this but if you need a good tutorial, then the official MongoDB website is the way to go.

https://docs.mongodb.com/manual/tutorial/install-mongodb-on-ubuntu/

### Setting up Amelia Websocket.
Amelia Websocket, which is a new addition implemented on Amelia 2.0, is a separate process of Amelia which can be said as the heart or the brain
of the Discord bot. The websocket handles the checking for updates on RSS Feeds and also Trending Notifications.

### Installation of Amelia Websocket.
To install the websocket, you need to download the websocket jar from the [releases page](https://github.com/ManaNet/Amelia/releases) and then setup the following
environment variables.
```
amelia_auth=AUTHENTICATION
amelia_db=mongodb://user:pass@ip:port
(OPTIONAL) amelia_websocket=ws://ip:3201/
```

After the environment variables are set, you can now start up the websocket with no configuration via: `java -jar Amelia-Websocket.jar`.

### Setting up Amelia Client.
After the Websocket application is running, you can now start with the Client application.

#### Instructions
1. Retrieve your token from [Discord App's Developer Portal](https://discord.com/developers/)
2. Add the token onto your System Environmental Variables as `amelia_token`.
3. Check your Java Version whether it is JDK 11.
4. Download the Client jar file and run it via `java -jar Amelia.jar`

#### JDK Version Example
After all of that is done, make sure you have Java 11 by opening your terminal and using `java --version`, it should return something amongst the likes of this.
```
openjdk 11.0.9.1 2020-11-04
OpenJDK Runtime Environment (build 11.0.9.1+1-Ubuntu-0ubuntu1.18.04)
OpenJDK 64-Bit Server VM (build 11.0.9.1+1-Ubuntu-0ubuntu1.18.04, mixed mode, sharing)
```

## Output Image Examples
![Amelia Websocket](https://media.discordapp.net/attachments/733025925683347596/851566821521227826/unknown-66.png?width=1373&height=533)
![Amelia Client](https://media.discordapp.net/attachments/733025925683347596/851566821761089576/unknown-102.png)

## Extra Links
For more details about connection strings, please head to the official MongoDB documentation site.

https://docs.mongodb.com/manual/reference/connection-string/

For information about setting environmental variables on Ubuntu:

https://mkyong.com/linux/how-to-set-environment-variable-in-ubuntu

https://help.ubuntu.com/community/EnvironmentVariables

For information about setting environmental variables on Windows:

https://docs.oracle.com/en/database/oracle/r-enterprise/1.5.1/oread/creating-and-modifying-environment-variables-on-windows.html

https://www.computerhope.com/issues/ch000549.htm
