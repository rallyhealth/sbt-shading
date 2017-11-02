/*
 * All of this comes from http://eed3si9n.com/testing-sbt-plugins
 */
ScriptedPlugin.scriptedSettings

/**
  * The scripted sbt projects also need to know any sbt opt overrides. For example:
  * - if the .ivy2 location is in another place
  * - if logging options should be changed
  */
lazy val defaultSbtOpts = settingKey[Option[String]]("The contents of the default_sbt_opts env var.")

defaultSbtOpts := {
  sys.env
    .collectFirst { case (key, value) if key.equalsIgnoreCase("default_sbt_opts") => value }
}

scriptedLaunchOpts := {
  scriptedLaunchOpts.value ++
    Seq("-Xmx1024M", "-Dplugin.version=" + version.value) ++
    defaultSbtOpts.value
}
