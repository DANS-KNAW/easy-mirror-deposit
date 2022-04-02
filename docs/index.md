easy-mirror-deposit
===================
Mirrors Data Station datasets in EASY.

SYNOPSIS
--------

    easy-mirror-deposit { server | check }

DESCRIPTION
-----------
Mirrors Data Station datasets in EASY. The mirror is implemented as a landing page in EASY/Fedora + a Dataverse Dataset Version Export (DVE) in a dedicated EASY
Mirror Store.


ARGUMENTS
---------

        positional arguments:
        {server,check}         available commands
        
        named arguments:
        -h, --help             show this help message and exit
        -v, --version          show the application version and exit

INSTALLATION AND CONFIGURATION
------------------------------
Currently this project is built as an RPM package for RHEL7/CentOS7 and later. The RPM will install the binaries to
`/opt/dans.knaw.nl/easy-mirror-deposit` and the configuration files to `/etc/opt/dans.knaw.nl/easy-mirror-deposit`.

For installation on systems that do no support RPM and/or systemd:

1. Build the tarball (see next section).
2. Extract it to some location on your system, for example `/opt/dans.knaw.nl/easy-mirror-deposit`.
3. Start the service with the following command
   ```
   /opt/dans.knaw.nl/easy-mirror-deposit/bin/easy-mirror-deposit server /opt/dans.knaw.nl/easy-mirror-deposit/cfg/config.yml 
   ```

BUILDING FROM SOURCE
--------------------
Prerequisites:

* Java 8 or higher
* Maven 3.3.3 or higher
* RPM

Steps:

    git clone https://github.com/DANS-KNAW/easy-mirror-deposit.git
    cd easy-mirror-deposit 
    mvn clean install

If the `rpm` executable is found at `/usr/local/bin/rpm`, the build profile that includes the RPM packaging will be activated. If `rpm` is available, but at a
different path, then activate it by using Maven's `-P` switch: `mvn -Pprm install`.

Alternatively, to build the tarball execute:

    mvn clean install assembly:single
