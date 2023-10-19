Development
===========

Known Issues
------------

A warning is logged because `easy-mirror-deposit` runs in Java 8 (an EASY constraint) and thereofore cannot use
certain Java 9+ methods. However, this is a warning only, and does not preclude the service from running correctly.
It can therefore be safely ignored.

From `journalctl -u easy-mirror-deposit`:

```text
 WARN  [2022-10-05 10:56:15,808] com.fasterxml.jackson.module.blackbird.BlackbirdModule: Unable to find Java 9+ MethodHandles.privateLookupIn.  Blackbird is not performing optimally
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! java.lang.NoSuchMethodError: java.lang.invoke.MethodHandles.privateLookupIn(Ljava/lang/Class;Ljava/lang/invoke/MethodHandles$Lookup;)Ljava/lang/invoke/MethodHandles$Lookup;
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at java.lang.invoke.MethodHandleNatives.resolve(Native Method)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at java.lang.invoke.MemberName$Factory.resolve(MemberName.java:975)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at java.lang.invoke.MemberName$Factory.resolveOrFail(MemberName.java:1000)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! ... 28 common frames omitted
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! Causing: java.lang.NoSuchMethodException: no such method: java.lang.invoke.MethodHandles.privateLookupIn(Class,Lookup)Lookup/invokeStatic
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at java.lang.invoke.MemberName.makeAccessException(MemberName.java:871)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at java.lang.invoke.MemberName$Factory.resolveOrFail(MemberName.java:1003)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at java.lang.invoke.MethodHandles$Lookup.resolveOrFail(MethodHandles.java:1386)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at java.lang.invoke.MethodHandles$Lookup.findStatic(MethodHandles.java:780)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at com.fasterxml.jackson.module.blackbird.util.ReflectionHack$Java9Up.init(ReflectionHack.java:39)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at com.fasterxml.jackson.module.blackbird.util.ReflectionHack$Java9Up.<clinit>(ReflectionHack.java:34)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at com.fasterxml.jackson.module.blackbird.util.ReflectionHack.privateLookupIn(ReflectionHack.java:24)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at com.fasterxml.jackson.module.blackbird.ser.BBSerializerModifier.lambda$findProperties$0(BBSerializerModifier.java:67)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at com.fasterxml.jackson.module.blackbird.util.Unchecked.lambda$runnable$0(Unchecked.java:31)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at com.fasterxml.jackson.module.blackbird.ser.BBSerializerModifier.findProperties(BBSerializerModifier.java:68)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at com.fasterxml.jackson.module.blackbird.ser.BBSerializerModifier.changeProperties(BBSerializerModifier.java:52)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at com.fasterxml.jackson.databind.ser.BeanSerializerFactory.constructBeanOrAddOnSerializer(BeanSerializerFactory.java:414)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at com.fasterxml.jackson.databind.ser.BeanSerializerFactory.findBeanOrAddOnSerializer(BeanSerializerFactory.java:294)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at com.fasterxml.jackson.databind.ser.BeanSerializerFactory._createSerializer2(BeanSerializerFactory.java:239)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at com.fasterxml.jackson.databind.ser.BeanSerializerFactory.createSerializer(BeanSerializerFactory.java:173)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at com.fasterxml.jackson.databind.SerializerProvider._createUntypedSerializer(SerializerProvider.java:1495)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at com.fasterxml.jackson.databind.SerializerProvider._createAndCacheUntypedSerializer(SerializerProvider.java:1463)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at com.fasterxml.jackson.databind.SerializerProvider.findValueSerializer(SerializerProvider.java:585)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at com.fasterxml.jackson.databind.ser.DefaultSerializerProvider.acceptJsonFormatVisitor(DefaultSerializerProvider.java:566)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at com.fasterxml.jackson.databind.ObjectMapper.acceptJsonFormatVisitor(ObjectMapper.java:4519)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at com.fasterxml.jackson.databind.ObjectMapper.acceptJsonFormatVisitor(ObjectMapper.java:4498)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at io.dropwizard.configuration.ConfigurationMetadata.<init>(ConfigurationMetadata.java:76)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at io.dropwizard.configuration.BaseConfigurationFactory.<init>(BaseConfigurationFactory.java:76)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at io.dropwizard.configuration.YamlConfigurationFactory.<init>(YamlConfigurationFactory.java:29)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at io.dropwizard.configuration.DefaultConfigurationFactoryFactory.create(DefaultConfigurationFactoryFactory.java:18)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at io.dropwizard.cli.ConfiguredCommand.parseConfiguration(ConfiguredCommand.java:137)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at io.dropwizard.cli.ConfiguredCommand.run(ConfiguredCommand.java:85)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at io.dropwizard.cli.Cli.run(Cli.java:78)
Oct 05 10:56:15 deasy easy-mirror-deposit[11596]: ! at io.dropwizard.core.Application.run(Application.java:94)

```