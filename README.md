# idea-plugin-strayCompletableFuture — Stray CompletableFuture Detector

An IntelliJ IDEA plugin that highlights **stray (uncollected) `CompletableFuture` calls** in Java:
an expression that produces a `java.util.concurrent.CompletableFuture` whose result is discarded as a
statement. Such futures are never awaited, chained, returned, or stored, so exceptions they complete
with are silently swallowed and the async work goes unobserved.

```java
CompletableFuture.runAsync(this::work);   // ⚠ flagged — result discarded
service.fetchAsync(id);                   // ⚠ flagged — returns CompletableFuture, discarded
future.thenApply(x -> x + 1);             // ⚠ flagged — new future discarded

CompletableFuture<Void> f = CompletableFuture.runAsync(this::work); // ok — stored
return service.fetchAsync(id);                                      // ok — returned
allFutures.add(service.fetchAsync(id));                             // ok — passed on
service.fetchAsync(id).join();                                      // ok — awaited
```

## How it works

`StrayCompletableFutureInspection` is a `LocalInspectionTool` (registered as `localInspection` in
`plugin.xml`). It visits every `PsiExpressionStatement` — the PSI node for an expression whose value is
thrown away — and reports it when the expression is a method call or constructor whose type is
`CompletableFuture` or a subtype (checked with `InheritanceUtil.isInheritor`). Any use that *collects*
the future (assignment, `return`, passing as an argument, method chaining) is a different PSI shape, so
it is not flagged.

Suppress a deliberate fire-and-forget call with `//noinspection StrayCompletableFuture` or via the
standard quick-fix menu.

## Project layout

```
build.gradle.kts                                            Gradle build (IntelliJ Platform plugin 1.17.4)
src/main/java/com/example/cfd/StrayCompletableFutureInspection.java
src/main/resources/META-INF/plugin.xml
src/main/resources/inspectionDescriptions/StrayCompletableFuture.html
samples/StrayFutureSamples.java                             Manual test cases
```

## Building

**Requirements:** JDK 17 (the 2023.2 platform is compiled for Java 17) and network access to download
the platform.

This scaffold ships without the `gradle/wrapper/gradle-wrapper.jar` binary. Create it once with a
system Gradle, then build:

```bash
gradle wrapper --gradle-version 8.7   # one-time, if you don't already have the wrapper jar
./gradlew buildPlugin                 # produces build/distributions/idea-plugin-strayCompletableFuture-0.1.0.zip
```

Alternatively, just open the folder in IntelliJ IDEA — it provisions Gradle automatically, so no
wrapper jar is needed.

## Trying it

```bash
./gradlew runIde
```

launches a sandbox IDE with the plugin installed. Open `samples/StrayFutureSamples.java` in it and
confirm the four flagged cases show warnings and the "ok" cases do not.

## Installing into your IDE

`Settings → Plugins → ⚙ → Install Plugin from Disk…` and pick the built
`build/distributions/idea-plugin-strayCompletableFuture-0.1.0.zip`.
