## Version 2.18, dev

## Version 2.17, released 2026-10-18

- [FIX] Fixed versioned `module-info.class` in multi-released jars being treated as a class
- [CHG] Upgraded to ASM 9.11
- [ADD] Added `DeclaredDependencies` to include class references from `META-INF/services`,
  `module-info.class`, Spring and Sisu metadata (opt-in) #385

## Version 2.16.0, released 2026-05-24

- [CHG] Upgraded to ASM 9.10

## Version 2.15.0, released 2026-02-25

- [FIX] Fixed a dangling InputStream
- [CHG] Changed minimum Java back to 8

## Version 2.14.0, released 2025-10-09

- [CHG] Upgraded to ASM 9.9
- [CHG] New released process

## Version 2.13.0, released 2025-06-14

- [CHG] Upgraded to ASM 9.8

## Version 2.12.0, released 2025-02-02

- [CHG] Upgraded to ASM 9.7.1 (JDK 24 support)

## Version 2.11.0, released 2024-11-18

- [CHG] Upgraded to ASM 9.7 (JDK 23 support)
- [CHG] Changed minimum Java to 11

## Version 2.10.0, released 2024-01-25

- [CHG] Upgraded to ASM 9.6
- [CHG] Upgraded other internal dependencies
- [CHG] Changed minimum Maven version to 3.6.3
- [ADD] Added support for multi released jars (Thanks to Niels Basjes)

## Version 2.9.0, released 2023-08-11

- [CHG] Upgraded to ASM 9.4 (JDK 20 support)

## Version 2.8.0, released 2022-08-04

- [CHG] Upgraded to ASM 9.3

## Version 2.7.0, released 2021-06-02

- [CHG] Changed test with java 16
- [CHG] Changed requires maven 3.6.0
- [ADD] Added ASM9 Opcodes

## Version 2.6.0, released 2021-02-10

- [FIX] Fixed issue #47, ClazzpathUnits should not be assigned for dependencies
- [CHG] Upgraded to ASM 9.1

## Version 2.5.0, released 2020-10-16

- [CHG] Upgraded to ASM 9.0 (JDK 16 support)

## Version 2.4.0, released 2020-03-30

- [CHG] Upgraded to ASM 8.0

## Version 2.3.0, released 2019-11-11

- [FIX] Fixed the shading of the dependencies
- [CHG] Upgraded to ASM 7.2-beta

## Version 2.2.0, released 2019-05-23

- [CHG] Upgraded to ASM 7.1

## Version 2.1.1, released 2018-10-22

- [FIX] Fixed JDK11 compatibility

## Version 2.1.0, released 2018-10-16

- [FIX] Fixed Windows file separator issues
- [CHG] Upgraded to ASM 7-beta (JDK11)
- [ADD] Added CI build for Windows

## Version 2.0.0, released 2018-07-27

- [FIX] Fixed tests
- [CHG] Upgraded ASM to support java 10
- [CHG] Raise minimum java version to 8
- [CHG] Internal use of nio
- [ADD] API not supports File and Path
- [ADD] Added travis

## Version 1.4.0, released 2018-05-18

- [CHG] Upgraded dependencies

## Version 1.3.0, released 2018-01-02

- [CHG] Upgraded dependencies
- [CHG] Inlined the dependencies
- [CHG] Removed prerequisites from POM

## Version 1.2.0, released 2017-08-18

- [CHG] Exclude package-info and module-info
- [CHG] Upgrade to ASM 6-beta

## Version 1.1.0, released 2015-11-15

- [FIX] Fixed file handle leak

## Version 1.0.0, released 2015-03-08

- [ADD] Support for static methods on interfaces

## Version 0.9.0, released 2014-04-24

- [CHG] Upgrade to asm5 for java8 support

## Version 0.8.0, released Skipped

- [CHG] Skipped for easier merging of forks

## Version 0.7.0, released 2011-02-11

- [FIX] Fixed the commons-io groupId

## Version 0.6.0, released 2010-06-27

- [CHG] Lot of refactoring and rename to jdependency

## Version 0.5.0, released Skipped

## Version 0.4.0, released 2006-03-24

## Version 0.3.0, released 2006-03-22

## Version 0.2.0, released 2006-01-12

## Version 0.1.0, released 2006-05-28
