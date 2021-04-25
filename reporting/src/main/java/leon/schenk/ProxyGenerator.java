package leon.schenk;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Configurable;

import leon.schenk.interfaces.BeanDefinition;
import leon.schenk.interfaces.ConfigurableApplicationContext;
import leon.schenk.interfaces.ConfigurableEnvironment;
import leon.schenk.interfaces.ConfigurableListableBeanFactory;
import leon.schenk.interfaces.EnumerablePropertySource;
import leon.schenk.interfaces.MutablePropertySources;
import leon.schenk.interfaces.PropertySource;
import leon.schenk.interfaces.ResolvableType;
import leon.schenk.interfaces.SpringApplication;

public class ProxyGenerator {
    private final ClassLoader classLoader;
    private final List<Class<?>> interfaceClasses = new ArrayList<>();

    public ProxyGenerator(final ClassLoader classLoader) {
        this.classLoader = classLoader;

        interfaceClasses.add(BeanDefinition.class);
        interfaceClasses.add(ConfigurableApplicationContext.class);
        interfaceClasses.add(ConfigurableEnvironment.class);
        interfaceClasses.add(ConfigurableListableBeanFactory.class);
        interfaceClasses.add(EnumerablePropertySource.class);
        interfaceClasses.add(MutablePropertySources.class);
        interfaceClasses.add(PropertySource.class);
        interfaceClasses.add(ResolvableType.class);
        interfaceClasses.add(SpringApplication.class);
    }

    public Class<?> referenceClass(final String className) {
        try {
            return classLoader.loadClass(className);
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public <T> T staticContext(final Class<T> clazz) throws ClassNotFoundException {
        final String target = clazz.getAnnotation(leon.schenk.ProxyInterface.class).target();
        return clazz.cast(Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{clazz}, new StaticContextInvocationHandler(clazz, target)));
    }

    public Object proxy(final Object target, final Class<?> targetClass, final Class<?> interfaceClass) {
        if (target == null) {
            return null;
        } else if (interfaceClass.isAnnotationPresent(leon.schenk.ProxyInterface.class)) {
            return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{interfaceClass}, new TargetContextInvocationHandler(target));
        } else if (interfaceClass.isInterface()) {
            return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{interfaceClass}, new TargetContextInvocationHandler(target, targetClass));
        } else if (interfaceClasses.stream().map(clazz -> clazz.getAnnotation(leon.schenk.ProxyInterface.class).target()).map(clazz -> referenceClass(clazz)).anyMatch(clazz -> clazz.isInstance(target))) {
            final List<Class<?>> interfaceInstances = interfaceClasses.stream()
                .filter(clazz -> referenceClass(clazz.getAnnotation(leon.schenk.ProxyInterface.class).target()).isInstance(target))
                .collect(Collectors.toList());

            final List<Class<?>> translatedInterfaces =  interfaceInstances.stream()
                .map(clazz -> referenceClass(clazz.getAnnotation(leon.schenk.ProxyInterface.class).target()))
                .collect(Collectors.toList());

            return Proxy.newProxyInstance(this.getClass().getClassLoader(), interfaceInstances.toArray(new Class<?>[]{}), new TargetContextInvocationHandler(target, translatedInterfaces.toArray(new Class<?>[]{})));
        } else {
            return target;
        }
    }

    public Object unproxy(final Object target) {
        if (target != null && Proxy.isProxyClass(target.getClass())) {
            final InvocationHandler invocationHandler = Proxy.getInvocationHandler(target);
            if (invocationHandler instanceof Unwrappable) {
                return ((Unwrappable)invocationHandler).getTarget();
            } else {
                return target;
            }
        } else {
            return target;
        }
    }

    private static interface Unwrappable {
        Object getTarget();
    }

    private final class TargetContextInvocationHandler implements InvocationHandler, Unwrappable {
        private final Object target;
        private final Class<?>[] targetClassses;

        public TargetContextInvocationHandler(final Object target) {
            this.target = target;
            this.targetClassses = new Class<?>[]{target.getClass()};
        }

        public TargetContextInvocationHandler(final Object target, final Class<?> targetClass) {
            this.target = target;
            this.targetClassses = new Class<?>[]{targetClass};
        }

        public TargetContextInvocationHandler(final Object target, final Class<?>[] targetClasses) {
            this.target = target;
            this.targetClassses = targetClasses.clone();
        }

        @Override
        public Object getTarget() {
            return target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            final Class<?>[] parameterTypesTarget = translateToTargetTypes(method.getParameterTypes());
            final Object[] translatedArguments = translateToTargetObject(method, args);

            Method targetMethod = null;
            for (final Class<?> targetClass : targetClassses) {
                try {
                    targetMethod = targetClass.getMethod(method.getName(), parameterTypesTarget);
                } catch (final NoSuchMethodException e) {
                } catch (final SecurityException e) {
                }
            }
            if (targetMethod == null) {
                throw new NoSuchMethodException();
            }

            final Object returnValue = targetMethod.invoke(target, translatedArguments);
            final Class<?> returnType = targetMethod.getReturnType();

            return proxy(returnValue, returnType, method.getReturnType());
        }
    }

    private final class StaticContextInvocationHandler implements InvocationHandler {
        private final Class<?> interfaceClass;
        private final Class<?> targetClass;

        public StaticContextInvocationHandler(final Class<?> interfaceClass, final String targetClass) throws ClassNotFoundException {
            this.interfaceClass = interfaceClass;
            this.targetClass = classLoader.loadClass(targetClass);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            final Class<?>[] parameterTypesTarget = translateToTargetTypes(method.getParameterTypes());
            final Object[] translatedArguments = translateToTargetObject(method, args);

            if (method.isAnnotationPresent(leon.schenk.ProxyConstructor.class)) {
                final Constructor<?> constructor = targetClass.getConstructor(parameterTypesTarget);
                final Object target = constructor.newInstance(translatedArguments);
                return proxy(target, targetClass, interfaceClass);
            }
            return null;
        }
    }

    private Class<?>[] translateToTargetTypes(final Class<?>[] types) throws ClassNotFoundException {
        final Class<?>[] interfaceTypes = types.clone();
        for (int i = 0; i < interfaceTypes.length; i++) {
            interfaceTypes[i] = interfaceTypes[i].isAnnotationPresent(leon.schenk.ProxyInterface.class) ? classLoader.loadClass(interfaceTypes[i].getAnnotation(leon.schenk.ProxyInterface.class).target()) : interfaceTypes[i];
        }
        return interfaceTypes;
    }

    private Object[] translateToTargetObject(Method method, Object[] args) {
        if (args == null) {
            return null;
        } else {
            final Object[] arguments = args.clone();
            for (int i = 0; i < arguments.length; i++) {
                arguments[i] = unproxy(arguments[i]);
            }
            return arguments;
        }
    }
}
