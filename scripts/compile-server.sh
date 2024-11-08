#!/bin/bash

cd ~/devel/TTT/Server
javac Server.java
jar cfm Server.jar MANIFEST.MF Server\$Client.class Server.class
java -jar Server.jar