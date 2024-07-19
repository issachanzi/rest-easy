package net.issachanzi.resteasy.model;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Utility class to search for subclasses of EasyModel in the classpath
 */
public class Loader {

    /**
     * Searches for all subclasses of EasyModel on the classpath
     *
     * @return A collection of model classes found
     */
    public static Collection<Class<? extends EasyModel>> load() {
        var result = new LinkedList<Class <? extends EasyModel>>();

        String classPath = System.getProperty ("java.class.path");
        String [] classPaths = classPath.split (File.pathSeparator);

        for (var root: classPaths) {
            File dir = new File (root);
            var models = loadDir(dir, dir);

            result.addAll(models);
        }

        return result;
    }

    private static Collection <Class <? extends EasyModel>> loadDir(
            File file,
            File root
    ) {
        var result = new LinkedList<Class<? extends EasyModel>> ();

        if (file.isDirectory()) {
            for (var f : file.listFiles()) {
                var contents = loadDir (f, root);

                result.addAll(contents);
            }
        }
        else if (file.getName().endsWith(".class")) {
            try {
                result.addAll(loadClassFile(file, root));
            } catch (ClassNotFoundException _) {}
        }

        return result;
    }

    private static Collection <Class <? extends EasyModel>> loadClassFile(
            File file,
            File root
    ) throws ClassNotFoundException {
        Path rootPath = Path.of (root.getPath());
        Path filePath = Path.of (file.getPath());
        String relativePath = rootPath.relativize(filePath).toString();

        String className = relativePath
                .replace(File.separatorChar, '.')
                .substring(0, relativePath.length() - 6); // Remove .class extension

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        Class<?> clazz = classLoader.loadClass (className);

        return loadModel (clazz);
    }

    @SuppressWarnings("unchecked")
    private static Collection <Class <? extends EasyModel>> loadModel(
            Class<?> clazz
    ) {
        var result = new LinkedList<Class <? extends EasyModel>> ();

        boolean isModel
                =  EasyModel.class.isAssignableFrom(clazz) // clazz must extend EasyModel
                && !EasyModel.class.equals(clazz); // clazz must not be EasyModel

        if (isModel) {
            result.add((Class<? extends EasyModel>) clazz);
        }

        // Recurse for any inner classes inside clazz
        for (Class <?> innerClazz : clazz.getDeclaredClasses()) {
            var innerModels = loadModel (innerClazz);

            result.addAll (innerModels);
        }

        return result;
    }

}
