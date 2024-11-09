#!/bin/bash

cd ~/devel/TTT
gradle shadowjar
cd ~/devel/TTT/app/build/libs
java -jar Flaggi.jar
