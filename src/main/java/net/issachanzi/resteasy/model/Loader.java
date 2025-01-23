package net.issachanzi.resteasy.model;

import java.awt.font.LineBreakMeasurer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class to search for subclasses of EasyModel in the classpath
 */
public class Loader {

    private ClassLoader classLoader;

    public Loader () {
        this (ClassLoader.getSystemClassLoader());
    }

    public Loader (ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Searches for all subclasses of EasyModel on the classpath
     *
     * @return A collection of model classes found
     */
    public Collection<Class<? extends EasyModel>> load() {
        ClassLoader cl = Loader.class.getClassLoader();
        if (cl instanceof URLClassLoader) {
            try {
                return loadMaven ((URLClassLoader) cl);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        var result = new LinkedList<Class <? extends EasyModel>>();

        String classPath = System.getProperty ("java.class.path");
        String [] classPaths = classPath.split (File.pathSeparator);

        for (var root: classPaths) {
            loadPath(root, result);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public Collection <Class <? extends EasyModel>> loadMaven (
            URLClassLoader cl
    ) throws URISyntaxException {
        URL[] urls = cl.getURLs();

        Collection <String> classNames = new LinkedList<> ();

        for (var url : urls) {
            File file = new File (url.toURI());
            if (file.getName().endsWith(".jar")) {
                // Load from dir
                try {
                    try (
                        var zip = new ZipInputStream(
                            new FileInputStream(file)
                        )
                    ) {
                        for (
                            ZipEntry entry = zip.getNextEntry();
                            entry != null;
                            entry = zip.getNextEntry()
                        ) {
                            if (entry.getName ().endsWith(".class")) {
                                String className = entry.getName()
                                    .replace('/', '.')
                                    .replace ('$', '.')
                                    .substring(
                                            0,
                                            entry.getName().length() - ".class".length()
                                    );

                                classNames.add(className);
                            }
                        }
                    }
                }
                catch (IOException ex) {
                    throw new RuntimeException (ex);
                }
            }
            else if (file.isDirectory()) {
                Vector <File> files = new Vector <> ();

                String pathPrefix = file.getAbsolutePath() + "/";

                files.add (file);

                for (int i = 0; i < files.size(); i++) {
                    var f = files.get (i);

                    files.addAll(Arrays.asList(Objects.requireNonNullElse(
                            f.listFiles(),
                            new File[0]))
                    );
                }

                classNames.addAll (files.stream()
                    .filter (
                    f -> !f.isDirectory() && f.getName().endsWith(".class"))
                    .map(File::getAbsolutePath)
                    .map (name -> name
                        .substring (
                            pathPrefix.length(),
                            name.length () - ".class".length()
                        )
                        .replace ('/', '.')
                    ).toList()
                );
            }
        }

        var result = new LinkedList <Class <? extends EasyModel>> ();

        for (String name : classNames) {
            try {
                Class <?> clazz = cl.loadClass(name);

                if (
                    clazz.getPackageName()
                        .startsWith("net.issachanzi.resteasy")
                ) {
                    continue;
                }

                if (EasyModel.class.isAssignableFrom (clazz)) {
                    result.add ((Class <? extends EasyModel>) clazz);
                }
            } catch (ClassNotFoundException ignored) {}
        }

        return result;
    }

    public void loadPath(String root, LinkedList<Class<? extends EasyModel>> result) {
        File dir = new File (root);
        var models = loadDir(dir, dir);

        result.addAll(models);
    }

    private Collection <Class <? extends EasyModel>> loadDir(
            File file,
            File root
    ) {
        var result = new LinkedList<Class<? extends EasyModel>> ();

        if (file.isDirectory()) {
            for (var f : Objects.requireNonNull(file.listFiles())) {
                var contents = loadDir (f, root);

                result.addAll(contents);
            }
        }
        else if (file.getName().endsWith(".class")) {
            try {
                result.addAll(loadClassFile(file, root));
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }

        return result;
    }

    private Collection <Class <? extends EasyModel>> loadClassFile(
            File file,
            File root
    ) throws ClassNotFoundException {
        Path rootPath = Path.of (root.getPath());
        Path filePath = Path.of (file.getPath());
        String relativePath = rootPath.relativize(filePath).toString();

        String className = relativePath
                .replace(File.separatorChar, '.')
                .substring(0, relativePath.length() - 6); // Remove .class extension

        Class<?> clazz = classLoader.loadClass (className);

        return loadModel (clazz);
    }

    @SuppressWarnings("unchecked")
    private Collection <Class <? extends EasyModel>> loadModel(
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
