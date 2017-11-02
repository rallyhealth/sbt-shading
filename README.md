# shading-sbt-plugin

Enforces that a library's components are all versioned.

#### For example:
* artifact name: lib-stats-__v2__
* packages: com.rallyhealth.stats.__v2__.Stats
* directories: com/rallyhealth/stats/__v2__/Stats

Mitigates runtime dependency hell by enabling multiple incompatible major versions of a library to coexist together safely.

## Setup
Add the latest [release](https://github.com/rallyhealth/shading-sbt-plugin/releases) to your plugins.sbt:

plugins.sbt
```
addSbtPlugin("com.rallyhealth.sbt" %% "shading-sbt-plugin" % "x.y.z")
```

build.sbt
```
lazy val `lib-stats` = project
  .enablePlugins(ShadingPlugin)
```

## Semver is not enough
[SemVer](http://semver.org/) alone will not save you from [Dependency Hell](https://en.wikipedia.org/wiki/Dependency_hell).  

### Major versions bring breaking changes.
> 8. Major version X (X.y.z | X > 0) MUST be incremented if any backwards incompatible changes are introduced to the public API.
>
> -- <cite>http://semver.org/#spec-item-8</cite>

## A Song of Dependencies and Hell
### Chapter 1: Everything works

Consider a library, `lib-stats % 1.0.0`, that records metrics. It has a `com.rallyhealth.stats.Stats.inc()` method that everybody loves.

Your app uses `lib-http`, `lib-queue`, and `lib-akka` which make use of `lib-stats % 1.0.0` to send metrics, and life is good.

![Dependency graph. Transitive dependency on lib-stats v1.0.0](readme/dependency-hell-v1.png)

### Chapter 2: The breaking change
One day, a well-meaning developer decides that `Stats.inc()` should really be renamed to `Stats.increment()`.

He renames the method and releases `lib-stats 2.0.0` as a major version, signaling the breaking change. He also updates `lib-queue` to make use of it and releases `2.0.0` of that as well.

You need the latest queuey goodness for a new feature due tomorrow, so you update to the latest `lib-queue`. Your code compiles. Your unit tests pass. You deploy it to dev.

And you're shocked to discover that both HTTP and Caching are broken.

![Dependency graph. lib-stats v1.0.0 is evicted by v2.0.0. lib-http and lib-cache are broken.](readme/dependency-hell-v2.png)

You post the error to the Engineering room. They say "hurr hurr derpendencies," and tell you you'll have to clean up the mess.

You need to ship this feature tomorrow. You cry.

#### Evictions
So what happened?

Sbt evicted `lib-stats 1.0.0` in favor of the newer `lib-stats 2.0.0`. That's by design. Sbt will only keep the latest version of a library.

`lib-http` tried to call `Stats.inc()` but there was no such method defined. There was only `Stats.increment()`.

You look up who renamed the method, but immediately regret doing so as impure thoughts flood your mind.

There must be a better way.

### Chapter 3: A Solution Emerges
#### Eliminating Evictions
Can we prevent the eviction from happening? Yes!

If we give the artifacts different names, they won't get evicted. Including the major version in the name, like `lib-stats-v2`, would do the trick quite nicely.

This plugin enforces that.

#### Preventing Namespace Conflicts
So if both `lib-stats-v1` and `lib-stats-v2` are on the classpath, how do we keep them from having incompatible versions of `com.rallyhealth.stats.Stats`?

Simple. This plugin enforces that you include `v2` in the package name.

You'll end up with `com.rallyhealth.stats.v1.Stats` and `com.rallyhealth.stats.v2.Stats` living in harmony.

![Dependency graph. lib-stats v1 and v2 are both in the graph. All libraries have compatible dependencies.](readme/dependency-hell-shaded.png)

### Epilogue
Your app uses only shaded internal libraries now.  You have two binary-incompatible versions of `lib-stats-v*` in your app, but you don't care. Everything works just fine.

One day you'll update `lib-http` and `lib-cache` to use the new `lib-stats-v2`, but not today. Your friends are heading to the bar, and you're free to join then.

Your phone vibrates and you see the !love from QA and your product owner telling you how wonderful you are.

You smile as you sip your gin and tonic. Dependency hell is the last thing on your mind.

## Errors
If the artifact name, directories, or package names do not include the current major version, the [shading checks](https://github.com/rallyhealth/shading-sbt-plugin/blob/master/src/main/scala/com/rallyhealth/sbt/shading/ShadingPlugin.scala#L24-L28) will fail the build when `test` or `publish` are run.

## Caveats

### Renaming
To upgrade from `lib-stats-v1` to `lib-stats-v2`, all classes that `import com.rallyhealth.stats.v1.Stats` will have to be updated to `import com.rallyhealth.stats.v2.Stats`.

This may seem like a drawback at first, but it's actually a strength. Introducing breaking changes has become more expensive. They can often be avoided. There's incentive to do so now.

### Third party jars
This plugin cannot protect you from unshaded third party libraries. For example, netty 3 does not play nicely with netty 4.

However, third party libs move at much slower pace than intra-organizational libs. Historically, most binary incompatibility pain is self-induced via shared internal libs with lots of shared transitive dependencies.

### Resource management
Take care around using shading with global state and resource consumption (threads, external connections, etc.).

For example, if your library acts as a global database connection pool, shading it opens the possibility of having multiple instances active at once and opening extra connections.

If the consequences of duplicating global state is unacceptable, shading may not be the correct solution.

### Minor/Patch breakages
This plugin only solves for breaking changes across major versions.

[git-versioning-sbt-plugin](https://github.com/rallyhealth/git-versioning-sbt-plugin#checking-semantic-versioning) complements this plugin by providing SemVer enforcement for minor/patch releases using the Typesafe [Migration Manager](https://github.com/typesafehub/migration-manager).

## Alternatives

### OSGi
A framework for managing components on the JVM. Requires running inside a third party OSGi server.

* https://www.osgi.org/
* https://en.wikipedia.org/wiki/OSGi

### Java 9: Project Jigsaw
Use some of the new modularization features of Java 9

http://openjdk.java.net/projects/jigsaw/

### sbt-assembly shading
sbt-assembly can do auto-shading of artifacts, but gets hairy with multi-project builds and IDE support.

https://github.com/sbt/sbt-assembly#shading

## Testing
This plugin is tested by unit tests and with sbt's built-in [scripted plugin](http://eed3si9n.com/testing-sbt-plugins).
