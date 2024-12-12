/*
 * Author: Matěj Šťastný
 * Date created: 12/2/2024
 * Github link: https://github.com/kireiiiiiiii/Flaggi
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package flaggi.util;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A utillity method class designed to handle file related stuff.
 * 
 */
public class FileUtil {

    /**
     * Gets a list of all directories in the given path. The path must be a jar
     * relative path.
     * 
     * @param path - target path.
     * @return - list of {@code String} dir names.
     */
    public static String[] listDirectoriesInJar(String path) {
        if (!path.endsWith("/")) {
            path += "/";
        }

        List<String> directories = new ArrayList<>();
        try {
            Enumeration<URL> resources = FileUtil.class.getClassLoader().getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if ("jar".equals(resource.getProtocol())) {
                    JarURLConnection connection = (JarURLConnection) resource.openConnection();
                    try (JarFile jarFile = connection.getJarFile()) {
                        Enumeration<JarEntry> entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String entryName = entry.getName();
                            if (entryName.startsWith(path) && entryName.endsWith("/") && !entryName.equals(path)) {
                                directories.add(formatDirName(path, entryName));
                            }

                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return directories.toArray(new String[0]);
    }

    /**
     * Formatting method/
     * 
     * @see listDirectoriesInJar
     * @param originalPath
     * @param path
     * @return - formatted {@code String}.
     */
    private static String formatDirName(String originalPath, String path) {
        int frontCut = originalPath.length();
        return path.substring(frontCut, path.length() - 1);
    }

}
