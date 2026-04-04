package com.mylibrary.retry;

import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Retryable(
        retryFor = TransientDataAccessException.class,
        maxAttemptsExpression = "${spring.retry.max-attempts}",
        backoff = @Backoff(
                delayExpression = "${spring.retry.backoff.delay}",
                multiplierExpression = "${spring.retry.backoff.multiplier}",
                maxDelayExpression = "${spring.retry.backoff.max-delay}"
        )
)
public @interface RetryOnDatabaseError {
}