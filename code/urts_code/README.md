# uRTS Tool
This is the source code of uRTS tool that supports both retestall and unified regression testing selection
mentioned in the paper.

## Installation
Use maven to install the uRTS tool:
```
$ mvn clean install
```

## Setup uRTS Maven Plugin
Add uRTS plugin in the list of "<plugins>" of your project's `pom.xml`:
```
<plugin>
    <groupId>org.urts</groupId>
    <artifactId>urts-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</plugin>
```

Add dependency for configuration API instrumentation with uRTS tracker:
```
<dependency>
    <groupId>org.urts</groupId>
    <artifactId>org.urts.core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>compile</scope>
</dependency>
```

## Instrument Configuration API with uRTS tracker
uRTS provides `recordGetConfig` and `recordSetConfig` listener to track configuration change.
```Java
/**
* Software's Configuration APIs invoke this method to collect
* exercised configuration.
* @param name Configuration name that used by get/set config-API
* @param value Configuration value that used by get/set config-API
*/
public static void recordGetConfig(String name, String value);
public static void recordSetConfig(String name, String value);
```

To use uRTS, user needs to insert these two APIs into the project's configuration set/get APIs.
An example of Hadoop Common configuration API instrumentation can be found [here](https://github.com/xlab-uiuc/hadoop-unify/commit/63df28234353e3330b96adcddce1b47b54f11f95).

## Add a new getter test to get all configuration values before selection
uRTS requires user to write a new test that gets all configuration parameters' value for
value comparison purpose. This test is executed before selection happens.
An exmple for Hadoop Common can be found [here](https://github.com/xlab-uiuc/uRTS-ae/tree/main/experiment/urts/hcommon/TestGetConfigValueForConfigAware.java).

## Configuration
To configure uRTS, put `.urtsrc` under the same directory of your `pom.xml` file.
You can configure uRTS with following configuration parameters:
```
[config.file.dir.path=<P>]
[config.production.name=<N>]
[config.mapping.path=<M>]
[config.inject.path=<I>]
[config.prod.path=<C>]
```
Where:
* \<P\> is the test Generated Configuration <key, value> file path, default value is current directory path.
* \<N\> is the current production name used to differentiate with other production and default round, default value is `NonSetProductionName`
* \<M\> is the ctest mapping file path to filter untestable configuration parameter, please follow [here](https://github.com/xlab-uiuc/openctest/tree/main/core/generate_ctest) to check how to generate ctest mapping
* \<I\> is the configuration file path that ctest used to read configuration from
* \<C\> is the configuration file path that contains all changed configuration

## Run selection with uRTS
Before selection we need to perform the added test to get new configuration values, test name is configured with `-DgetterClass`
Then we use uRTS to perform test selection and execute the selected tests.
An exmple command line to perform uRTS with getter test called `TestGetConfigValueForConfigAware`:
```
$ mvn urts:urts -DgetterClass=TestGetConfigValueForConfigAware [-DfailIfNoTests=false] [-Dmaven.test.failure.ignore=true]
```



