package fr.xephi.authme.command.executable.authme;

import ch.jalu.injector.Injector;
import fr.xephi.authme.AuthMe;
import fr.xephi.authme.ConsoleLogger;
import fr.xephi.authme.command.ExecutableCommand;
import fr.xephi.authme.datasource.DataSource;
import fr.xephi.authme.initialization.Reloadable;
import fr.xephi.authme.initialization.SettingsDependent;
import fr.xephi.authme.message.MessageKey;
import fr.xephi.authme.service.CommonService;
import fr.xephi.authme.settings.Settings;
import fr.xephi.authme.settings.properties.DatabaseSettings;
import fr.xephi.authme.util.Utils;
import org.bukkit.command.CommandSender;

import javax.inject.Inject;
import java.util.List;

/**
 * The reload command.
 */
public class ReloadCommand implements ExecutableCommand {

    @Inject
    private AuthMe plugin;

    @Inject
    private Injector injector;

    @Inject
    private Settings settings;

    @Inject
    private DataSource dataSource;

    @Inject
    private CommonService commonService;

    @Override
    public void executeCommand(CommandSender sender, List<String> arguments) {
        try {
            settings.reload();
            ConsoleLogger.setLoggingOptions(settings);
            // We do not change database type for consistency issues, but we'll output a note in the logs
            if (!settings.getProperty(DatabaseSettings.BACKEND).equals(dataSource.getType())) {
                Utils.logAndSendMessage(sender, "Note: cannot change database type during /authme reload");
            }
            performReloadOnServices();
            commonService.send(sender, MessageKey.CONFIG_RELOAD_SUCCESS);
        } catch (Exception e) {
            sender.sendMessage("Error occurred during reload of AuthMe: aborting");
            ConsoleLogger.logException("Aborting! Encountered exception during reload of AuthMe:", e);
            plugin.stopOrUnload();
        }
    }

    private void performReloadOnServices() {
        injector.retrieveAllOfType(Reloadable.class)
            .forEach(r -> r.reload());

        injector.retrieveAllOfType(SettingsDependent.class)
            .forEach(s -> s.reload(settings));
    }
}
