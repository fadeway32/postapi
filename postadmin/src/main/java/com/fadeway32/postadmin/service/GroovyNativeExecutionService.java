package com.fadeway32.postadmin.service;

import com.fadeway32.postadmin.dto.GroovyExecutionResult;
import com.fadeway32.postadmin.dto.GroovySimulateRequest;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import groovy.transform.ThreadInterrupt;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.control.customizers.SecureASTCustomizer;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.security.Permission;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

@Service
public class GroovyNativeExecutionService {

    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*(\\.[A-Za-z_$][A-Za-z0-9_$]*)*(\\.\\*)?");
    private static final List<String> DEFAULT_ALLOWED_IMPORTS = Arrays.asList(
            "java.math.BigDecimal",
            "java.math.BigInteger",
            "java.time.Duration",
            "java.time.Instant",
            "java.time.LocalDate",
            "java.time.LocalDateTime",
            "java.time.Period",
            "java.time.ZoneId",
            "java.time.ZonedDateTime",
            "java.util.ArrayList",
            "java.util.Arrays",
            "java.util.Collections",
            "java.util.HashMap",
            "java.util.HashSet",
            "java.util.LinkedHashMap",
            "java.util.LinkedHashSet",
            "java.util.UUID",
            "com.fadeway32.crypto.util.AsymmetricCryptoUtils",
            "com.fadeway32.crypto.util.CryptoCodecUtils",
            "com.fadeway32.crypto.util.CryptoKeyPair",
            "com.fadeway32.crypto.util.*",
            "com.fadeway32.crypto.util.CryptoUtils",
            "com.fadeway32.crypto.util.DigestUtils",
            "com.fadeway32.crypto.util.PostcryptionUtils",
            "com.fadeway32.crypto.util.PostcryptionUtils",
            "com.fadeway32.crypto.util.SymmetricCryptoUtils"
    );
    private static final List<String> DEFAULT_BLOCKED_IMPORTS = Arrays.asList(
            "groovy.lang.Eval",
            "groovy.lang.GroovyClassLoader",
            "groovy.lang.GroovyShell",
            "java.io.*",
            "java.lang.Class",
            "java.lang.ClassLoader",
            "java.lang.ProcessBuilder",
            "java.lang.Runtime",
            "java.lang.SecurityManager",
            "java.lang.System",
            "java.lang.reflect.*",
            "java.net.*",
            "java.nio.file.*"
    );
    private static final List<String> DEFAULT_BLOCKED_RECEIVERS = Arrays.asList(
            "groovy.lang.Eval",
            "groovy.lang.GroovyClassLoader",
            "groovy.lang.GroovyShell",
            "java.io.File",
            "java.lang.Class",
            "java.lang.ClassLoader",
            "java.lang.ProcessBuilder",
            "java.lang.Runtime",
            "java.lang.SecurityManager",
            "java.lang.System",
            "java.lang.reflect.Method",
            "java.net.URL",
            "java.nio.file.Files",
            "java.nio.file.Path",
            "java.nio.file.Paths"
    );

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "postadmin-groovy-simulator");
            thread.setDaemon(true);
            return thread;
        }
    });

    public GroovyExecutionResult simulate(GroovySimulateRequest request) {
        SandboxPolicy policy = buildPolicy(request);
        long start = System.nanoTime();
        Future<GroovyExecutionResult> future = executorService.submit(new Callable<GroovyExecutionResult>() {
            @Override
            public GroovyExecutionResult call() {
                return execute(request, policy);
            }
        });

        try {
            GroovyExecutionResult result = future.get(policy.timeoutMillis, TimeUnit.MILLISECONDS);
            result.setElapsedMillis(elapsedMillis(start));
            return result;
        } catch (TimeoutException ex) {
            future.cancel(true);
            GroovyExecutionResult result = baseResult(policy);
            result.setSuccess(false);
            result.setTimeout(true);
            result.setErrorType("TIMEOUT");
            result.setErrorMessage("groovy script timed out after " + policy.timeoutMillis + " ms");
            result.setElapsedMillis(elapsedMillis(start));
            return result;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            GroovyExecutionResult result = baseResult(policy);
            result.setSuccess(false);
            result.setErrorType("INTERRUPTED");
            result.setErrorMessage("groovy script execution interrupted");
            result.setElapsedMillis(elapsedMillis(start));
            return result;
        } catch (ExecutionException ex) {
            GroovyExecutionResult result = baseResult(policy);
            result.setSuccess(false);
            result.setErrorType(errorType(ex.getCause()));
            result.setErrorMessage(errorMessage(ex.getCause()));
            result.setElapsedMillis(elapsedMillis(start));
            return result;
        }
    }

    private GroovyExecutionResult execute(GroovySimulateRequest request, SandboxPolicy policy) {
        GroovyExecutionResult result = baseResult(policy);
        Map<String, Object> variables = new LinkedHashMap<String, Object>(request.getBindings());
        variables.put("bindings", new LinkedHashMap<String, Object>(request.getBindings()));
        variables.put("now", LocalDateTime.now());
        Binding binding = new Binding(variables);
        RestrictedGroovyClassLoader classLoader = null;
        try {
            CompilerConfiguration compilerConfiguration = compilerConfiguration(policy);
            classLoader = new RestrictedGroovyClassLoader(getClass().getClassLoader(), compilerConfiguration, policy);
            Class<?> scriptClass = classLoader.parseClass(request.getScript(), "PostAdminGroovySimulation.groovy");
            Script script = InvokerHelper.createScript(scriptClass, binding);
            Object returnValue;
            if (policy.installSecurityManager) {
                SecurityManagerRegistration securityManagerRegistration = SandboxSecurityManager.ensureInstalled();
                result.setSecurityManagerActive(securityManagerRegistration.installed);
                result.setSecurityManagerMessage(securityManagerRegistration.message);
            } else {
                result.setSecurityManagerActive(false);
                result.setSecurityManagerMessage("security manager disabled for this run");
            }
            if (result.isSecurityManagerActive()) {
                SandboxSecurityManager.activate();
                try {
                    returnValue = script.run();
                } finally {
                    SandboxSecurityManager.deactivate();
                }
            } else {
                returnValue = script.run();
            }
            result.setSuccess(true);
            result.setReturnValue(returnValue);
            result.setBindings(new LinkedHashMap<String, Object>(binding.getVariables()));
            return result;
        } catch (Throwable ex) {
            result.setSuccess(false);
            result.setErrorType(errorType(ex));
            result.setErrorMessage(errorMessage(ex));
            result.setBindings(new LinkedHashMap<String, Object>(binding.getVariables()));
            return result;
        } finally {
            if (classLoader != null) {
                try {
                    classLoader.close();
                } catch (IOException ignored) {
                    // GroovyClassLoader close failures do not change execution outcome.
                }
            }
        }
    }

    private CompilerConfiguration compilerConfiguration(SandboxPolicy policy) {
        CompilerConfiguration configuration = new CompilerConfiguration();
        SecureASTCustomizer secure = new SecureASTCustomizer();
        secure.setPackageAllowed(false);
        secure.setMethodDefinitionAllowed(false);
        secure.setClosuresAllowed(true);
        secure.setIndirectImportCheckEnabled(true);
        secure.setImportsBlacklist(nonStar(policy.blockedImports));
        secure.setStarImportsBlacklist(star(policy.blockedImports));
        secure.setStaticImportsBlacklist(nonStar(policy.blockedImports));
        secure.setStaticStarImportsBlacklist(star(policy.blockedImports));
        secure.setReceiversBlackList(policy.blockedReceivers);
        secure.addExpressionCheckers(new SandboxExpressionChecker(policy));
        configuration.addCompilationCustomizers(
                new ASTTransformationCustomizer(ThreadInterrupt.class),
                new ImportPolicyCustomizer(policy),
                secure
        );
        return configuration;
    }

    private SandboxPolicy buildPolicy(GroovySimulateRequest request) {
        SandboxPolicy policy = new SandboxPolicy();
        policy.timeoutMillis = request.getTimeoutMillis() == null ? 2000 : request.getTimeoutMillis();
        policy.installSecurityManager = Boolean.TRUE.equals(request.getInstallSecurityManager());
        policy.allowedImports = mergeAndValidate(DEFAULT_ALLOWED_IMPORTS, request.getAllowedImports(), "allowedImports");
        policy.blockedImports = mergeAndValidate(DEFAULT_BLOCKED_IMPORTS, request.getBlockedImports(), "blockedImports");
        policy.blockedReceivers = mergeAndValidate(DEFAULT_BLOCKED_RECEIVERS, request.getBlockedReceivers(), "blockedReceivers");
        assertNoImportConflict(policy.allowedImports, policy.blockedImports);
        return policy;
    }

    private List<String> mergeAndValidate(List<String> defaults, List<String> additions, String field) {
        Set<String> values = new LinkedHashSet<String>();
        values.addAll(defaults);
        if (additions != null) {
            for (String addition : additions) {
                String value = normalizeClassName(addition, field);
                values.add(value);
            }
        }
        return new ArrayList<String>(values);
    }

    private String normalizeClassName(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " contains blank class name");
        }
        String normalized = value.trim();
        if (!CLASS_NAME_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(field + " contains invalid class name: " + value);
        }
        return normalized;
    }

    private void assertNoImportConflict(List<String> allowedImports, List<String> blockedImports) {
        for (String allowedImport : allowedImports) {
            if (isBlocked(allowedImport, blockedImports)) {
                throw new IllegalArgumentException("allowedImports conflicts with blockedImports: " + allowedImport);
            }
        }
    }

    private List<String> nonStar(List<String> values) {
        List<String> result = new ArrayList<String>();
        for (String value : values) {
            if (!value.endsWith(".*")) {
                result.add(value);
            }
        }
        return result;
    }

    private List<String> star(List<String> values) {
        List<String> result = new ArrayList<String>();
        for (String value : values) {
            if (value.endsWith(".*")) {
                result.add(value.substring(0, value.length() - 1));
            }
        }
        return result;
    }

    private GroovyExecutionResult baseResult(SandboxPolicy policy) {
        GroovyExecutionResult result = new GroovyExecutionResult();
        result.setSecurityManagerRequested(policy.installSecurityManager);
        result.setAllowedImports(policy.allowedImports);
        result.setBlockedImports(policy.blockedImports);
        result.setBlockedReceivers(policy.blockedReceivers);
        return result;
    }

    private long elapsedMillis(long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }

    private String errorType(Throwable throwable) {
        Throwable target = rootCause(throwable);
        if (target instanceof CompilationFailedException) {
            return "COMPILE_ERROR";
        }
        if (target instanceof SecurityException) {
            return "SECURITY_ERROR";
        }
        if (target instanceof InterruptedException) {
            return "INTERRUPTED";
        }
        return target == null ? "ERROR" : target.getClass().getSimpleName();
    }

    private String errorMessage(Throwable throwable) {
        Throwable target = rootCause(throwable);
        if (target == null) {
            return "unknown groovy execution error";
        }
        String message = target.getMessage();
        return message == null || message.trim().isEmpty() ? target.toString() : message;
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable target = throwable;
        while (target != null && target.getCause() != null && target.getCause() != target) {
            target = target.getCause();
        }
        return target;
    }

    private static boolean isBlocked(String className, List<String> blockedClassNames) {
        for (String blockedClassName : blockedClassNames) {
            if (blockedClassName.endsWith(".*")) {
                String prefix = blockedClassName.substring(0, blockedClassName.length() - 1);
                if (className.startsWith(prefix)) {
                    return true;
                }
            } else if (blockedClassName.equals(className)) {
                return true;
            }
        }
        return false;
    }

    private static class SandboxExpressionChecker implements SecureASTCustomizer.ExpressionChecker {

        private final SandboxPolicy policy;

        SandboxExpressionChecker(SandboxPolicy policy) {
            this.policy = policy;
        }

        @Override
        public boolean isAuthorized(Expression expression) {
            if (expression instanceof ConstructorCallExpression) {
                String typeName = typeName(((ConstructorCallExpression) expression).getType());
                return !"java.lang.Thread".equals(typeName) && !isBlocked(typeName, policy.blockedReceivers);
            }
            if (expression instanceof StaticMethodCallExpression) {
                return !isBlocked(typeName(((StaticMethodCallExpression) expression).getOwnerType()), policy.blockedReceivers);
            }
            if (expression instanceof MethodCallExpression) {
                MethodCallExpression methodCall = (MethodCallExpression) expression;
                if ("execute".equals(String.valueOf(methodCall.getMethodAsString()))
                        || "exec".equals(String.valueOf(methodCall.getMethodAsString()))
                        || "exit".equals(String.valueOf(methodCall.getMethodAsString()))
                        || "sleep".equals(String.valueOf(methodCall.getMethodAsString()))
                        || "start".equals(String.valueOf(methodCall.getMethodAsString()))
                        || "stop".equals(String.valueOf(methodCall.getMethodAsString()))) {
                    return false;
                }
                if (methodCall.getObjectExpression() instanceof ClassExpression) {
                    ClassExpression classExpression = (ClassExpression) methodCall.getObjectExpression();
                    return !isBlocked(typeName(classExpression.getType()), policy.blockedReceivers);
                }
            }
            if (expression instanceof ClassExpression) {
                return !isBlocked(typeName(((ClassExpression) expression).getType()), policy.blockedReceivers);
            }
            return true;
        }

        private static String typeName(ClassNode classNode) {
            return classNode == null ? "" : classNode.getName();
        }
    }

    private static class ImportPolicyCustomizer extends CompilationCustomizer {

        private final SandboxPolicy policy;

        ImportPolicyCustomizer(SandboxPolicy policy) {
            super(CompilePhase.SEMANTIC_ANALYSIS);
            this.policy = policy;
        }

        @Override
        public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
            for (ImportNode importNode : source.getAST().getImports()) {
                assertImportAllowed(importNode.getClassName());
            }
            for (ImportNode importNode : source.getAST().getStarImports()) {
                String packageName = importNode.getPackageName();
                assertStarImportAllowed(packageName == null ? "" : packageName + "*");
            }
            for (Map.Entry<String, ImportNode> entry : source.getAST().getStaticImports().entrySet()) {
                assertImportAllowed(entry.getValue().getClassName());
            }
            for (Map.Entry<String, ImportNode> entry : source.getAST().getStaticStarImports().entrySet()) {
                assertImportAllowed(entry.getValue().getClassName());
            }
        }

        private void assertImportAllowed(String className) {
            if (isBlocked(className, policy.blockedImports)) {
                throw new SecurityException("import is blocked by groovy sandbox: " + className);
            }
            if (!policy.allowedImports.contains(className)) {
                throw new SecurityException("import is not whitelisted by groovy sandbox: " + className);
            }
        }

        private void assertStarImportAllowed(String starImport) {
            if (isBlocked(starImport, policy.blockedImports)) {
                throw new SecurityException("star import is blocked by groovy sandbox: " + starImport);
            }
            throw new SecurityException("star import is not allowed by groovy sandbox: " + starImport);
        }
    }

    private static class RestrictedGroovyClassLoader extends GroovyClassLoader {

        private final SandboxPolicy policy;

        RestrictedGroovyClassLoader(ClassLoader parent, CompilerConfiguration config, SandboxPolicy policy) {
            super(parent, config);
            this.policy = policy;
        }

        @Override
        public Class loadClass(String name) throws ClassNotFoundException {
            assertAllowed(name);
            return super.loadClass(name);
        }

        @Override
        protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            assertAllowed(name);
            return super.loadClass(name, resolve);
        }

        @Override
        public Class loadClass(String name, boolean lookupScriptFiles, boolean preferClassOverScript, boolean resolve)
                throws ClassNotFoundException, CompilationFailedException {
            assertAllowed(name);
            return super.loadClass(name, lookupScriptFiles, preferClassOverScript, resolve);
        }

        private void assertAllowed(String className) {
            if (isBlocked(className, policy.blockedImports)) {
                throw new SecurityException("class is blocked by groovy sandbox: " + className);
            }
        }
    }

    private static class SandboxSecurityManager extends SecurityManager {

        private static final ThreadLocal<Boolean> ACTIVE = new ThreadLocal<Boolean>();
        private static volatile SecurityManagerRegistration registration;
        private final SecurityManager delegate;

        SandboxSecurityManager(SecurityManager delegate) {
            this.delegate = delegate;
        }

        static synchronized SecurityManagerRegistration ensureInstalled() {
            if (registration != null) {
                return registration;
            }
            SecurityManager current = System.getSecurityManager();
            if (current instanceof SandboxSecurityManager) {
                registration = new SecurityManagerRegistration(true, "sandbox security manager already installed");
                return registration;
            }
            try {
                System.setSecurityManager(new SandboxSecurityManager(current));
                registration = new SecurityManagerRegistration(true, "sandbox security manager installed");
            } catch (Throwable ex) {
                registration = new SecurityManagerRegistration(false,
                        "security manager unavailable on this JVM: " + ex.getClass().getSimpleName());
            }
            return registration;
        }

        static void activate() {
            ACTIVE.set(Boolean.TRUE);
        }

        static void deactivate() {
            ACTIVE.remove();
        }

        @Override
        public void checkPermission(Permission permission) {
            if (isActive()) {
                denyRuntimePermission(permission);
            }
            if (delegate != null) {
                delegate.checkPermission(permission);
            }
        }

        @Override
        public void checkPermission(Permission permission, Object context) {
            checkPermission(permission);
        }

        @Override
        public void checkExit(int status) {
            if (isActive()) {
                throw new SecurityException("System.exit is blocked in groovy sandbox");
            }
            if (delegate != null) {
                delegate.checkExit(status);
            }
        }

        @Override
        public void checkExec(String command) {
            if (isActive()) {
                throw new SecurityException("process execution is blocked in groovy sandbox");
            }
            if (delegate != null) {
                delegate.checkExec(command);
            }
        }

        @Override
        public void checkRead(String file) {
            if (isActive()) {
                throw new SecurityException("file read is blocked in groovy sandbox");
            }
            if (delegate != null) {
                delegate.checkRead(file);
            }
        }

        @Override
        public void checkWrite(String file) {
            if (isActive()) {
                throw new SecurityException("file write is blocked in groovy sandbox");
            }
            if (delegate != null) {
                delegate.checkWrite(file);
            }
        }

        @Override
        public void checkDelete(String file) {
            if (isActive()) {
                throw new SecurityException("file delete is blocked in groovy sandbox");
            }
            if (delegate != null) {
                delegate.checkDelete(file);
            }
        }

        @Override
        public void checkConnect(String host, int port) {
            if (isActive()) {
                throw new SecurityException("network connect is blocked in groovy sandbox");
            }
            if (delegate != null) {
                delegate.checkConnect(host, port);
            }
        }

        @Override
        public void checkConnect(String host, int port, Object context) {
            checkConnect(host, port);
        }

        @Override
        public void checkListen(int port) {
            if (isActive()) {
                throw new SecurityException("network listen is blocked in groovy sandbox");
            }
            if (delegate != null) {
                delegate.checkListen(port);
            }
        }

        @Override
        public void checkAccept(String host, int port) {
            if (isActive()) {
                throw new SecurityException("network accept is blocked in groovy sandbox");
            }
            if (delegate != null) {
                delegate.checkAccept(host, port);
            }
        }

        @Override
        public void checkMulticast(InetAddress address) {
            if (isActive()) {
                throw new SecurityException("network multicast is blocked in groovy sandbox");
            }
            if (delegate != null) {
                delegate.checkMulticast(address);
            }
        }

        @Override
        public void checkPropertyAccess(String key) {
            if (isActive() && ("user.home".equals(key) || "user.dir".equals(key)
                    || "java.home".equals(key) || "java.class.path".equals(key))) {
                throw new SecurityException("sensitive property access is blocked in groovy sandbox: " + key);
            }
            if (delegate != null) {
                delegate.checkPropertyAccess(key);
            }
        }

        private static boolean isActive() {
            return Boolean.TRUE.equals(ACTIVE.get());
        }

        private static void denyRuntimePermission(Permission permission) {
            if (permission instanceof RuntimePermission) {
                String name = permission.getName();
                if (name != null && (name.startsWith("exitVM")
                        || "setSecurityManager".equals(name)
                        )) {
                    throw new SecurityException("runtime permission is blocked in groovy sandbox: " + name);
                }
            }
        }
    }

    private static class SecurityManagerRegistration {

        private final boolean installed;
        private final String message;

        SecurityManagerRegistration(boolean installed, String message) {
            this.installed = installed;
            this.message = message;
        }
    }

    private static class SandboxPolicy {

        private int timeoutMillis;
        private boolean installSecurityManager;
        private List<String> allowedImports;
        private List<String> blockedImports;
        private List<String> blockedReceivers;
    }
}
