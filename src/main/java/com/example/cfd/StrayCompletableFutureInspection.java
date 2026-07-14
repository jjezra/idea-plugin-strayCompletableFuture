package com.example.cfd;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiExpressionStatement;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiNewExpression;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.InheritanceUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Flags a "stray" (uncollected) {@link java.util.concurrent.CompletableFuture}: a call that
 * produces a {@code CompletableFuture} whose result is thrown away as an expression statement.
 *
 * <p>Because the returned future is neither awaited, chained, returned, nor stored, any exception
 * it completes with is silently swallowed and the caller never learns whether the async work
 * finished. This is a common source of lost errors and races.
 *
 * <p>Examples flagged:
 * <pre>{@code
 *   CompletableFuture.runAsync(this::work);   // result discarded
 *   service.fetchAsync(id);                   // returns CompletableFuture<?>, discarded
 *   future.thenApply(x -> x + 1);             // new future discarded
 * }</pre>
 *
 * <p>Not flagged (the future is "collected"):
 * <pre>{@code
 *   CompletableFuture<Void> f = CompletableFuture.runAsync(this::work);
 *   return service.fetchAsync(id);
 *   allFutures.add(service.fetchAsync(id));
 *   service.fetchAsync(id).join();
 * }</pre>
 */
public class StrayCompletableFutureInspection extends AbstractBaseJavaLocalInspectionTool {

    private static final String COMPLETABLE_FUTURE = "java.util.concurrent.CompletableFuture";

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitExpressionStatement(@NotNull PsiExpressionStatement statement) {
                super.visitExpressionStatement(statement);

                // An expression used purely as a statement has its value discarded. If that value
                // is a CompletableFuture, nobody is collecting it.
                PsiExpression expression = statement.getExpression();

                // Only care about calls / constructions that yield a value. A bare assignment,
                // increment, etc. is not our concern.
                if (!(expression instanceof PsiMethodCallExpression)
                        && !(expression instanceof PsiNewExpression)) {
                    return;
                }

                if (!isCompletableFuture(expression.getType())) {
                    return;
                }

                // Inside a lambda body written in expression form (x -> future), the value is the
                // lambda's return value, so it is being collected by the surrounding call. But an
                // expression *statement* inside a block-bodied lambda ({ future; }) is still stray,
                // which the PSI structure already distinguishes for us: block bodies produce
                // PsiExpressionStatement, expression bodies do not. Nothing extra to do here.

                holder.registerProblem(
                        expression,
                        "Stray CompletableFuture: the returned future is never collected "
                                + "(awaited, chained, returned, or stored), so its result and any "
                                + "exception are silently discarded");
            }
        };
    }

    private static boolean isCompletableFuture(PsiType type) {
        if (type == null) {
            return false;
        }
        // Matches CompletableFuture and any subclass (e.g. custom subtypes), regardless of the
        // generic argument.
        return InheritanceUtil.isInheritor(type, COMPLETABLE_FUTURE);
    }
}
