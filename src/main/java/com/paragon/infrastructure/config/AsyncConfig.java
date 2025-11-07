package com.paragon.infrastructure.config;

import com.paragon.infrastructure.persistence.jdbc.transaction.UnitOfWorkAwareDataSource;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.sql.SQLException;
import java.util.Map;

@Configuration
@EnableAsync
public class AsyncConfig {
    private final UnitOfWorkAwareDataSource dataSource;

    @Autowired
    public AsyncConfig(UnitOfWorkAwareDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean(name = "taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(200);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Async-");
        executor.setTaskDecorator(new MdcAndSecurityContextTaskDecorator(dataSource));
        executor.initialize();
        return executor;
    }

    static class MdcAndSecurityContextTaskDecorator implements TaskDecorator {
        private final UnitOfWorkAwareDataSource dataSource;

        public MdcAndSecurityContextTaskDecorator(UnitOfWorkAwareDataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Override
        public Runnable decorate(Runnable runnable) {
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            SecurityContext context = SecurityContextHolder.getContext();
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

            return () -> {
                try {
                    if (contextMap != null) MDC.setContextMap(contextMap);
                    SecurityContextHolder.setContext(context);
                    if (requestAttributes != null) {
                        RequestContextHolder.setRequestAttributes(requestAttributes);
                    }

                    runnable.run();
                } finally {
                    // clean up any lingering transactions on this worker thread
                    if (dataSource.isTransactionActive()) {
                        try {
                            dataSource.rollbackTransaction();
                        } catch (SQLException e) {
                            // ignore
                        }
                    }

                    MDC.clear();
                    SecurityContextHolder.clearContext();
                    RequestContextHolder.resetRequestAttributes();
                }
            };
        }
    }
}
