# Pantheon Installation

> **Note**  If you have installed Pantheon from the [packaged binaries](https://bintray.com/consensys/pegasys-repo/pantheon/0.8.1#files) or are running the [Docker image](https://hub.docker.com/r/pegasyseng/pantheon/), you do not need to follow the procedures in this section. You can proceed with the [Basic Quickstart](Quickstart) or [Starting Pantheon](Starting-Pantheon).


## Contents

* [Prerequisites](#prerequisites)

### Running Locally

* [Installation on Linux / Unix / Mac OS X](#installation-on-linux--unix--mac-os-x)
* [Installation on Windows](#installation-on-windows)

### Running Virtually

* [Installation on VM](#installation-on-vm)


## Prerequisites

* Disk space and RAM: Your computer should have at least 4 GB RAM. Disk space needed varies depending on the network on which you run nodes. A small test network might require 200 MB while a mainnet node might require 1.5TB. If syncing a node on mainnet, allow 1.5 TB to 2 TB for the full blockchain archive.

* [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

  > **IMPORTANT:** Pantheon requires Java 8+ to compile; earlier versions are not supported.

* [Git](https://git-scm.com/downloads) or [GitHub Desktop](https://desktop.github.com/)


## Installation on Linux / Unix / Mac OS X

#### Clone the Pantheon Repository

Clone the **PegaSysEng/pantheon** repo to your home directory (`/home/<user>`):

```
git clone --recursive https://github.com/PegaSysEng/pantheon.git
```

#### Build Pantheon

After the cloning process completes, go to the `pantheon` repo directory and run `git branch` to make sure you're in the branch in which you cloned the Pantheon source code (by default this is the `master` branch).

Build the pantheon executable using `gradlew`. You can use the `-x test` option to exclude tests, which can take a long time to run and are not necessary for initial build:

```
./gradlew build -x test
```

Continue with [Basic Quickstart](Quickstart), [Private Network Quickstart](Private-Network-Quickstart), or [Starting Pantheon](Starting-Pantheon).


## Installation on Windows

> **Note**  Pantheon is currently supported only on 64-bit versions of Windows, and requires a 64-bit version of JDK/JRE. We recommend that you also remove any 32-bit JDK/JRE installations.

#### Install Pantheon

In Git bash, go to your working directory for repositories. Clone the `PegaSysEng/pantheon` repo into this directory:

```
git clone --recursive https://github.com/PegaSysEng/pantheon
```

This creates a `pantheon` directory under Git revision control that will contain the cloned repo contents.

#### Build Pantheon

Go to the `pantheon` directory:

```
cd pantheon
```

Open a Windows command prompt (the following won't work in Git bash). Build Pantheon with the Gradle wrapper `gradlew`, omitting tests as follows:

```
gradlew build -x test
```

> **Note** To run `gradlew`, you must have the **JAVA_HOME** system variable set to the Java installation directory.
For example: `JAVA_HOME = C:\Program Files\Java\jdk1.8.0_181`.

After the build completes, your system is ready to run Pantheon. See [Starting Pantheon](Starting-Pantheon) and other sections of [Getting Started](Getting-Started) for guidance on using Pantheon.


## Installation on VM

You can run Pantheon on a virtual machine (VM) on a cloud service such as AWS or Azure, or locally using a VM manager such as [VirtualBox](https://www.virtualbox.org/).

If you set up your own VM locally using a VM manager such as [VirtualBox](https://www.virtualbox.org/), there are a few considerations:

* Make sure that Intel Virtualization Technology (VTx) and Virtualization Technology for Directed I/O (VT-d) are enabled in BIOS settings.

* On Windows, you might need to disable Hyper-V in the Windows Feature list.

It is recommended that you create a VM with the following attributes:

* Memory Size: Set to 4096 (recommended)

* Create a virtual hard disk with at least 10 GB; 20 GB is recommended

* Virtual hard disk file type: VDI (if you need to share it with other apps, use VHD)

* (Optional) You can create a shared directory in order to copy block files or genesis files from the host computer to the VM. For details on how to create a shared directory, see "Share Folders" in [Install Ubuntu on Oracle VirtualBox](https://linus.nci.nih.gov/bdge/installUbuntu.html).
