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

package honhimw.jackson.dataformat.hyper.poi.ooxml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.poifs.filesystem.FileMagic;

@Slf4j
public final class PackageUtil {

    private PackageUtil() {
    }

    public static boolean isOOXML(final File source) {
        try {
            return FileMagic.valueOf(source) == FileMagic.OOXML;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean isOOXML(final InputStream source) {
        try {
            return FileMagic.valueOf(FileMagic.prepareToCheckMagic(source)) == FileMagic.OOXML;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static OPCPackage open(final File source) {
        try {
            return OPCPackage.open(source, PackageAccess.READ);
        } catch (InvalidFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static OPCPackage open(final InputStream source) {
        try {
            return OPCPackage.open(source);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InvalidFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static PackagePart extractCorePart(final File source) {
        return _extractCorePart(open(source));
    }

    public static PackagePart extractCorePart(final InputStream source) {
        return _extractCorePart(open(source));
    }

    private static PackagePart _extractCorePart(final OPCPackage pack) {
        final PackageRelationship rel = _extractCoreRelationship(pack);
        if (rel == null) {
            throw new IllegalArgumentException("No core document relationship");
        }
        final PackagePartName name;
        try {
            name = PackagingURIHelper.createPartName(rel.getTargetURI());
        } catch (InvalidFormatException e) {
            throw new IllegalArgumentException(e);
        }
        return pack.getPart(name);
    }

    private static PackageRelationship _extractCoreRelationship(final OPCPackage pack) {
        final PackageRelationship rel = pack.getRelationshipsByType(PackageRelationshipTypes.CORE_DOCUMENT)
            .getRelationship(0);
        if (rel != null) {
            return rel;
        }
        return pack.getRelationshipsByType(PackageRelationshipTypes.STRICT_CORE_DOCUMENT).getRelationship(0);
    }
}
