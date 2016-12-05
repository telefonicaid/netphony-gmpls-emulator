netphony-gmpls-emulator v1.3.3
===================
Repository branch build status:

| **Master**  | **Develop**   |
|:---:|:---:|
| [![Build Status](https://travis-ci.org/telefonicaid/netphony-gmpls-emulator.svg?branch=master)](https://travis-ci.org/telefonicaid/netphony-gmpls-emulator) | [![Build Status](https://travis-ci.org/telefonicaid/netphony-gmpls-emulator.svg?branch=develop)](https://travis-ci.org/telefonicaid/netphony-gmpls-emulator) |

Latest Maven Central Release: 

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/es.tid.netphony/gmpls-emulator/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/es.tid.netphony/gmpls-emulator/)

This software is a Java Based emulator of a Transport Network. It is based on the emulator of a Transport Node, which can have L1 (OTN) and L0 (fixed grid or flexi-grid DWDM) equipment, that runs a GMPLS based control plane. In particular, it has a OSPF-TE daemon that sends/receives OSPF-TE packets and a RSVP-TE daemon to set-up LSPs. It maintains a PCEP connection with a domain PCE. If used in stateless mode, the PCEP connection is used for path queries. If used in stateful mode, the PCEP connection can be used for remote initiation of LSPs.

## Compilation and use

The software can be built using the maven tool. 
To build the .jar file and run the tests, clone the repository, go to the main directory and run:
 ```bash
    git clone https://github.com/telefonicaid/netphony-gmpls-emulator.git
    cd netphony-gmpls-emulator
    mvn install
 ```
 To use the library in your application, add the dependency in your pom.xml file:
  ```xml
    <dependency>
      <groupId>es.tid.netphony</groupId>
      <artifactId>network-emulator</artifactId>
      <version>1.3.3</version>
    </dependency>
 ```
  Authors keep also a copy of the artifact in maven central to facilitate the deployment. 
  Note that, in the develop branch is maintained only in snapshots.
  
##Transport Node Emulator

NodeLauncher is the main class to run a GMPLS Node emulator. It represents a Transport Node with GMPLS capabilities (e.g. an OTN node or a ROADM). It launches the OSPF-TE, RSVP-TE and PCEP necessary connectiosn. 

To run the Transport Node Emulator as a standalone application use the class NodeLauncher. You can use maven to create an autoexecutable jar that includes all dependencies in a single file. Plase be aware that you need to start as root.
  ```bash
    git clone https://github.com/telefonicaid/netphony-gmpls-emulator.git
    cd netphony-gmpls-emulator
    mvn clean package -P generate-autojar
    cd target
    sudo java -jar Emulator-jar-with-dependencies.jar 
 ```
 
 Before running, you should configure the parameteres. The parameters are configured in an xml file. By default, if used with NodeLauncher, or it is not specified a file name, XXX.xml should be used. An example of the file is located in examples/xxx.xml (and with the maven assembly build, it is copied in the target directory).

