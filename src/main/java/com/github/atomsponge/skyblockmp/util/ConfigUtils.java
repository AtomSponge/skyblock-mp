package com.github.atomsponge.skyblockmp.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author AtomSponge
 */
@UtilityClass
public class ConfigUtils {
    /**
     * Loads a {@link com.typesafe.config.Config} from a specified {@link java.io.File}.
     * Creates the file with the default values if it does not exist.
     * If a value does not exist in the file, the value from the default config will be added to the file.
     *
     * @param loader   The class loader
     * @param resource The path to the default config resource
     * @param file     The file
     * @return The loaded config
     * @throws java.io.IOException
     */
    public static Config load(ClassLoader loader, String resource, File file) throws IOException {
        Config defaults = ConfigFactory.parseResources(loader, resource);
        Config config = ConfigFactory.parseFile(file).withFallback(defaults);

        ConfigRenderOptions renderOptions = ConfigRenderOptions.defaults().setJson(false).setOriginComments(false);
        try (PrintWriter printWriter = new PrintWriter(file)) {
            String rendered = config.root().render(renderOptions);
            String[] lines = rendered.split(System.lineSeparator());

            // Only indent two spaces
            for (int i = 0; i < lines.length; i++) {
                lines[i] = lines[i].replace("    ", "  ");
            }

            // New line after values
            for (int i = 0; i < lines.length - 1; i++) {
                String line = lines[i];
                if (!line.trim().startsWith("#") && // No new line after comments
                        !line.endsWith("{") && // No new line after "block beginnings"
                        !lines[i + 1].trim().equals("}")) { // No new line after "block endings"
                    lines[i] += System.lineSeparator();
                }
            }

            printWriter.write(StringUtils.join(lines, System.lineSeparator()));
        }
        return config;
    }
}