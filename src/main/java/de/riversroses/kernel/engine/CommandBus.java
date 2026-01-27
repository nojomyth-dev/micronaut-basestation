package de.riversroses.kernel.engine;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

import de.riversroses.infra.error.DomainException;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class CommandBus {

  private final Queue<Command<?>> queue = new ConcurrentLinkedQueue<>();

  /**
   * Fire-and-forget command executed on the tick thread.
   */
  public void queue(String name, Consumer<CommandContext> action) {
    queue.offer(new Command<>(name, ctx -> {
      action.accept(ctx);
      return null;
    }, null));
  }

  /**
   * Fire-and-forget command executed on the tick thread.
   */
  public void submitVoid(String name, Function<CommandContext, ?> action) {
    queue.offer(new Command<>(name, ctx -> {
      action.apply(ctx);
      return null;
    }, null));
  }

  /**
   * Request/response command executed on the tick thread.
   * The caller blocks until tick processes it (or timeout).
   */
  public <T> T submitAndWait(String name, Function<CommandContext, T> action, long timeoutMs) {
    CompletableFuture<T> future = new CompletableFuture<>();
    queue.offer(new Command<>(name, action, future));

    try {
      return future.get(timeoutMs, TimeUnit.MILLISECONDS);
    } catch (TimeoutException te) {
      future.cancel(true);
      throw new IllegalStateException("Command execution timed out: " + name, te);
    } catch (ExecutionException ee) {
      Throwable cause = ee.getCause();

      if (cause instanceof RuntimeException re)
        throw re;
      if (cause instanceof Error err)
        throw err;

      throw new IllegalStateException("Command execution failed: " + name, cause);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Command execution interrupted: " + name, ie);
    }
  }

  /**
   * Called by the tick thread. Executes queued commands and completes futures.
   */
  public void processAll(CommandContext ctx) {
    int limit = 5000;
    Command<?> cmd;

    while (limit-- > 0 && (cmd = queue.poll()) != null) {
      try {
        Object result = cmd.action.apply(ctx);
        if (cmd.future != null) {
          completeOk(cmd.future, result);
        }
      } catch (Throwable t) {
        if (isExpected(t)) {
          log.warn("Command rejected: {} ({})", cmd.name, rootCause(t).toString());
        } else {
          log.warn("Command failed: {}", cmd.name, t);
        }

        if (cmd.future != null) {
          cmd.future.completeExceptionally(t);
        }
      }
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static void completeOk(CompletableFuture future, Object result) {
    future.complete(result);
  }

  private static Throwable rootCause(Throwable t) {
    Throwable cur = t;
    while (cur.getCause() != null &&
        (cur instanceof CompletionException
            || cur instanceof ExecutionException
            || cur instanceof IllegalStateException)) {
      cur = cur.getCause();
    }
    return cur;
  }

  private static boolean isExpected(Throwable t) {
    Throwable root = rootCause(t);
    return root instanceof DomainException
        || root instanceof ConstraintViolationException
        || root instanceof IllegalArgumentException;
  }

  private record Command<T>(
      String name,
      Function<CommandContext, T> action,
      CompletableFuture<T> future) {
  }
}
