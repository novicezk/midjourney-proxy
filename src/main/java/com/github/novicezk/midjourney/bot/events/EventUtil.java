package com.github.novicezk.midjourney.bot.events;

import net.dv8tion.jda.api.entities.Role;

import java.util.List;

public class EventUtil {
    public static String rolesToString(List<Role> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Role role : list) {
            sb.append(" ").append(role.getName()).append(",");
        }
        return sb.deleteCharAt(sb.length()-1).append("]").toString();
    }
}
