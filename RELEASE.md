- mvn verify
- mvn versions:display-dependency-updates
- mvn versions:display-plugin-updates
- update CHANGELOG.md
  1. Change the `Unreleased` header to the release version.
  2. Add a link URL to ensure the header link works.
  3. Add a new `Unreleased` section to the top.
- change version in pom.xml
- push and wait for build
- create tag jdependency-VERSION
- push and wait for build
- login to https://oss.sonatype.org/
- staging repositories
- close
- release