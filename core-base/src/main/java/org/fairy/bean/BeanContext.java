/*
 * MIT License
 *
 * Copyright (c) 2021 Imanity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.fairy.bean;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fairy.Fairy;
import org.fairy.FairyBootstrap;
import org.fairy.bean.details.*;
import org.fairy.bean.details.constructor.BeanParameterDetailsMethod;
import org.fairy.bean.exception.ServiceAlreadyExistsException;
import org.fairy.plugin.Plugin;
import org.fairy.plugin.PluginListenerAdapter;
import org.fairy.plugin.PluginManager;
import org.fairy.reflect.Reflect;
import org.fairy.reflect.ReflectLookup;
import org.fairy.util.AccessUtil;
import org.fairy.util.NonNullArrayList;
import org.fairy.util.SimpleTiming;
import org.fairy.util.Utility;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class BeanContext {

    public static boolean SHOW_LOGS = false;
    public static BeanContext INSTANCE;
    public static final int PLUGIN_LISTENER_PRIORITY = 100;

    /**
     * Logging
     */
    protected static final Logger LOGGER = LogManager.getLogger(BeanContext.class);
    protected static void log(String msg, Object... replacement) {
        if (SHOW_LOGS) {
            LOGGER.info("[BeanContext] " + String.format(msg, replacement));
        }
    }
    protected static SimpleTiming logTiming(String msg) {
        return SimpleTiming.create(time -> log("Ended %s - took %d ms", msg, time));
    }

    /**
     * Lookup Storages
     */
    private final Map<Class<?>, BeanDetails> beanByType = new ConcurrentHashMap<>();
    private final Map<String, BeanDetails> beanByName = new ConcurrentHashMap<>();

    /**
     * NOT THREAD SAFE
     */
    private final List<BeanDetails> sortedBeans = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Initializing Method for Bean Context
     */
    public void init() {
        INSTANCE = this;

        this.registerBean(new SimpleBeanDetails(this, "beanContext", this.getClass()));
        log("BeanContext has been registered as bean.");

        ComponentRegistry.registerComponentHolders();
        try {
            this.scanClasses("framework", BeanContext.class.getClassLoader(), Collections.singletonList("org.fairy"));
        } catch (Throwable throwable) {
            LOGGER.error("Error while scanning classes for framework", throwable);
            Fairy.getPlatform().shutdown();
            return;
        }

        if (PluginManager.isInitialized()) {
            log("Find PluginManager, attempt to register Plugin Listeners");

            PluginManager.INSTANCE.registerListener(new PluginListenerAdapter() {
                @Override
                public void onPluginEnable(Plugin plugin) {
                    BeanDetails beanDetails = new SimpleBeanDetails(plugin, plugin.getName(), plugin.getClass());

                    try {
                        beanDetails.bindWith(plugin);
                        registerBean(beanDetails, false);
                        log("Plugin " + plugin.getName() + " has been registered as bean.");
                    } catch (Throwable throwable) {
                        LOGGER.error("An error occurs while registering plugin", throwable);
                        try {
                            plugin.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        return;
                    }

                    try {
                        scanClasses(plugin.getName(), plugin.getPluginClassLoader(), findClassPaths(plugin.getClass()), beanDetails);
                    } catch (Throwable throwable) {
                        LOGGER.error("An error occurs while handling scanClasses()", throwable);
                        try {
                            plugin.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }

                @Override
                public void onPluginDisable(Plugin plugin) {
                    Collection<BeanDetails> beanDetailsList = findDetailsBindWith(plugin);
                    try {
                        call(PreDestroy.class, beanDetailsList);
                    } catch (Throwable throwable) {
                        LOGGER.error(throwable);
                    }

                    beanDetailsList.forEach(details -> {
                        log("Bean " + details.getName() + " Disabled, due to plugin " + plugin.getName() + " disabled.");

                        try {
                            details.onDisable();
                            unregisterBean(details);
                        } catch (Throwable throwable) {
                            LOGGER.error(throwable);
                        }
                    });

                    try {
                        call(PostDestroy.class, beanDetailsList);
                    } catch (Throwable throwable) {
                        LOGGER.error(throwable);
                    }
                }

                @Override
                public int priority() {
                    return PLUGIN_LISTENER_PRIORITY;
                }
            });
        }

        FairyBootstrap.INSTANCE.getPlatform().onPostServicesInitial();
    }

    /**
     * Shutdown Method for Bean Context
     */
    public void stop() {
        List<BeanDetails> detailsList = Lists.newArrayList(this.sortedBeans);
        Collections.reverse(detailsList);

        this.call(PreDestroy.class, detailsList);

        for (BeanDetails details : detailsList) {
            log("Bean " + details.getName() + " Disabled, due to framework being disabled.");

            details.onDisable();
            unregisterBean(details);
        }

        this.call(PostDestroy.class, detailsList);
    }

    public BeanDetails registerBean(BeanDetails beanDetails) {
        return this.registerBean(beanDetails, true);
    }

    public BeanDetails registerBean(BeanDetails beanDetails, boolean sort) {
        this.beanByType.put(beanDetails.getType(), beanDetails);
        this.beanByName.put(beanDetails.getName(), beanDetails);
        if (sort) {
            this.sortedBeans.add(beanDetails);
        }

        return beanDetails;
    }

    public Collection<BeanDetails> unregisterBean(Class<?> type) {
        return this.unregisterBean(this.getBeanDetails(type));
    }

    public Collection<BeanDetails> unregisterBean(String name) {
        return this.unregisterBean(this.getBeanByName(name));
    }

    // UNFINISHED, or finished? idk
    public Collection<BeanDetails> unregisterBean(@NonNull BeanDetails beanDetails) {
        this.beanByType.remove(beanDetails.getType());
        this.beanByName.remove(beanDetails.getName());

        this.lock.writeLock().lock();
        this.sortedBeans.remove(beanDetails);
        this.lock.writeLock().unlock();

        final ImmutableList.Builder<BeanDetails> builder = ImmutableList.builder();

        // Unregister Child Dependency
        for (String child : beanDetails.getChildren()) {
            BeanDetails childDetails = this.getBeanByName(child);

            builder.add(childDetails);
            builder.addAll(this.unregisterBean(childDetails));
        }

        // Remove Children from dependencies
        for (String dependency : beanDetails.getAllDependencies()) {
            BeanDetails dependDetails = this.getBeanByName(dependency);

            if (dependDetails != null) {
                dependDetails.removeChildren(beanDetails.getName());
            }
        }

        return builder.build();
    }

    public BeanDetails getBeanDetails(Class<?> type) {
        return this.beanByType.get(type);
    }

    public Object getBean(@NonNull Class<?> type) {
        BeanDetails details = this.getBeanDetails(type);
        if (details == null) {
            return null;
        }
        return details.getInstance();
    }

    public BeanDetails getBeanByName(String name) {
        return this.beanByName.get(name);
    }

    public boolean isRegisteredBeans(String... beans) {
        for (String bean : beans) {
            BeanDetails dependencyDetails = this.getBeanByName(bean);
            if (dependencyDetails == null || dependencyDetails.getInstance() == null) {
                return false;
            }
        }
        return true;
    }

    public boolean isBean(Class<?> beanClass) {
        return this.beanByType.containsKey(beanClass);
    }

    public boolean isBean(Object bean) {
        return this.isBean(bean.getClass());
    }

    public Collection<BeanDetails> findDetailsBindWith(Plugin plugin) {
        return this.beanByType.values()
                .stream()
                .filter(beanDetails -> beanDetails.isBind() && beanDetails.getBindPlugin().equals(plugin))
                .collect(Collectors.toList());
    }

    /**
     * Injections
     */

    public void injectAutowired(Field field, Object instance) throws ReflectiveOperationException {
        Class<?> type = field.getType();
        boolean optional = false, beanHolder = false;
        if (type == Optional.class) {
            optional = true;
            type = Utility.sneaky(() -> Reflect.getParameter(field, 0));
            if (type == null) {
                return;
            }
        } else if (type == BeanHolder.class) {
            beanHolder = true;

            type = Utility.sneaky(() -> Reflect.getParameter(field, 0));
            if (type == null) {
                return;
            }
        }
        Object objectToInject = this.getBean(type);
        if (optional) {
            objectToInject = Optional.ofNullable(objectToInject);
        } else if (beanHolder) {
            objectToInject = new BeanHolder<>(objectToInject);
        }

        if (objectToInject != null) {
            AccessUtil.setAccessible(field);
            Reflect.setField(instance, field, objectToInject);
        } else {
            LOGGER.error("The Autowired field " + field.toString() + " trying to wired with type " + type.getSimpleName() + " but couldn't find any matching Service! (or not being registered)");
        }
    }

    public void injectBeans(Object instance) {
        try {
            Collection<Field> fields = Reflect.getDeclaredFields(instance.getClass());

            for (Field field : fields) {
                int modifiers = field.getModifiers();
                Autowired annotation = field.getAnnotation(Autowired.class);

                if (annotation == null || Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
                    continue;
                }

                this.injectAutowired(field, instance);
            }
        } catch (Throwable throwable) {
            LOGGER.error("Error while injecting beans for " + instance.getClass().getSimpleName(), throwable);
        }
    }

    /**
     * Registration
     */

    public ComponentBeanDetails registerComponent(Object instance, Class<?> type, ComponentHolder componentHolder) throws InvocationTargetException, IllegalAccessException {
        Component component = type.getAnnotation(Component.class);
        if (component == null) {
            throw new IllegalArgumentException("The type " + type.getName() + " doesn't have Component annotation!");
        }

        ServiceDependency serviceDependency = type.getAnnotation(ServiceDependency.class);
        if (serviceDependency != null) {
            for (String dependency : serviceDependency.dependencies()) {
                if (!this.isRegisteredBeans(dependency)) {
                    switch (serviceDependency.type().value()) {
                        case FORCE:
                            LOGGER.error("Couldn't find the dependency " + dependency + " for " + type.getSimpleName() + "!");
                        case SUB_DISABLE:
                            return null;
                        case SUB:
                            break;
                    }
                }
            }
        }

        String name = component.value();
        if (name.length() == 0) {
            name = instance.getClass().getName();
        }

        ComponentBeanDetails details = new ComponentBeanDetails(type, instance, name, componentHolder);
        if (!details.shouldInitialize()) {
            return null;
        }

        this.registerBean(details);
        this.attemptBindPlugin(details);

        try {
            details.call(PreInitialize.class);
        } catch (Throwable throwable) {
            LOGGER.error(throwable);
        }
        return details;
    }

    private void attemptBindPlugin(BeanDetails beanDetails) {
        if (PluginManager.isInitialized()) {
            Plugin plugin = PluginManager.INSTANCE.getPluginByClass(beanDetails.getType());

            if (plugin != null) {
                beanDetails.bindWith(plugin);

                log("Bean " + beanDetails.getName() + " is now bind with plugin " + plugin.getName());
            }
        }
    }

    public void scanClasses(String scanName, ClassLoader classLoader, Collection<String> classPaths, BeanDetails... included) throws Exception {
        log("Start scanning beans for %s with packages [%s]...", scanName, String.join(" ", classPaths));

        // Build the instance for Reflection Lookup
        ReflectLookup reflectLookup;
        try (SimpleTiming ignored = logTiming("Reflect Lookup building")) {
            reflectLookup = new ReflectLookup(Collections.singleton(classLoader), classPaths);
        }

        // Scanning through the JAR to see every Service Bean can be registered
        List<BeanDetails> beanDetailsList;
        try (SimpleTiming ignored = logTiming("Scanning Beans")) {
            beanDetailsList = new NonNullArrayList<>(Arrays.asList(included));

            for (Class<?> type : reflectLookup.findAnnotatedClasses(Service.class)) {

                Service service = type.getAnnotation(Service.class);
                Preconditions.checkNotNull(service, "The type " + type.getName() + " doesn't have @Service annotation!");

                String name = service.name();

                if (this.getBeanByName(name) == null) {
                    ServiceBeanDetails beanDetails = new ServiceBeanDetails(type, name, service.dependencies());

                    log("Found " + name + " with type " + type.getSimpleName() + ", Registering it as bean...");

                    this.attemptBindPlugin(beanDetails);
                    this.registerBean(beanDetails, false);

                    beanDetailsList.add(beanDetails);
                } else {
                    new ServiceAlreadyExistsException(name).printStackTrace();
                }
            }
        }

        // Scanning methods that registers bean
        try (SimpleTiming ignored = logTiming("Scanning Bean Method")) {
            for (Method method : reflectLookup.findAnnotatedStaticMethods(Bean.class)) {
                if (method.getReturnType() == void.class) {
                    new IllegalArgumentException("The Method " + method.toString() + " has annotated @Bean but no return type!").printStackTrace();
                }
                BeanParameterDetailsMethod detailsMethod = new BeanParameterDetailsMethod(method, this);
                final Object instance = detailsMethod.invoke(null, this);

                Bean bean = method.getAnnotation(Bean.class);
                if (bean == null) {
                    continue;
                }

                String name = bean.name();
                if (name.isEmpty()) {
                    name = instance.getClass().toString();
                }

                if (this.getBeanByName(name) == null) {
                    List<String> dependencies = new ArrayList<>();
                    for (Parameter type : detailsMethod.getParameters()) {
                        BeanDetails details = this.getBeanDetails(type.getType());
                        if (details != null) {
                            dependencies.add(details.getName());
                        }
                    }

                    BeanDetails beanDetails = new DependenciesBeanDetails(instance.getClass(), instance, name, dependencies.toArray(new String[0]));

                    log("Found " + name + " with type " + instance.getClass().getSimpleName() + ", Registering it as bean...");

                    this.attemptBindPlugin(beanDetails);
                    this.registerBean(beanDetails, false);

                    beanDetailsList.add(beanDetails);
                } else {
                    new ServiceAlreadyExistsException(name).printStackTrace();
                }
            }
        }

        // Load Beans in Dependency Tree Order
        try (SimpleTiming ignored = logTiming("Initializing Beans")) {
            beanDetailsList = this.loadInOrder(beanDetailsList);
        } catch (Throwable throwable) {
            LOGGER.error("An error occurs while handling loadInOrder()", throwable);
        }

        // Unregistering Beans that returns false in shouldInitialize
        try (SimpleTiming ignored = logTiming("Unregistering Disabled Beans")) {
            this.sortedBeans.addAll(beanDetailsList);

            for (BeanDetails beanDetails : ImmutableList.copyOf(beanDetailsList)) {
                if (!beanDetailsList.contains(beanDetails)) {
                    continue;
                }
                try {
                    if (!beanDetails.shouldInitialize()) {
                        log("Unregistering " + beanDetails.getName() + " due to it cancelled to register");

                        beanDetailsList.remove(beanDetails);
                        for (BeanDetails details : this.unregisterBean(beanDetails)) {
                            log("Unregistering " + details.getName() + " due to it dependency unregistered");

                            beanDetailsList.remove(details);
                        }
                    }
                } catch (InvocationTargetException | IllegalAccessException e) {
                    LOGGER.error(e);
                    this.unregisterBean(beanDetails);
                }
            }
        }

        // Call @PreInitialize methods for bean
        try (SimpleTiming ignored = logTiming("Call @PreInitialize")) {
            this.call(PreInitialize.class, beanDetailsList);
        }

        // Scan Components
        try (SimpleTiming ignored = logTiming("Scanning Components")) {
            beanDetailsList.addAll(ComponentRegistry.scanComponents(this, reflectLookup));
        }

        // Inject @Autowired fields for beans
        try (SimpleTiming ignored = logTiming("Injecting Beans")) {
            beanDetailsList.forEach(beanDetails -> {
                Object instance = beanDetails.getInstance();
                if (instance != null) {
                    this.injectBeans(instance);
                }
            });
        }

        // Inject @Autowired static fields
        try (SimpleTiming ignored = logTiming("Injecting Static Autowired Fields")) {
            for (Field field : reflectLookup.findAnnotatedStaticFields(Autowired.class)) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                this.injectAutowired(field, null);
            }
        }

        // Call onEnable() for Components
        try (SimpleTiming ignored = logTiming("Call onEnable() for Components")) {
            beanDetailsList.forEach(BeanDetails::onEnable);
        }

        // Call @PostInitialize
        try (SimpleTiming ignored = logTiming("Call @PostInitialize")) {
            this.call(PostInitialize.class, beanDetailsList);
        }

    }

    public void call(Class<? extends Annotation> annotation, Collection<BeanDetails> beanDetailsList) {
        for (BeanDetails beanDetails : beanDetailsList) {
            try {
                beanDetails.call(annotation);
            } catch (Throwable throwable) {
                LOGGER.error(throwable);
            }
        }
    }

    private List<BeanDetails> loadInOrder(List<BeanDetails> beanDetailsList) {
        Map<String, BeanDetails> unloaded = new HashMap<>();
        for (BeanDetails beanDetails : beanDetailsList) {
            unloaded.put(beanDetails.getName(), beanDetails);

            if (beanDetails instanceof ServiceBeanDetails) {
                ((ServiceBeanDetails) beanDetails).setupConstruction(this);
            }
        }

        // Remove Services without valid dependency
        Iterator<Map.Entry<String, BeanDetails>> removeIterator = unloaded.entrySet().iterator();
        while (removeIterator.hasNext()) {
            Map.Entry<String, BeanDetails> entry = removeIterator.next();
            BeanDetails beanDetails = entry.getValue();

            if (!beanDetails.hasDependencies()) {
                continue;
            }

            for (Map.Entry<ServiceDependencyType, List<String>> allDependency : beanDetails.getDependencyEntries()) {
                final ServiceDependencyType type = allDependency.getKey();

                search: for (String dependency : allDependency.getValue()) {
                    BeanDetails dependencyDetails = this.getBeanByName(dependency);

                    if (dependencyDetails == null) {
                        switch (type) {
                            case FORCE:
                                LOGGER.error("Couldn't find the dependency " + dependency + " for " + beanDetails.getName() + "!");
                                removeIterator.remove();
                                break search;
                            case SUB_DISABLE:
                                System.out.println(beanDetails.getName());
                                removeIterator.remove();
                                break search;
                            case SUB:
                                break;
                        }
                    // Prevent dependency each other
                    } else {
                        if (dependencyDetails.hasDependencies()
                                && dependencyDetails.getAllDependencies().contains(beanDetails.getName())) {
                            LOGGER.error("Target " + beanDetails.getName() + " and " + dependency + " depend to each other!");
                            removeIterator.remove();

                            unloaded.remove(dependency);
                            break;
                        }

                        dependencyDetails.addChildren(beanDetails.getName());
                    }
                }
            }
        }

        // Continually loop until all dependency found and loaded
        List<BeanDetails> sorted = new NonNullArrayList<>();

        while (!unloaded.isEmpty()) {
            Iterator<Map.Entry<String, BeanDetails>> iterator = unloaded.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, BeanDetails> entry = iterator.next();
                BeanDetails beanDetails = entry.getValue();
                boolean missingDependencies = false;

                for (Map.Entry<ServiceDependencyType, List<String>> dependencyEntry : beanDetails.getDependencyEntries()) {
                    final ServiceDependencyType type = dependencyEntry.getKey();
                    for (String dependency : dependencyEntry.getValue()) {
                        BeanDetails dependencyDetails = this.getBeanByName(dependency);
                        if (dependencyDetails != null && dependencyDetails.getInstance() != null) {
                            continue;
                        }

                        if (type == ServiceDependencyType.SUB && !unloaded.containsKey(dependency)) {
                            continue;
                        }

                        missingDependencies = true;
                    }
                }

                if (!missingDependencies) {
                    if (beanDetails instanceof ServiceBeanDetails) {
                        ((ServiceBeanDetails) beanDetails).build(this);
                    }

                    sorted.add(beanDetails);
                    iterator.remove();
                }
            }
        }

        return sorted;
    }

    public List<String> findClassPaths(Class<?> plugin) {
        ClasspathScan annotation = plugin.getAnnotation(ClasspathScan.class);

        if (annotation != null) {
            return Lists.newArrayList(annotation.value());
        }

        return Collections.emptyList();
    }

}
