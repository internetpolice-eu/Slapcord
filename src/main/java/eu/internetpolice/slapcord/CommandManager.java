package eu.internetpolice.slapcord;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

public class CommandManager extends ListenerAdapter {
    private final Bot bot;

    public CommandManager(Bot bot) {
        this.bot = bot;
        bot.getJda().addEventListener(this);
        bot.getJda().updateCommands().queue();

        bot.getJda().upsertCommand(getCommandData()).queue();
    }

    private CommandData getCommandData() {
        CommandData data = new CommandData("slap", "Slaps the given user with a large trout.");
        data.addOption(OptionType.USER, "target", "Member you want to slap.", true);
        return data;
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (event.getName().equalsIgnoreCase("slap")) {
            if (event.getOption("target") != null) {
                String tagName = event.getOption("target").getAsString();
                Member target = event.getGuild().getMemberById(tagName);
                if (target != null) {
                    MessageBuilder message = new MessageBuilder("")
                        .append(event.getUser())
                        .append(" slaps ")
                        .append(target)
                        .append(" around a bit with a large trout");
                    event.reply(message.build()).queue();
                    return;
                }
            }

            event.reply(new MessageBuilder("Target cannot be found :(").build()).queue();
        }
    }
}
