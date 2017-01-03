// addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.5")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.8.0")
addSbtPlugin("org.xerial.sbt" % "sbt-pack" % "0.7.9" exclude("org.slf4j", "slf4j-simple"))

resolvers += Resolver.url(
  "bintray-eltimn-sbt-plugins",
  url("http://dl.bintray.com/eltimn/sbt-plugins"))(
  Resolver.ivyStylePatterns)

addSbtPlugin("com.eltimn" % "sbt-frontend" % "0.1.2-9a0178f587142b9620b39d6a55cc2f5b65017be6")
