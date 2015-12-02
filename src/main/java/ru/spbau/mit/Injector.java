package ru.spbau.mit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Injector {

    /**
     * Create and initialize object of `rootClassName` class using classes from
     * `implementationClassNames` for concrete dependencies.
     */

    private static HashMap<String, Object> objects = new HashMap<>();
    private static HashMap<String, Boolean> color = new HashMap<>();
    private static ArrayList<String> classes;

    public static Object initialize(String rootClassName, List<String> implementationClassNames) throws Exception {
        color = new HashMap<>();
        objects = new HashMap<>();
        classes = new ArrayList<>(implementationClassNames);
        classes.add(rootClassName);
        dfs(rootClassName);
        return objects.get(rootClassName);
    }

    private static void dfs(String vertex) throws Exception {
        if (color.containsKey(vertex) && color.get(vertex)) {
            throw new InjectionCycleException();
        }
        color.put(vertex, true);
        Constructor<?> constructor = Class.forName(vertex).getConstructors()[0];
        for (Class<?> param : constructor.getParameterTypes()) {
            int modifier = param.getModifiers();
            Boolean f = false;
            String par = null;
            for (String cl : classes) {
                if (param.isAssignableFrom(Class.forName(cl))) {
                    if (f) {
                        throw new AmbiguousImplementationException();
                    }
                    f = true;
                    par = cl;
                }
            }
            if (!f) {
                throw new ImplementationNotFoundException();
            }
            if (!objects.containsKey(par)) {
                dfs(par);
            }
            objects.put(param.getName(), objects.get(par));

        }
        Object[] initiars = new Object[constructor.getParameterTypes().length];
        int i = 0;
        for (Class<?> param : constructor.getParameterTypes()) {
            initiars[i] = objects.get(param.getName());
            i++;
        }
        objects.put(vertex, constructor.newInstance(initiars));
        color.put(vertex, false);
    }
}