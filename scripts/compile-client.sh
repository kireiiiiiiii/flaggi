#!/bin/bash

cd ~/devel/flaggi
gradle shadowjar
cd ~/devel/flaggi/app/build/libs
java -jar Flaggi.jar
