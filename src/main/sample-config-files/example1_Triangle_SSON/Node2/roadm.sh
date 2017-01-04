#!/bin/bash
EMULATOR_HOME=../../
ROCKSAW_HOME=../../
TRANSPORT_NODE_NAME=nodeDemo1
TRANSPORT_NODE_CONFIG_HOME=`pwd`
echo java -Djava.library.path=$ROCKSAW_HOME -Dlog4j.configurationFile=$TRANSPORT_NODE_CONFIG_HOME/log4j2.xml -Dname=$TRANSPORT_NODE_NAME -jar $EMULATOR_HOME/Emulator-jar-with-dependencies.jar
java -Djava.library.path=$ROCKSAW_HOME -Dlog4j.configurationFile=$TRANSPORT_NODE_CONFIG_HOME/log4j2.xml -Dname=$TRANSPORT_NODE_NAME -jar $EMULATOR_HOME/Emulator-jar-with-dependencies.jar

