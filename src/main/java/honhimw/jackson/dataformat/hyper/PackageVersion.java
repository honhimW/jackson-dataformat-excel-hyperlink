/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package honhimw.jackson.dataformat.hyper;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.core.util.VersionUtil;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSource;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public final class PackageVersion implements Versioned {

    public static final String GROUP_ID = "io.github.scndry";
    public static final String ARTIFACT_ID = "jackson-dataformat-spreadsheet";
    @SuppressWarnings("java:S1845")
    public static final Version VERSION = parseVersion();

    private static Version parseVersion() {
        try {
            final String ver = determineVersion();
            if (ver != null) {
                return VersionUtil.parseVersion(ver, GROUP_ID, ARTIFACT_ID);
            }
        } catch (Exception e) { /* ignore */ }
        return Version.unknownVersion();
    }

    private static String determineVersion() throws IOException, URISyntaxException {
        final String version = PackageVersion.class.getPackage().getImplementationVersion();
        if (version != null) return version;
        final CodeSource source = PackageVersion.class.getProtectionDomain().getCodeSource();
        if (source == null) return null;
        try (JarFile jarFile = getJarFile(source)) {
            return jarFile.getManifest().getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        }
    }

    private static JarFile getJarFile(final CodeSource source) throws IOException, URISyntaxException {
        final URL location = source.getLocation();
        final URLConnection connection = location.openConnection();
        if (connection instanceof JarURLConnection) {
            return ((JarURLConnection) connection).getJarFile();
        }
        return new JarFile(new File(location.toURI()));
    }

    @Override
    public Version version() {
        return VERSION;
    }
}
