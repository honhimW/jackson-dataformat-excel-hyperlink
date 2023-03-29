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

package io.github.honhimw.jackson.dataformat.hyper.poi.ooxml;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.xssf.usermodel.XSSFRelation;

@Slf4j
final class XSSFCorePart {

    private static final Pattern TRANSITIONAL_NS_PATTERN = Pattern.compile(
        "http://schemas\\.openxmlformats\\.org/(\\w+)/2006/(\\w+)");
    private final PackagePart _part;
    private final boolean _strictFormat;

    XSSFCorePart(final PackagePart corePart) {
        _part = corePart;
        _strictFormat = corePart.getPackage().isStrictOoxmlFormat();
    }

    public InputStream getInputStream() {
        try {
            return _part.getInputStream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public PackagePart getRelatedPart(final XSSFRelation rel) {
        return getRelatedPart(getRelationship(rel));
    }

    public PackagePart getRelatedPart(final PackageRelationship rel) {
        if (!_part.isRelationshipExists(rel)) {
            return null;
        }
        try {
            return _part.getRelatedPart(rel);
        } catch (InvalidFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public PackageRelationship getRelationship(final XSSFRelation rel) {
        return getRelationships(rel).getRelationship(0);
    }

    public PackageRelationshipCollection getRelationships(final XSSFRelation rel) {
        try {
            return _part.getRelationshipsByType(_relationshipType(rel));
        } catch (InvalidFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public OPCPackage getPackage() {
        return _part.getPackage();
    }

    private String _relationshipType(final XSSFRelation rel) {
        if (_strictFormat) {
            return TRANSITIONAL_NS_PATTERN.matcher(rel.getRelation()).replaceFirst("http://purl.oclc.org/ooxml/$1/$2");
        }
        return rel.getRelation();
    }
}
