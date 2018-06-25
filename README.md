# Nirdizati Training UI

Project made as a bachelors thesis in University of Tartu.

This project is a part of a bigger system called [Nirdizati](http://nirdizati.org/).

Source code for Nirdizati can be found [here](https://github.com/nirdizati).

## What is Nirdizati?

Nirdizati is an open-source web-based predictive process monitoring engine for running business processes. The dashboard is updated periodically based on incoming streams of events. However, unlike classical monitoring dashboards, Nirdizati does not focus on showing the current state of business process executions, but also their future state (e.g. when will each case finish). On the backend, Nirdizati uses predictive models pre-trained using data about historical process execution.

More information on this can be found [here](https://eprints.qut.edu.au/109686/).

## What is the goal of this project?

Nirdizati Training component provides possibility for user to upload his own logs in ```.XES``` or ```.CSV``` format, analyze them, construct models using different parameters and then depoy them into Nirdizati Runtime component.

Goal of this project is to remake UI for Nirdizati training component. As a result, Nirdizati Training UI will be remade into more user-friendly and intuitive system.

## About this project

This project contains UI of predictive monitoring web application that can be found [here](https://training.nirdizati.org/)

## Setting up

### Prerequisites

Currently application building process is reliant on a [plugin](https://github.com/Zukkari/SASS-compile-maven-plugin) that compiles SASS to CSS. Once this plugin is installed in your local maven repository, you can configure the application.

### Configuration

The main configuration file is ```config.xml``` found in resources folder of the project. It contains various settings that can be changed when running the application. Most notable of those are directories that will be used by the application (found under the node ```directories``` node). Those should be configured to be existing paths on the filesystem, otherwise application will not be able to start up.

Note that the project has only been built using Java 8 and building is not tested against Java 9 and beyond.

### Building

Application is built using [Maven](https://maven.apache.org/) build system. Once Maven is installed on your system, you can package the project into a ```war``` package by running ```mvn package``` inside the root directory of the project. Please note that the application is relying on ZK EE repository, which requires an access key.

### Deploying

Once application is built it can be deployed to a regular Java servlet container. We are running this on Tomcat 8.5, so other applications are not tested. 

## Student project contest info
Poster repository can be found [here](https://github.com/Zukkari/nirdizati-poster).
