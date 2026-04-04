/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package module.core.coreInterface;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author An
 */
public class CoreInterface {
    public static <T> T retryInterface(Callable<T> task, RetryDto dto) throws Exception {
        Exception lastException = null;

        for (int attempt = 1; attempt <= dto.maxRetry; attempt++) {
            try {
                return task.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt == dto.maxRetry) {
                    break;
                }

                int jitter = dto.randomState > 0
                        ? ThreadLocalRandom.current().nextInt(dto.randomState + 1)
                        : 0;
                int delay = Math.min(dto.retryTime * (attempt + jitter), dto.maxTimeRetry);

                try {
                    Thread.sleep(Math.max(delay, 0));
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw interruptedException;
                }
            }
        }

        throw lastException;
    }
}
