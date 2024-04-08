package com.github.novicezk.midjourney.bot;

import com.github.novicezk.midjourney.bot.commands.CommandsManager;
import com.github.novicezk.midjourney.bot.utils.Config;
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
    @Override
    public void run(ApplicationArguments args) {
        String token = Config.getDiscordBotToken();
        JDA api = JDABuilder
                .createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES)
                .setActivity(Activity.listening("your commands"))
                .build();

        api.addEventListener(new CommandsManager());
    }
}
