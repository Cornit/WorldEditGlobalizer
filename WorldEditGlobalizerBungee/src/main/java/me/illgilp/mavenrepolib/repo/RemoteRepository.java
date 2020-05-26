package me.illgilp.mavenrepolib.repo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import me.illgilp.mavenrepolib.lib.Library;
import me.illgilp.mavenrepolib.listener.ProgressListener;

public class RemoteRepository implements Repository {

    private String url;
    private File libsFolder;

    public RemoteRepository(String url, File libsFolder) {
        this.url = url;
        this.libsFolder = libsFolder;
    }

    @Override
    public Library getLibrary(String group, String artifact, String version, ProgressListener progressListener) {
        String spec = new StringBuilder()
            .append(group.replace(".", "/"))
            .append("/")
            .append(artifact)
            .append("/")
            .append(version)
            .append("/")
            .append(artifact)
            .append("-")
            .append(version)
            .append(".jar")
            .toString();
        File libFile = new File(libsFolder, spec);
        if (!libFile.exists()) {
            if (download(group, artifact, version, progressListener)) {
                return new Library(libFile);
            }
            return null;
        } else {
            Library library = new Library(libFile);
            if (!library.isValid()) {
                if (download(group, artifact, version, progressListener)) {
                    return new Library(libFile);
                }
                return null;
            } else {
                return new Library(libFile);
            }
        }
    }

    @Override
    public boolean exists(String group, String artifact, String version) {
        try {
            String spec = new StringBuilder()
                .append(group.replace(".", "/"))
                .append("/")
                .append(artifact)
                .append("/")
                .append(version)
                .append("/")
                .append(artifact)
                .append("-")
                .append(version)
                .append(".jar")
                .toString();

            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(new URL(url), spec).openConnection();
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == 200) {
                return true;
            }
            return false;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean download(String group, String artifact, String version, ProgressListener progressListener) {
        try {
            String spec = new StringBuilder()
                .append(group.replace(".", "/"))
                .append("/")
                .append(artifact)
                .append("/")
                .append(version)
                .append("/")
                .append(artifact)
                .append("-")
                .append(version)
                .append(".jar")
                .toString();

            File libFile = new File(libsFolder, spec);
            if (progressListener != null) {
                progressListener.onStart(libFile);
            }
            if (!downloadFile(libFile, new URL(new URL(url), spec).toString(), progressListener)) {
                if (progressListener != null) {
                    if (progressListener.onFailed(libFile)) {
                        download(group, artifact, version, progressListener);
                    }
                }
                return false;
            }

            if (!downloadFile(new File(libFile.getParentFile(), libFile.getName() + ".md5"), new URL(new URL(url), spec + ".md5").toString(), progressListener)) {
                if (progressListener != null) {
                    if (progressListener.onFailed(libFile)) {
                        download(group, artifact, version, null);
                    }
                }
                return false;
            }

            Library library = new Library(libFile);
            if (!library.isValid()) {
                if (progressListener != null) {
                    if (progressListener.onFailed(libFile)) {
                        download(group, artifact, version, progressListener);
                    }
                }
                return false;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (progressListener != null) {
            progressListener.onDone(libsFolder);
        }
        return true;
    }

    private boolean downloadFile(File file, String url, ProgressListener progressListener) {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() != 200) {
                return false;
            }
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                byte dataBuffer[] = new byte[1024];
                long curr = 0;
                int bytesRead;
                while ((bytesRead = httpURLConnection.getInputStream().read(dataBuffer)) > 0) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                    curr += bytesRead;
                    if (progressListener != null) {
                        progressListener.onProgressChange(file, curr, httpURLConnection.getContentLengthLong());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            httpURLConnection.getInputStream().close();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
