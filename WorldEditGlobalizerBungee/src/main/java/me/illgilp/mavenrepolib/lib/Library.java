package me.illgilp.mavenrepolib.lib;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import me.illgilp.mavenrepolib.util.IOUtils;

public class Library {

    private final File jarFile;
    private String md5Hash;

    public Library(File jarFile) {
        this.jarFile = jarFile;
        File hashFile = new File(jarFile.getAbsolutePath() + ".md5");
        if (!hashFile.exists()) {
            md5Hash = null;
        } else {
            md5Hash = IOUtils.readString(hashFile);
        }
    }

    public final boolean isValid() {
        if (md5Hash == null || md5Hash.isEmpty()) return false;
        if (!jarFile.exists()) {
            return false;
        }
        String createdHash = IOUtils.hashFile(jarFile);
        if (!md5Hash.equals(createdHash)) {
            return false;
        }
        return true;
    }

    public final void addURLToClassPath(URLClassLoader classLoader) {
        try {
            @SuppressWarnings("rawtypes")
            Class clazz = URLClassLoader.class;
            @SuppressWarnings("unchecked")
            Method method = clazz.getDeclaredMethod("addURL", new Class[] { URL.class });
            method.setAccessible(true);
            method.invoke(classLoader, new Object[] { jarFile.toURL() });
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public File getJarFile() {
        return jarFile;
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    @Override
    public String toString() {
        return "Library{" +
            "jarFile=" + jarFile +
            ", jarHash='" + IOUtils.hashFile(jarFile) + "'" +
            ", md5Hash='" + md5Hash + '\'' +
            '}';
    }
}
