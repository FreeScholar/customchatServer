# customchat
Test site and server - Chatalot.com http://chatalotchat.com:6742 

customchat server - Version 1.8

The code is in /src

Server Requirements JVM

CustomChat Server files that must be compiled to create the customchat.jar file

Customchat Server folders with raw code:
chat
htmlutil
licensekey
util
## Running Locally
To run the chat server on your local machine, you should download the compiled files from CustomChat.com
http://www.customchat.com/downloads

## Building From Source
In order to build the project from source, you will need to take a couple extra steps. We are using [Maven](https://maven.apache.org/index.html) to manage our builds.

`mvn package`

`mvn exec:java -Dexec.mainClass="customchat.licensekey.License"`

This will generate a license file for you called ccLicense. You will need to place this in the TLD of the project (it should go there by default if you executed your command from the TLD.)

### Packaging The JAR
`mvn package`

This will create a .jar archive in the `target/` directory. In order to use this jar, copy it to the TLD. You may run the JAR by executing

`java -jar name-of-your-jar.jar`

### Running Without JAR
`mvn exec:java -Dexec.mainClass="customchat.chat.Server"`

This will launch the server. See terminal's output for details.

