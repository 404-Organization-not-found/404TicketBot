package dev.blackdev;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TicketListener extends ListenerAdapter {
    private static final long TICKET_CATEGORY_ID = 1333229079314432111L;
    private static final long CLOSED_TICKET_CATEGORY_ID = 1332826722608877679L;
    private static final long ROLE_1_ID = 1332113031945261158L;
    private static final long ROLE_2_ID = 1332112796959375391L;
    private final Map<Long, Long> ticketCreators = new HashMap<>();

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("ticket:select")) {
            String selected = event.getValues().get(0);
            Guild guild = event.getGuild();
            User user = event.getUser();
            if (guild != null) {
                Category category = guild.getCategoryById(TICKET_CATEGORY_ID);
                if (category != null) {
                    guild.createTextChannel("ticket-" + user.getName(), category)
                            .addPermissionOverride(guild.getRoleById(ROLE_1_ID), Arrays.asList(Permission.VIEW_CHANNEL), null)
                            .addPermissionOverride(guild.getRoleById(ROLE_2_ID), Arrays.asList(Permission.VIEW_CHANNEL), null)
                            .addPermissionOverride(guild.getPublicRole(), null, Arrays.asList(Permission.VIEW_CHANNEL))
                            .addPermissionOverride(guild.getMember(user), Arrays.asList(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND), null)
                            .queue(channel -> {
                                ticketCreators.put(channel.getIdLong(), user.getIdLong());
                                Role role = guild.getRoleById(ROLE_1_ID);
                                Role role2 = guild.getRoleById(ROLE_2_ID);
                                channel.sendMessage("Ticket created by " + user.getAsMention() + " for reason: " + selected + ". A Staff member will be with you shortly. " + role.getAsMention() + " " + role2.getAsMention())
                                        .mentionRoles(ROLE_1_ID, ROLE_2_ID)
                                        .setActionRow(Button.danger("ticket:close", "Close Ticket"))
                                        .queue();
                            });
                }
            }
            event.reply("Ticket created!").setEphemeral(true).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("ticket:close")) {
            TextChannel channel = event.getChannel().asTextChannel();
            Guild guild = event.getGuild();
            if (guild != null) {
                Category closedCategory = guild.getCategoryById(CLOSED_TICKET_CATEGORY_ID);
                if (closedCategory != null) {
                    Long creatorId = ticketCreators.get(channel.getIdLong());
                    if (creatorId != null) {
                        User creator = guild.getJDA().getUserById(creatorId);
                        if (creator != null) {

                            channel.getHistory().retrievePast(100).queue(messages -> {
                                Collections.reverse(messages);
                                StringBuilder transcript = new StringBuilder();
                                for (Message message : messages) {
                                    transcript.append(message.getAuthor().getName())
                                            .append(": ")
                                            .append(message.getContentDisplay())
                                            .append("\n");
                                }
                                try {
                                    File transcriptFile = new File("transcript-" + channel.getName() + ".txt");
                                    FileWriter writer = new FileWriter(transcriptFile);
                                    writer.write(transcript.toString());
                                    writer.close();

                                    creator.openPrivateChannel().queue(privateChannel -> {
                                        privateChannel.sendMessage("Here is the transcript of your ticket:")
                                                .addFiles(net.dv8tion.jda.api.utils.FileUpload.fromData(transcriptFile))
                                                .queue();
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });


                            channel.getManager().setParent(closedCategory).queue();
                            channel.getPermissionOverride(guild.getMember(creator))
                                    .getManager()
                                    .deny(Permission.VIEW_CHANNEL)
                                    .queue();
                            channel.sendMessage("Ticket closed by " + event.getUser().getAsMention())
                                    .mentionRoles(ROLE_1_ID, ROLE_2_ID)
                                    .queue();
                        }
                    }
                }
            }
            event.reply("Ticket closed!").setEphemeral(true).queue();
        }
    }
}