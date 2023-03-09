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

package support;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface FixtureAs {

    default InputStream fixtureAsStream(final String name) {
        final Path path = Paths.get("support/fixture").resolve(name);
        return getClass().getClassLoader().getResourceAsStream(path.toString());
    }

    default File fixtureAsFile(final String name) {
        final Path path = Paths.get("support/fixture").resolve(name);
        final URL url = getClass().getClassLoader().getResource(path.toString());
        if (url == null) return null;
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
