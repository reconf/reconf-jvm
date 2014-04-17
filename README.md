# ReConf JVM in 30 seconds

The ReConf JVM Project is a library that provides an easy way to utilize smart configurations in a Java application.

### Smart configurations

A smart configuration is capable of automatically reloading itself from time to time - no need to restart the application. Each configuration item is a key-value pair written in a special - yet simple - format, which allows the library to create the appropriate Java object according to its type.

### Just enough XML

ReConf Client relies on a simple and straightforward XML configuration file, just enough to know a little bit about the execution environment.

### Get rid of boilerplate code

Just create a plain old Java interface, decorate it with a few custom annotations, call a factory method and that's it! Your application is good to go.

# ReConf JVM Integration Guide

#### Table of Contents 

* [What are the benefits of using it?](#what-are-the-benefits-of-using-it)
* [And the minimum requirements?](#and-the-minimum-requirements)
* [How can I use it?](#how-can-i-use-it)
 * [A few concepts first](#a-few-concepts-first)
 * [Import our Maven dependency](#import-our-maven-dependency)
 * [Configure your reconf.xml file](#configure-your-reconfxml-file)
 * [Create a Configuration Repository](#create-a-configuration-repository)
 * [Using a ConfigurationRepository](#using-a-configurationrepository)
 * [Native types built automatically](#native-types-built-automatically)
* [Advanced Features](#advanced-features)
 * [Setting up items with different characteristics](#setting-up-items-with-different-characteristics)
 * [Updating a ConfigurationRepository through code](#updating-a-configurationrepository-through-code)
 * [ConfigurationRepository reuse through Customizations](#configurationrepository-reuse-through-customizations)
 * [Organizing log messages](#organizing-log-messages)
 * [Localization with reconf.xml](#localization-with-reconfxml)
 * [Reading from different configuration file](#reading-from-different-configuration-file)
 * [Integrating with Spring](#integrating-with-spring)
 * [Using Customizations with Spring](#using-customizations-with-spring)
 * [Events and Listeners](#using-notifications)
* [Troubleshooting](#troubleshooting)

<a name="what-are-the-benefits-of-using-it"/>
## What are the benefits of using it?

It can provide the following capabilities to a Java application
* **Automatic update of configuration items** - from time to time the framework retrieves the latest configuration from the configuration server and handles the updated version to the application.
* **Atomic configuration retrieval** - if something fails, nothing changes. This way the application remains consistent.
* **Local cache of configuration items** - so it doesn't affect your application's resiliency and availability.
* **Native creation of Java objects** - no need to manually parse Strings and convert it to objects. The library is able to natively create Lists, Sets, Maps, primitives, Strings, arrays and every class that provides a constructor that takes a String as an argument.

<a name="and-the-minimum-requirements"/>
## And the minimum requirements?

* Java Runtime Environment 6.
* An instance of ReConf Server (or an Apache Server configured to act as one).
* A few megabytes of local storage for caching purposes (the size of it is directly proportional to the size of your configurations - the bigger they are, the bigger the space it needs).

<a name="how-can-i-use-it"/>
## How can I use it?

<a name="a-few-concepts-first"/>
### A few concepts first

Two elements form the basis of ReConf: configuration items (`@ConfigurationItem`) and configuration repositories (`@ConfigurationRepository`); simply put, one or more items grouped together form a repository. Every configuration item has a **name**, a **component** and a **product**. The latter two attributes can be defined once, in `@ConfigurationRepository`. Keep with us for more details on how to integrate!

<a name="import-our-maven-dependency"/>
### Import our Maven dependency

Add these lines to the `pom.xml` file
```xml
<dependency>
    <groupId>br.com.uol.reconf</groupId>
    <artifactId>reconf-client</artifactId>
    <version>2.0.0</version>
</dependency>
```

<a name="configure-your-reconfxml-file"/>
### Configure your reconf.xml file

ReConf looks for a file named **reconf.xml** in the classpath. The bare minimum configuration must have two elements, the basic URL where the ReConf server can be found (like http://server.reconf.intranet) and a directory to store the local cache (for example /export/application/local-cache).

The file below is an example of a very simple configuration file. There is an XSD file available at https://raw.github.com/reconf/reconf-jvm/master/schema/reconf-1.0.xsd. 

```xml
<configuration xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="https://raw.github.com/reconf/reconf-jvm/master/schema/reconf-1.0.xsd">
    <local-cache>
        <location>/export/application/local-cache</location>
    </local-cache>
    <server>
        <url>http://server.reconf.intranet</url>
    </server>
</configuration>
```

The application must have the necessary permissions to read, write and delete the directory configured in local-cache/location.

<a name="create-a-configuration-repository"/>
### Create a Configuration Repository

In order to define a configuration repository, you must create a new Java interface and decorate it with the annotations provided by the reconf-client jar.

The example below provides a very simple configuration repository where each `@ConfigurationItem` will update itself every 10 seconds.

```java
import java.math.*;
import java.util.concurrent.*;
import reconf.client.annotations.*;

@ConfigurationRepository(product="my-product", component="hello-application",
    interval=10, timeUnit=TimeUnit.SECONDS)
public interface WelcomeConfiguration {

    @ConfigurationItem("welcome.text")
    String getText();

    @ConfigurationItem("promotional.price")
    BigDecimal getPrice();
}
```

<a name="using-a-configurationrepository"/>
### Using a ConfigurationRepository

Configuration repositories are easily obtained via `get` method provided by the reconf.client.proxy.ConfigurationRepositoryFactory class. Because creating a proxy is an expensive operation, this operation caches its result; meaning that calling it one or more times will return the same instance when the same arguments are provided.

```java
    public static void main(String[] args) {
        WelcomeConfiguration welcome = ConfigurationRepositoryFactory.get(WelcomeConfiguration.class);
        System.out.println(welcome.getText());
    }
```

<a name="native-types-built-automatically"/>
### Native types built automatically

The library is capable of recognizing and building automatically several types of Java objects, including the interfaces from the java.util package. You can find the details below. To take full advantage of this feature, it is necessary to use the ReConf format to declare the value of your configurations.

There are two kinds of configurations: the ones that are mapped to a single object (delimited by single quotes `' '`) and sets of single objects (delimited by square brackets `[ ]`).

#### Declaring simple objects
Simple objects can be built by the tool as long as they are either primitives (or wrappers) or provide a public constructor that takes one java.lang.String as argument. The table below shows a few examples.

| returning type | configuration value | resulting object |
|----------------|-----------------------|------------------|
| java.lang.String | 'my simple String' | "my simple String" |
| java.lang.String | '\n' | "\n" (a java.lang.String containing the new line symbol) |
| boolean | 'tRuE' | true |
| java.lang.Boolean | 'false' | false |
| char | 'a' | 'a' |
| java.lang.Character | '\t' | '\t' (a java.lang.Character containing the tab symbol) |
| java.lang.Character | 'ab' | error! |
| float | '1.2' | 1.2 |
| java.math.BigDecimal | '1' | 1 |
| int | '10' | 10 |

#### Declaring arrays of objects

An array fits the "a collection of single objects" definition and so it must be declared inside square brackets. The table below shows a few examples.

| returning type | configuration value | resulting object |
|----------------|-----------------------|------------------|
| java.lang.String[ ] | [ 'a', ' b', 'c' ] | [ "a", " b", "c" ] |
| java.lang.String[ ] | [ ] | zero-sized String array |
| java.lang.String[ ] | " " | error! (no single quotes) |
| boolean[ ] | [ 'true','false' ] | [ true, false ] |
| int[ ] | [ '1' , '2' ] | [ 1, 2 ] |
| java.math.BigDecimal[ ] | [ '1', '10' ] | [ 1, 10 ] |
| char[ ] | [ 'a', '\n' ] | [ 'a', '\n' ] |
| float[ ] | [ '-1.01' ] | [ -1.01 ] |

#### Before we dive into Collections of objects

To group objects inside a Collection, there is no need to declare the returning type as a concrete implementation (like java.util.ArrayList or java.util.HashSet). The library is shipped with pre-selected implementations according to the type and, if you don't want to use the implementation we chose, simply use the class of choice as the returning type.

| returning type | default implementation |
|----------------|------------------------|
| java.util.Collection | java.util.ArrayList |
| java.util.List | java.util.ArrayList |
| java.util.Set | java.util.HashSet |
| java.util.SortedSet | java.util.TreeSet |
| java.util.NavigableSet | java.util.TreeSet |
| java.util.Queue | java.util.LinkedList |
| java.util.concurrent.BlockingQueue | java.util.concurrent.ArrayBlockingQueue |
| java.util.concurrent.BlockingDeque | java.util.concurrent.LinkedBlockingDeque |
| java.util.Map | java.util.HashMap |
| java.util.concurrent.ConcurrentMap | java.util.concurrent.ConcurrentHashMap |
| java.util.concurrent.ConcurrentNavigableMap | java.util.concurrent.ConcurrentSkipListMap |
| java.util.NavigableMap | java.util.TreeMap |
| java.util.SortedMap | java.util.TreeMap |

#### Building Collections of objects

Collections of objects must be delimited by square brackets. The table below shows a few examples.

| returning type | configuration value | resulting collection |
|----------------|-----------------------|------------------|
| java.util.Collection<java.lang.String> | [ 'a', 'b', 'c' ] | "a", "b", "c" |
| java.util.Collection<java.lang.String> | [ ] | empty |
| java.util.Collection<java.util.Collection<java.lang.String>> | [ [ ] ] | an empty collection containing an empty collection |
| java.util.Collection<java.util.Collection<java.lang.String>> | [ [ 'a' ], [ 'b' ] ] | a collection containing two collections, one with "a" and the other with "b" |
| java.util.Collection<java.lang.String> | ' ' | error! (no square brackets) |
| java.util.Collection<java.lang.Boolean> | [ 'true', 'false' ] | true, false |
| java.util.Collection<java.math.BigDecimal> | [ '1', '10' ] | 1, 10 |
| java.util.Collection<java.lang.Character> | [ 'a', 'b' ] | 'a', 'b' |
| java.util.Collection<java.lang.Float> | [ '-1', '1.01' ] | -1, 1.01 |

#### Building Maps
A Map is different from a Collection because it contains pairs of tuples of the form Key-Value, whereas a Collection is a container of objects with no relation among themselves. For this reason, the formatting part is different, but not that much, since a Map is a complex type. Just separate a key from its value by using a colon `:` and a pair of key-values from each other using a comma `,`.

| returning type | configuration value | resulting map |
|----------------|-----------------------|------------------|
| java.util.Map<java.lang.String,java.lang.Character> | [ 'a':'b' , 'c':'d' ] | { "a" = 'b', "c" = 'd' } |
| java.util.Map<java.lang.Object,java.lang.Object> | [ ] | empty |
| java.util.Map<java.lang.String,java.util.List<java.lang.Object>> | [ 'k' : [ ] ] | { "k" = [ ] } (the key "k" maps to an empty collection) |
| java.util.Map<java.lang.String,java.util.List<java.lang.Integer>> | [ 'k' : [ '1','2' ] ] | { "k" = [ 1,2 ] } |
| java.util.Map<java.lang.Object,java.lang.Object> | ' ' | error! (no square brackets) |
| java.util.Map<java.lang.String,java.lang.String> | [ 'x' ] | error! (a key must map to a value) |
| java.util.Map<java.lang.String,java.lang.String> | [ 'x' : 1 ] | error! (no single quotes enclosing 1) |
| java.util.Map<java.lang.String,java.lang.Boolean> | [ 'true' : 'false' ] | { "true" = false } |
| java.util.Map<java.lang.Integer,java.lang.Integer> | [ '1' : '2' ] | { 1 = 2 } |

<a name="advanced-features"/>
## Advanced Features

<a name="setting-up-items-with-different-characteristics"/>
### Setting up items with different characteristics

It is possible to specialize an item, setting it up with characteristics that deviate from the ones configured in the `@ConfigurationRepository` annotation. The library operates using a simple rule: when inspecting an item, if nothing is found, the item will inherit the attributes defined in the interface. Otherwise, just the particular difference found will be applied.

#### Reading the configuration from another component and/or product

In the example below, "currency.code" belongs to another component, named "goodbye-component". The fourth item, "minimum.age" is part of the "general-configuration" component, which belongs to "all-products" product. The other two items, "promotional.price", and "welcome.text" belong to "hello-application" which is under "my-product". Finally, all items will update on a ten-second basis.

```java
import java.math.*;
import java.util.concurrent.*;
import reconf.client.annotations.*;

@ConfigurationRepository(product="my-product", component="hello-application",
    interval=10, timeUnit=TimeUnit.SECONDS)
public interface WelcomeConfiguration {

    @ConfigurationItem("welcome.text")
    String getText();

    @ConfigurationItem("promotional.price")
    BigDecimal getPrice();

    @ConfigurationItem(value="currency.code", component="goodbye-application")
    String getCurrencyCode();

    @ConfigurationItem(value="minimum.age", component="general-configuration", product="all-products")
    int getMinimumAge();
}
```

<a name="updating-a-configurationrepository-through-code"/>
### Updating a ConfigurationRepository through code

There's a way to force an update operation of every `@ConfigurationItem` of a `@ConfigurationRepository`, regardless the update frequency parameters (interval and timeUnit). To enable it, add a **void** method to the interface and annotate it with `@UpdateConfigurationRepository`. When called, the method will block until all update operations have returned. In case everything goes ok, the local cache is updated; otherwise a runtime `UpdateConfigurationRepositoryException` is thrown to notify the application that a problem has occurred.

```java
package examples;

import java.util.*;
import java.util.concurrent.*;
import reconf.client.annotations.*;

@ConfigurationRepository(product="my-product", component="hello-application",
    interval=1, timeUnit=TimeUnit.HOURS)
public interface WelcomeConfiguration {

    @ConfigurationItem("welcome.text")
    String getText();

    @UpdateConfigurationRepository
    void updateIt();
}
```

<a name="configurationrepository-reuse-through-customizations"/>
### ConfigurationRepository reuse through Customizations

Customizations are a feature that allows the developer to solve the following problem: "Can I create two instances of the same ConfigurationRepository containing different configuration values?". In order to do that, we introduce the concept of Customizations. This feature provides a way to slightly change a configuration repository by adding prefixes and/or suffixes for components and/or configuration item names.

```java
    public static void main(String[] args) {
        Customization cust = new Customization();
        cust.setComponentPrefix("cp-");
        cust.setComponentSuffix("-cs");
        cust.setComponentItemPrefix("kp-");
        cust.setComponentItemSuffix("-ks");

        WelcomeConfiguration welcome = ConfigurationRepositoryFactory.get(WelcomeConfiguration.class);

        WelcomeConfiguration customWelcome = ConfigurationRepositoryFactory.get(WelcomeConfiguration.class, cust);
        System.out.println(welcome.getText() + ", " + customWelcome.getText());
    }
```

The example above creates two repositories, both from the same interface. The "welcome" repository will behave just as expected, retrieving the configuration from the "my-product/hello-application/welcome.text" hierarchy. The second instance though, namely "customWelcome", will retrieve the configuration from the "my-product/cp-hello-application-cs/kp-welcome.text-ks" hierarchy.

<a name="organizing-log-messages"/>
### Organizing log messages

If you use slf4j (if you don't there are [a lot of reasons](http://logback.qos.ch/reasonsToSwitch.html) to do it), you can declare a new logger and append it to the appender of your preference. The example sets the level to DEBUG but I don't recommend doing it in production environment since it's very verbose. The INFO level will do just fine.

```xml
<logger name="ReConf" additivity="false" level="DEBUG">
    <appender-ref ref="A1" />
</logger>
```

<a name="localization-with-reconfxml"/>
### Localization with reconf.xml

To activate localized log messages, add the tag `locale` in the reconf.xml file. The locale must comply with the [JDK 6 and JRE 6 Supported Locales](http://www.oracle.com/technetwork/java/javase/locales-137662.html). Besides the default locale (en_US) the library also provides an additional one, Portuguese Brazil (pt_BR).

```xml
<configuration xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="https://raw.github.com/reconf/reconf-jvm/master/schema/reconf-1.0.xsd">
    <locale>pt_BR</locale>
    <local-cache>
        <location>/export/application/local-cache</location>
    </local-cache>
    <server>
        <url>http://server.reconf.intranet</url>
    </server>
</configuration>
```

<a name="overriding-the-updatefrequency-annotation-with-reconfxml"/>
### Overriding the update frequency parameters with reconf.xml
It's very common to define a reasonable update frequency for production environment and a different one during testing. Adding a `configuration-repository-update-frequency` tag in the reconf.xml file will cause it to override the update frequency parameters (interval and timeUnit) of every configuration repository.

```xml
<configuration xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="https://raw.github.com/reconf/reconf-jvm/master/schema/reconf-1.0.xsd">
    <local-cache>
        <location>/export/application/local-cache</location>
    </local-cache>
    <server>
        <url>http://server.reconf.intranet</url>
    </server>
    <configuration-repository-update-frequency>
        <interval>1</interval>
        <time-unit>SECONDS</time-unit>
    </configuration-repository-update-frequency>
</configuration>
```

<a name="reading-from-different-configuration-file"/>
### Reading from different configuration file
To read a different configuration file, start the application with `-Dreconf.client.xml.location=/path/to/a/different/file.xml`. The library will try to read from this location before looking for a reconf.xml file in the classpath.

<a name="integrating-with-spring"/>
### Integrating with Spring

The package `reconf-spring` provides a class for easy integration with Spring, including the use of @Autowired annotation. Add the following dependency to the `pom.xml` file.

```xml
<dependency>
    <groupId>br.com.uol.reconf</groupId>
    <artifactId>reconf-spring</artifactId>
    <version>2.0.0</version>
</dependency>
```

For every configuration repository, declare a bean of class "reconf.spring.RepositoryConfigurationBean" with a "configInterface" attribute configured with the interface containing the `@ConfigurationRepository` annotation.

Assuming that we are using the interface below.
```java
package example;

import java.util.concurrent.*;
import reconf.client.annotations.*;

@ConfigurationRepository(product="my-product", component="hello-application",
    interval=10, timeUnit=TimeUnit.SECONDS)
public interface WelcomeConfiguration {

    @ConfigurationItem("welcome.text")
    String getText();
}
```

The xml should look like this.

```xml
<bean class="reconf.spring.RepositoryConfigurationBean">
    <property name="configInterface" value="example.WelcomeConfiguration" />
</bean>
```

<a name="using-customizations-with-spring"/>
### Using Customizations with Spring

The xml excerpt below creates two beans, a regular "welcome" and a custom "customWelcome" detailed in [ConfigurationRepository reuse through Customizations](#configurationrepository-reuse-through-customizations).

```xml
<bean id="customWelcome" class="reconf.spring.RepositoryConfigurationBean">
    <property name="configInterface" value="example.WelcomeConfiguration"/>
    <property name="componentPrefix" value="cp-"/>
    <property name="componentSuffix" value="-cs"/>
    <property name="componentItemPrefix" value="kp-"/>
    <property name="componentItemSuffix" value="-ks"/>
</bean>

<bean id="welcome" class="reconf.spring.RepositoryConfigurationBean">
    <property name="configInterface" value="example.WelcomeConfiguration"/>
</bean>
```

<a name="using-notifications"/>
### Events and Listeners

TODO!

<a name="troubleshooting"/>
## Troubleshooting

1. Check for error and warn messages logged by the framework.
2. Check for "strange" characters in your configuration item values, such as tabs and spaces, before and after enclosing characters (`''` and `[]`).
3. Try to delete everything inside the local-cache directory defined in the reconf.xml file.
4. Enable DEBUG logging and look for strange messages.

## License

 Copyright 1996-2013 UOL Inc

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
