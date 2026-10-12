package de.falixdocs;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** /help, /links, /about und /falixdocs reload */
public class DocsCommand implements CommandExecutor, TabCompleter {

    private final FalixDocs plugin;

    public DocsCommand(FalixDocs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender s, Command c, String label, String[] a) {
        switch (c.getName().toLowerCase(Locale.ROOT)) {
            case "help" -> sendSection(s, "help");
            case "about" -> sendSection(s, "about");
            case "links" -> sendLinks(s);
            case "falixdocs" -> admin(s, a);
            default -> { }
        }
        return true;
    }

    private void admin(CommandSender s, String[] a) {
        if (a.length > 0 && a[0].equalsIgnoreCase("reload")) {
            if (!s.hasPermission("falixdocs.admin")) {
                s.sendMessage(plugin.msg("no-permission"));
                return;
            }
            plugin.reloadConfig();
            s.sendMessage(plugin.msg("reloaded"));
            return;
        }
        s.sendMessage(plugin.msg("usage", "{usage}", "/falixdocs reload"));
    }

    /** Zeigt Titel und Zeilen eines Abschnitts (help oder about). */
    private void sendSection(CommandSender s, String section) {
        Player p = s instanceof Player pl ? pl : null;
        String title = plugin.getConfig().getString(section + ".title", "");
        if (!title.isBlank()) {
            s.sendMessage(plugin.color(replace(title, p)));
        }
        for (String line : plugin.getConfig().getStringList(section + ".lines")) {
            s.sendMessage(plugin.color(replace(line, p)));
        }
    }

    /** Zeigt die Links. Im Chat sind sie anklickbar, auf der Konsole als Text. */
    private void sendLinks(CommandSender s) {
        Player p = s instanceof Player pl ? pl : null;
        String title = plugin.getConfig().getString("links.title", "");
        if (!title.isBlank()) {
            s.sendMessage(plugin.color(replace(title, p)));
        }

        List<Map<?, ?>> entries = plugin.getConfig().getMapList("links.entries");
        for (Map<?, ?> e : entries) {
            String name = text(e.get("name"));
            String url = text(e.get("url"));
            String description = text(e.get("description"));

            if (p == null) {
                s.sendMessage(plugin.color("&b" + name + " &8- &7" + url));
                continue;
            }
            TextComponent line = new TextComponent(plugin.color("&b» " + name));
            line.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
            line.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(plugin.color("&7Klicken zum Öffnen:\n&f" + url)).create()));
            line.addExtra(new TextComponent(plugin.color("  &8- &7" + description)));
            p.spigot().sendMessage(line);
        }
    }

    private static String text(Object o) {
        return o == null ? "" : o.toString();
    }

    private String replace(String text, Player p) {
        return text
                .replace("{player}", p != null ? p.getName() : "Spieler")
                .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String label, String[] a) {
        List<String> out = new ArrayList<>();
        if (c.getName().equalsIgnoreCase("falixdocs") && a.length == 1 && "reload".startsWith(a[0].toLowerCase(Locale.ROOT))) {
            out.add("reload");
        }
        return out;
    }
}
