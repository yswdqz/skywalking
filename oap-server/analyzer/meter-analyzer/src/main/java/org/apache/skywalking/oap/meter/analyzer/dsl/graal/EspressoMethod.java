package org.apache.skywalking.oap.meter.analyzer.dsl.graal;

import org.apache.skywalking.oap.meter.analyzer.dsl.Expression;
import org.apache.skywalking.oap.meter.analyzer.dsl.Result;
import org.apache.skywalking.oap.meter.analyzer.dsl.SampleFamily;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EspressoMethod {
    private static Map<String, String> contextOptions;

    private static boolean initialized = false;

    protected static final Lazy<Context> context = Lazy.of(() -> {
        return Context.newBuilder("java")
                .options(contextOptions)
                .allowAllAccess(true)
                .build();
    });
    protected static final Lazy<Value> EXPRESSION = Lazy.of(() -> loadClass(context.get(), "org.apache.skywalking.oap.meter.analyzer.dsl.Expression"));
    protected static final Lazy<Value> DSL = Lazy.of(() -> loadClass(context.get(), "org.apache.skywalking.oap.meter.analyzer.dsl.DSL"));
    private static Value loadClass(Context context, String className) {
        return context.getBindings("java").getMember(className);
    }

    public Value loadClass(String className) {
        return loadClass(context.get(), className);
    }

    public static void initialize(Map<String, String> options) {
        if (!initialized) {
            contextOptions = (options != null) ? options : Collections.emptyMap();
            EXPRESSION.get();
            DSL.get();
            initialized = true;
        }
    }

    public static void initialize() {
        HashMap<String, String> options = new HashMap<>();
        options.put("java.Properties.org.graalvm.home", System.getenv("JAVA_HOME"));
        System.out.println(System.getenv("JAVA_HOME"));
        System.out.println(System.getenv("JAVA_CLASSPATH"));

        options.put("java.Classpath", System.getenv("JAVA_CLASSPATH"));
        initialize(options);
    }


    public static Result expressionExecute(Map<String, SampleFamily> expression) {
        Result run = EXPRESSION.get().getMember("run").execute(expression).as(Result.class);
        System.out.println(run);
        return run;
    }

    public static Expression dslParse(String expression) {
        Expression parse = DSL.get().getMember("parse").execute(expression).as(Expression.class);
        System.out.println(parse);
        return parse;
    }
}
