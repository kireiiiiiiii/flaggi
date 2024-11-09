#!/bin/bash

cd ~/devel/flaggi/server
javac Server.java
jar cfm Server.jar MANIFEST.MF Server\$Client.class Server.class
java -jar Server.jar
