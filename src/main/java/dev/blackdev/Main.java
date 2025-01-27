package dev.blackdev;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    static Dotenv dotenv = Dotenv.load();
    public static JDA jda;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final String[] activities = {
            "404" + " \uD83D\uDCBB",
            "Coding " + "\uD83D\uDC68\u200D\uD83D\uDCBB",
            " " + "\uD83C\uDDE9\uD83C\uDDEA"
    };

    private static int activityIndex = 0;

    public static void main(String[] args) throws InterruptedException {
        jda = JDABuilder.create(dotenv.get("TOKEN"), Arrays.asList(GatewayIntent.values()))
                .addEventListeners(new TicketListener())
                .build()
                .awaitReady();

        jda.updateCommands().addCommands(

        ).queue();
        System.out.println("Commands loaded");

        scheduler.scheduleAtFixedRate(() -> {
            jda.getPresence().setActivity(Activity.customStatus(activities[activityIndex]));
            activityIndex = (activityIndex + 1) % activities.length;
        }, 0, 5, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                shutdown();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));

        TicketUtils.sendTicketPanel(jda);
    }

    public static void shutdown() throws InterruptedException {
        jda.getPresence().setStatus(OnlineStatus.OFFLINE);
        jda.shutdown();
        if (!jda.awaitShutdown(3, TimeUnit.SECONDS))
            jda.shutdownNow();
        scheduler.shutdown();
    }
}