### Amelia, a Discord Bot of Waters.

Amelia is a simple Discord bot that is used to fetch RSS feeds, and updates users
about a certain author updating their stories.

#### Requirements
- Java 11.
- MongoDB.
- A server, naturally.

#### How to install.
If you want to install this on your own server, first, create a Discord bot application on https://discordapp.com/developers,
after which, you need to have a MongoDB server installed on either a Windows or a Ubuntu server, I will not go into detail
over how to do this but if you need a good tutorial, then the official MongoDB website is the way to go.
https://docs.mongodb.com/manual/tutorial/install-mongodb-on-ubuntu/

Now, that you have both the MongoDB server and the Discord bot application, grab your token from the Discord App's Developer Portal,
then add it into your System Environmental Variables as `amelia_token` before adding the connection string of your MongoDB server
to the variables as `amelia_db`.

For more details about connection strings, please head to the official MongoDB documentation site.
https://docs.mongodb.com/manual/reference/connection-string/

For information about setting environmental variables on Ubuntu:
https://mkyong.com/linux/how-to-set-environment-variable-in-ubuntu/
https://help.ubuntu.com/community/EnvironmentVariables

For information about setting environmental variables on Windows:
https://docs.oracle.com/en/database/oracle/r-enterprise/1.5.1/oread/creating-and-modifying-environment-variables-on-windows.html
https://www.computerhope.com/issues/ch000549.htm

After all of that is done, make sure you have Java 11 by opening your terminal and using `java --version`, it should return something amongst the likes of this.
```
openjdk 11.0.9.1 2020-11-04
OpenJDK Runtime Environment (build 11.0.9.1+1-Ubuntu-0ubuntu1.18.04)
OpenJDK 64-Bit Server VM (build 11.0.9.1+1-Ubuntu-0ubuntu1.18.04, mixed mode, sharing)

```
or it could be something higher the JDK 11, that is fine.

Now, that everything is done, simply head to the Releases section of this Github page and download the Fat .jar
from there, and place it on a place where you can access then simply call `java -jar Amelia.jar` and it should be running.
