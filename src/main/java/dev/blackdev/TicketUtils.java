// src/main/java/dev/blackdev/TicketUtils.java
package dev.blackdev;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.awt.Color;
import java.util.concurrent.CompletableFuture;

public class TicketUtils {
    private static final long TICKET_PANEL_CHANNEL_ID = 1332094477766099008L;
    private static final long ROLE_1_ID = 1332113031945261158L;
    private static final long ROLE_2_ID = 1332112796959375391L;

    public static void sendTicketPanel(JDA jda) {
        TextChannel channel = jda.getTextChannelById(TICKET_PANEL_CHANNEL_ID);
        if (channel != null) {
            // Clear the channel
            CompletableFuture<Void> clearChannelFuture = channel.getIterableHistory().takeAsync(100)
                    .thenCompose(messages -> {
                        CompletableFuture<?>[] futures = messages.stream()
                                .map(message -> message.delete().submit())
                                .toArray(CompletableFuture[]::new);
                        return CompletableFuture.allOf(futures);
                    });

            clearChannelFuture.thenRun(() -> {

                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Ticket System")
                        .setDescription("Select an option to create a ticket")
                        .setColor(Color.BLUE);

                StringSelectMenu menu = StringSelectMenu.create("ticket:select")
                        .addOption("General Support", "general")
                        .addOption("Technical Support", "technical")
                        .build();

                MessageCreateData message = new MessageCreateBuilder()
                        .setEmbeds(embed.build())
                        .setActionRow(menu)
                        .build();

                channel.sendMessage(message).queue();
            });
        }
    }
}