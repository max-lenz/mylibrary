package com.mylibrary.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class LibraryMetrics {

    private final MeterRegistry meterRegistry;

    public LibraryMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordBookCreated() {
        Counter.builder("library.books.created")
                .description("Total books created")
                .register(meterRegistry)
                .increment();
    }

    public void recordBookDeleted() {
        Counter.builder("library.books.deleted")
                .description("Total books deleted")
                .register(meterRegistry)
                .increment();
    }

    public void recordBookLoaned() {
        Counter.builder("library.loans.issued")
                .description("Total book loans issued")
                .register(meterRegistry)
                .increment();
    }

    public void recordBookReturned() {
        Counter.builder("library.loans.returned")
                .description("Total book loans returned")
                .register(meterRegistry)
                .increment();
    }

    public void recordOverdueLoan() {
        Counter.builder("library.loans.overdue")
                .description("Total overdue loans")
                .register(meterRegistry)
                .increment();
    }

    public void recordReaderRegistered() {
        Counter.builder("library.readers.registered")
                .description("Total readers registered")
                .register(meterRegistry)
                .increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }
}
