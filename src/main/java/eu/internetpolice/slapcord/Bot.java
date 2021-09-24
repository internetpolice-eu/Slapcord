package eu.internetpolice.slapcord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class Bot {
    private static Bot instance;

    private final CommentedConfigurationNode config;
    private final JDA jda;
    private final CommandManager commandManager;
    private final Logger logger = LogManager.getLogger("Slapcord");

    public Bot(String[] args) {
        instance = this;
        logger.info("Booting Slapcord v" + getVersion());

        Optional<CommentedConfigurationNode> configOptional = loadConfig();
        if (configOptional.isEmpty()) {
            throw new RuntimeException("Failed to load bot configuration. Shutting down bot.");
        }
        config = configOptional.get();

        Optional<JDA> jdaOptional = buildJda();
        if (jdaOptional.isEmpty()) {
            throw new RuntimeException("Failed to start JDA. Shutting down bot.");
        }
        jda = jdaOptional.get();
        jda.getPresence().setActivity(Activity.playing("/slap @target"));

        commandManager = new CommandManager(this);
    }

    private Optional<JDA> buildJda() {
        String token = getConfig().node("services", "discord", "auth", "token").getString();
        JDABuilder jdaBuilder = JDABuilder.createDefault(token);
        jdaBuilder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);

        try {
            return Optional.of(jdaBuilder.build().awaitReady());
        } catch (InterruptedException | LoginException ex) {
            logger.fatal("Failed to start JDA: " + ex);
            return Optional.empty();
        }
    }

    private Optional<CommentedConfigurationNode> loadConfig() {
        if (!Files.exists(Path.of("config.yml"))) {
            saveResource("config.yml", false);
        }

        final YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(Path.of("config.yml"))
                .build();
        try {
            return Optional.of(loader.load());
        } catch (IOException e) {
            getLogger().warn("An error occurred while loading bot configuration: " + e.getMessage());
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
        }

        return Optional.empty();
    }

    public @NotNull CommentedConfigurationNode getConfig() {
        return config;
    }

    public @NotNull Logger getLogger() {
        return logger;
    }

    public @NotNull JDA getJda() {
        return jda;
    }

    public @NotNull String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    public void saveResource(String resourcePath, boolean overwrite) {
        if (resourcePath == null || resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null!");
        }

        resourcePath = resourcePath.replace('\\', '/');
        Optional<InputStream> optionalIn = getResource(resourcePath);
        if (optionalIn.isEmpty()) {
            throw new IllegalArgumentException("The resource '" + resourcePath + "' cannot be found");
        }

        InputStream in = optionalIn.get();

        File outFile = new File(resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(resourcePath.substring(0, Math.max(lastIndex, 0)));

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists() || overwrite) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } else {
                logger.warn("Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
            }
        } catch (IOException ex) {
            logger.error("Could not save " + outFile.getName() + " to " + outFile, ex);
        }
    }

    public Optional<InputStream> getResource(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }

        try {
            URL url = getClass().getClassLoader().getResource(filename);

            if (url == null) {
                return Optional.empty();
            }

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return Optional.of(connection.getInputStream());
        } catch (IOException ex) {
            return Optional.empty();
        }
    }
}
