// Scratch file for manually verifying the inspection. Not part of the plugin build.
// Open this in the sandbox IDE launched by `./gradlew runIde` and look for warnings.

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class StrayFutureSamples {

    CompletableFuture<String> fetchAsync(int id) {
        return CompletableFuture.supplyAsync(() -> "value-" + id);
    }

    void work() {
    }

    void flaggedCases() {
        // --- These SHOULD be highlighted (stray / uncollected) ---
        CompletableFuture.runAsync(this::work);   // result discarded
        fetchAsync(1);                            // returns CompletableFuture, discarded
        fetchAsync(2).thenApply(s -> s + "!");    // new future discarded
        CompletableFuture.completedFuture("x");   // discarded
    }

    List<CompletableFuture<String>> okCases() {
        // --- These should NOT be highlighted (future is collected) ---
        CompletableFuture<Void> f = CompletableFuture.runAsync(this::work); // stored
        f.join();

        List<CompletableFuture<String>> all = new ArrayList<>();
        all.add(fetchAsync(3));                   // passed as argument

        fetchAsync(4).join();                     // awaited (join returns String, discarded String is fine)

        String s = fetchAsync(5).join();          // used
        System.out.println(s);

        return all;                               // returned
    }

    CompletableFuture<String> returnedDirectly() {
        return fetchAsync(6);                     // returned, not stray
    }
}
