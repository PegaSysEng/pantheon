# Installation from Binary Distribution

## Contents
* [Mac OS](#mac-os)
* [Windows](#windows)
* [Linux / Unix](#linux--unix)

## Mac OS 

### Prerequisites

* [Homebrew](https://brew.sh/)

### Install Using Homebrew

```bash
brew tap pegasyseng/pantheon
brew install pantheon
```
Display Pantheon command line help to confirm installation: 

```bash
pantheon --help
```

## Windows with Chocolatey 

### Prerequisites

* [Chocolatey](Install-Chocolatey)

### Install Using Chocolatey

To install from [Chocolatey package](https://chocolatey.org/packages/pantheon/0.8.2): 

```bash
choco install pantheon
``` 

Display Pantheon command line help to confirm installation: 

```bash
pantheon --help
```

## Linux / Unix / Windows without Chocolatey

### Prerequisites 

* [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

  > **Note** Pantheon requires Java 8+ to compile; earlier versions are not supported.
  >
  > **Note**  Pantheon is currently supported only on 64-bit versions of Windows, and requires a 64-bit version of JDK/JRE. We recommend that you also remove any 32-bit JDK/JRE installations.

### Install from Packaged Binaries

Download the Pantheon [packaged binaries](https://bintray.com/consensys/pegasys-repo/pantheon/_latestVersion#filesß).

Unpackage the downloaded files and change into the `pantheon-<release>` directory. 

Display Pantheon command line help to confirm installation: 

```bash
bin/pantheon --help
```