package nl.knaw.dans.easy.mirror.core;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DatasetMetadataReader {
    private final Path dve;

    public DatasetMetadataReader(Path dve) {
        this.dve = dve;
    }

    public DatasetMetadata extractDatasetMetadata() throws IOException {
        String jsonLd = extractJsonLd();
        DatasetMetadata md = new DatasetMetadata();
        DocumentContext context = JsonPath.parse(jsonLd);
        context.read("")


        return md;
    }

    private String extractJsonLd() throws IOException {
        ZipFile dveZip = new ZipFile(dve.toString());
        List<ZipEntry> jsonLdEntries = Collections.list(dveZip.entries()).stream().filter(e -> e.getName().matches("^[^/]+/metadata/oai-ore.jsonld")).collect(Collectors.toList());
        if (jsonLdEntries.size() == 1) {
            return readStringFromZipEntry(dveZip, jsonLdEntries.get(0));
        }
        else {
            throw new IllegalArgumentException(String.format("Found %d files for */metadata/oai-ore.jsonld. Expected exactly 1 file", jsonLdEntries.size()));
        }
    }

    private String readStringFromZipEntry(ZipFile file, ZipEntry entry) throws IOException {
        StringWriter stringWriter = new StringWriter();
        IOUtils.copy(file.getInputStream(entry), stringWriter, "UTF-8");
        return stringWriter.toString();
    }
}
