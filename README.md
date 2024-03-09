# Janilla PetClinic Sample Application

## What's this?

This is a Janilla version of the Spring PetClinic official sample application by [Spring](https://spring.io/).

The original application lives at <https://github.com/spring-projects/spring-petclinic>.

## Understanding the PetClinic application

A database-oriented application designed to display and manage information related to pets and veterinarians in a pet clinic.

## Run PetClinic locally

Janilla PetClinic is a [Janilla](https://www.janilla.com/) application built using [Maven](https://maven.apache.org/users/index.html). You can run it from Maven directly (it should work just as well with Java 21 or newer):

```bash
git clone https://github.com/diego-schivo/janilla-petclinic.git
cd janilla-petclinic
mvn compile exec:java
```

You can then access the PetClinic at <http://localhost:8080/>.

## In case you find a bug/suggested improvement for Janilla PetClinic

Our issue tracker is available [here](https://github.com/diego-schivo/janilla-petclinic/issues).

## Database configuration

In its default configuration, Janilla PetClinic stores its data in a file under the user home directory, which gets populated at startup with data.

You can change the file location by editing `configuration.properties` in the source package.

## Working with Janilla PetClinic in Eclipse IDE

### Prerequisites

The following items should be installed in your system:

- Java 21 or newer
- [Git command line tool](https://help.github.com/articles/set-up-git)
- Eclipse with the [m2e plugin](https://www.eclipse.org/m2e/)

In order to install them all:

1. Download the [Eclipse Installer](https://www.eclipse.org/downloads/packages/installer)
2. Install the package for Enterprise Java and Web Developers with JRE 21.0.1

### Steps

1. Launch Eclipse and choose Import projects from Git (with smart import)
2. Select GitHub as the repository source, then search for `janilla-conduit fork:true` and complete the wizard
3. Select a project (eg: `janilla-conduit-fullstack`) and launch Debug as Java Application
4. Open a browser and navigate to <http://localhost:8080/>

## Looking for something in particular?

| Item | Files |
| ---- | ----- |
| The Main Class| [PetClinicApplication](https://github.com/diego-schivo/janilla-petclinic/blob/main/source/com/janilla/petclinic/PetClinicApplication.java) |
| Configuration File| [configuration.properties](https://github.com/diego-schivo/janilla-petclinic/blob/main/source/com/janilla/petclinic/configuration.properties) |

## Contributing

The [issue tracker](https://github.com/diego-schivo/janilla-petclinic/issues) is the preferred channel for bug reports, feature requests and submitting pull requests.

## License

The Janilla PetClinic sample application is released under version 2.0 of the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).
