package com.paragon.infrastructure.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(200);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Async-");
        executor.setTaskDecorator(new MdcAndSecurityContextTaskDecorator());
        executor.initialize();
        return executor;
    }

    static class MdcAndSecurityContextTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            // Capture MDC and SecurityContext from the submitting thread
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            SecurityContext context = SecurityContextHolder.getContext();

            return () -> {
                try {
                    // Restore both contexts inside async thread
                    if (contextMap != null) MDC.setContextMap(contextMap);
                    SecurityContextHolder.setContext(context);

                    runnable.run();
                } finally {
                    // Clean up to prevent context leakage
                    MDC.clear();
                    SecurityContextHolder.clearContext();
                }
            };
        }
    }
}
