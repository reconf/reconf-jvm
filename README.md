ReConf JVM
==========

The ReConf JVM Project is a library that provides an easy way to utilize smart configurations in a Java application.

Smart configurations
--------------------

A smart configuration is capable of automatically reloading itself from time to time - no need to restart the application. Each configuration item is a key-value pair written in a special - yet simple - format, which allows the library to create the appropriate Java object according to its type.

Just enough XML
---------------

ReConf Client relies on a simple and straightforward XML configuration file, just enough to know a little bit about the execution environment.


Get rid of boilerplate code
-------------------------------

Just create a plain old Java interface, decorate it with a few custom annotations, call a factory method and that's it! Your application is good to go.