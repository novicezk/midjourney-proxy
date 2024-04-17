package com.github.novicezk.midjourney.bot;

import com.github.novicezk.midjourney.bot.commands.CommandsManager;
import com.github.novicezk.midjourney.bot.queue.QueueManager;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.controller.SubmitController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class AdamBotInitializer implements ApplicationRunner {
    private static volatile JDA apiInstance;
    private final SubmitController submitController;

    @Override
    public void run(ApplicationArguments args) {
        // Checking of the JDA was created already
        if (apiInstance == null) {
            synchronized (AdamBotInitializer.class) {
                if (apiInstance == null) {
                    String token = Config.getDiscordBotToken();
                    apiInstance = JDABuilder
                            .createDefault(token)
                            .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES)
                            .setActivity(Activity.listening("your commands"))
                            .build();

                    apiInstance.addEventListener(new CommandsManager(submitController));
                }
            }
        }
    }

    public static JDA getApiInstance() {
        if (apiInstance == null) {
            throw new IllegalStateException("JDA has not been initialized yet.");
        }
        return apiInstance;
    }
}
