package co.com.pragma.crediya.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

@Component
public class ReactorMdcHook {

    private static final String HOOK_KEY = "mdc-correlation";
    private static final String CID_KEY  = LoggingConfig.CORRELATION_ID_MDC_KEY;

    @PostConstruct
    public void setup() {
        // Registra un "middleware" que se aplica a cada operador del flujo
        Hooks.onEachOperator(HOOK_KEY,
                Operators.lift((scannable, actualSubscriber) ->
                        new CoreSubscriber<Object>() {

                            @Override
                            public Context currentContext() {
                                // Contexto reactivo actual (aquí viene el correlationId)
                                return actualSubscriber.currentContext();
                            }



                            private void copyContextToMdc() {
                                ContextView ctx = currentContext();
                                if (ctx != null && ctx.hasKey(CID_KEY)) {
                                    MDC.put(CID_KEY, ctx.get(CID_KEY));
                                }
                            }

                            private void clearMdc() {
                                MDC.remove(CID_KEY);
                            }

                            @Override
                            public void onSubscribe(Subscription s) {
                                copyContextToMdc();
                                try { actualSubscriber.onSubscribe(s); }
                                finally { clearMdc(); }
                            }

                            @Override
                            public void onNext(Object t) {
                                copyContextToMdc();
                                try { actualSubscriber.onNext(t); }
                                finally { clearMdc(); }
                            }

                            @Override
                            public void onError(Throwable t) {
                                copyContextToMdc();
                                try { actualSubscriber.onError(t); }
                                finally { clearMdc(); }
                            }

                            @Override
                            public void onComplete() {
                                copyContextToMdc();
                                try { actualSubscriber.onComplete(); }
                                finally { clearMdc(); }
                            }
                        }
                )
        );
    }

    @PreDestroy
    public void cleanup() {
        Hooks.resetOnEachOperator(HOOK_KEY);
    }
}